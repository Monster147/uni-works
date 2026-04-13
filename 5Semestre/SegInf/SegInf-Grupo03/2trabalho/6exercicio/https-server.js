require('dotenv').config({override: true});

const express = require('express');
const cookieParser = require('cookie-parser');
const axios = require('axios');
const jwt = require('jsonwebtoken');
const path = require('path');
const session = require('express-session');
const { pdp, requireAuth, requirePermissions, assignRole, ensureGoogleToken, parseRepoUrl, getOrCreateList } = require('./RBAC-casbin/server-support-functions');
const app = express();
const SESSION_SECRET = process.env.SESSION_SECRET
const HMAC_SECRET = process.env.HMAC_SECRET
const CALLBACK = 'callback';
// system variables where Client credentials are stored
const GOOGLE_CLIENT_ID = process.env.GOOGLE_CLIENT_ID
const GOOGLE_CLIENT_SECRET = process.env.GOOGLE_CLIENT_SECRET
const PORT = process.env.PORT || 3001;
const OAUTH2_REDIRECT_URI = process.env.OAUTH2_REDIRECT_URI || `http://localhost:${PORT}/auth/callback`
const GITHUB_CLIENT_ID = process.env.GITHUB_CLIENT_ID
const userTaskListMap = {}; //to not utilize the ids of task lists directly

const DEFAULT_LIST_NAME = "GitTasks Default List";

app.use(cookieParser());
app.use(session({
  secret: SESSION_SECRET,
  resave: false,
  saveUninitialized: false,
  cookie: { httpOnly: true }
}));
app.use(express.static("public"));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
/*
app.get('/', (req, res) => {
  res.send('Sou um servidor que não funciona?');
});
*/
app.get("/me", (req, res) => {
    if (!req.session.user) {
        return res.status(401).json({ error: "Not authenticated" });
    }

console.log("User info requested:", req.session.user);

    res.json({
        google: req.session.user || null,
        github: req.session.github || null
    });
});


app.get('/protectedresource',
    requireAuth,
    requirePermissions("milestones", "view"),
    (req, res) => {
        res.status(200).send("Access to protected resource granted");
    }
);

app.get('/printcookies', (req, resp) => {
    console.log("Cookies received:", req.cookies    );
    resp.statusCode = 200;
    resp.json(req.cookies);
});

app.get('/setcookies', (req, resp) => {
    resp.cookie('C1', 'A', { httpOnly: true });
    resp.cookie('C2', 'B', { httpOnly: true });
    resp.cookie('mycookie', 'some-value', { expires: new Date(Date.now() + 900000), httpOnly: true });
    resp.send('cookies set defined');
})

const crypto = require('crypto');
const hmac = crypto.createHmac('sha256', SESSION_SECRET);

app.get('/setcookies-hmac', (req, resp) => {
    const id = crypto.randomBytes(16).toString('hex'); //32 chars hex
    const secret = HMAC_SECRET;
    //create and compute hmac
    const hmac = crypto.createHmac('sha256', secret).update(id).digest();

    // convert to base64
    const hBase64 = hmac.toString('base64');

    //define cookies
    resp.cookie('MyAppCookie', id, { expires: new Date(Date.now() + 900000), httpOnly: true});
    resp.cookie('T', hBase64, { expires: new Date(Date.now() + 900000), httpOnly: true });
    console.log('MyAppCookie=',id);
    console.log('T=',hBase64);
    resp.send('Cookie MyAppCookie is protected by tag *T*');

});

app.get('/premium', requireAuth, 
    requirePermissions("tasks", "create_custom_list_task"), (req, resp) => {
    resp.send("Welcome to premium content area!");
});

app.get('/regular', requireAuth, requirePermissions("tasks", "create_default_list_task"), (req, resp) => {
    resp.send("Welcome to regular content area!");
});

app.get('/free', requireAuth, requirePermissions("milestones", "view"), (req, resp) => {
    resp.send("Welcome to free content area!");
});


/*
    * Redirect user to Google OAuth2 authorization endpoint
*/
app.get('/login/google', (req, resp) => {
    //If already authenticated, skip login
    if (req.session.user) {
        console.log("User already authenticated, skipping Google login.");
        return resp.redirect('/'); 
    }

    const state = "random-" + Math.random();
    resp.cookie("oauth_state", state);
    const scope = ['openid', 'email', 'https://www.googleapis.com/auth/tasks'].join(' ');
    const url =
        // authorization endpoint
        "https://accounts.google.com/o/oauth2/v2/auth?" +
        
        // google client id
        `client_id=${GOOGLE_CLIENT_ID}` +

        // OpenID scope "openid email"
        `&scope=${encodeURIComponent(scope)}` +

        // parameter state is used to check if the user-agent requesting login is the same making the request to the callback URL
        // more info at https://www.rfc-editor.org/rfc/rfc6749#section-10.12
        `&state=${state}` +

        // responde_type for "authorization code grant"
        `&response_type=code` +

        // redirect uri used to register RP
        `&redirect_uri=${encodeURIComponent(OAUTH2_REDIRECT_URI)}`;
    resp.redirect(302, url);
});

/*
    * Process the code, exchange it for tokens and authenticate the user
*/
app.get('/auth/'+CALLBACK, async (req, resp) => {
    console.log("Google callback received");

    //Validate state
    if (req.query.state !== req.cookies.oauth_state) {
        return resp.status(400).send("State mismatch — possible CSRF attack");
    }

    const code = req.query.code;
    if(!code) return resp.status(400).send("No code received");

    console.log('making request to token endpoint')
    //Prepare token exchange
    // content-type: application/x-www-form-urlencoded (URL-Encoded Forms)
    const form = new URLSearchParams();
    form.append('code', req.query.code);
    form.append('client_id', GOOGLE_CLIENT_ID);
    form.append('client_secret', GOOGLE_CLIENT_SECRET);
    form.append('redirect_uri', 'http://localhost:'+PORT+'/auth/'+CALLBACK);
    form.append('grant_type', 'authorization_code');
    console.log(form);
    try {
        console.log("Requesting token from Google...");
        //trade code for tokens
        const tokenResp = await axios.post('https://oauth2.googleapis.com/token',
            form.toString(),
            { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
    );
        console.log("Token response:", tokenResp.data);
      
        const tokens = tokenResp.data;

        // decode id_token from base64 encoding
        const jwt_payload = jwt.decode(tokens.id_token);
        console.log("ID token payload:", jwt_payload);

        //obtain user info
        const userInfo = await axios.get('https://openidconnect.googleapis.com/v1/userinfo',
            { headers: { Authorization: `Bearer ${tokens.access_token}` } } 
        );

        const role = await assignRole(userInfo.data.email);
        
        if(!role || role.length === 0) {
            return resp.status(403).json("User not registered in the system");
        }

        //store user info in memory
        req.session.user = {
            email: userInfo.data.email,
            name: userInfo.data.name,
            picture: userInfo.data.picture,
            role: role,
            googleTokens: {
                access_token: tokens.access_token,
                refresh_token: tokens.refresh_token,
                expires_at : Date.now() + (tokens.expires_in * 1000)
            }
        };
        console.log("User authenticated:", req.session.user);

        resp.redirect('/');
       // resp.redirect('/login/github'); //mais rapido para testar
    } catch (error) {
            console.error("Google callback error:", error.response?.data || error);
            resp.status(500).json("Error on Google authentication");        
    }
});

//to access milestones from a private repo
app.get('/login/github', requireAuth,(req, resp) => {
    const redirect = "https://github.com/login/oauth/authorize" +
        `?client_id=${GITHUB_CLIENT_ID}` +
        "&scope=repo%20read:user";   //add repo scope to read private repositories in the future

    console.log("Redirecting to GitHub OAuth:", redirect);
    resp.redirect(redirect);
});

app.get('/github/'+CALLBACK, async (req, resp) => {
    const code = req.query.code;
    if(!code) {
        return resp.status(400).json("No code received");
    } else console.log("GitHub code received:", code);

    const tokenResp = await axios.post('https://github.com/login/oauth/access_token', {
        client_id: process.env.GITHUB_CLIENT_ID,
        client_secret: process.env.GITHUB_CLIENT_SECRET,
        code: code,
        redirect_uri: process.env.GITHUB_REDIRECT_URI
    },
        { headers: { Accept: "application/json" } }
    );

    console.log("GitHub token response:", tokenResp.data);
    const { access_token } = tokenResp.data;

    if (!access_token) {
        console.error("GitHub OAuth failed:", tokenResp.data);
        return resp.status(500).json("GitHub OAuth error");
    }

    console.log("GitHub access token:", access_token);

    const userResp = await axios.get("https://api.github.com/user", {
        headers: {
            Authorization: `Bearer ${access_token}`,
            Accept: "application/vnd.github+json"
        }
    });

    const username = userResp.data.login;
    const avatar = userResp.data.avatar_url;

     console.log("GitHub user:", username);

    //save in memory
    req.session.github = { access_token, username, avatar };

    resp.redirect('/');
});

//Playload milestones from a given repo
app.get("/milestones", requireAuth, requirePermissions("milestones", "view"), async (req, resp) => {
    const token = req.session.github?.access_token; //Optional. Required for private repos
    const owner = req.query.owner;
    const repo = req.query.repo;
     if (!owner || !repo) {
        return resp.status(400).json("Missing owner or repo");
    }

    console.log( `https://api.github.com/repos/${owner}/${repo}/milestones`)

    try {
        const gitResp = await axios.get(
            `https://api.github.com/repos/${owner}/${repo}/milestones`,
            {
                headers: {
                   ...(token && { Authorization: `Bearer ${token}` }), // Add only if exists
                    Accept: "application/vnd.github+json"
                }
            }
        );

        const json = gitResp.data.map(milestone => ({
                Title: milestone.title, 
                Description: milestone.description,
                Milestone_Number: milestone.number,
                Creator: milestone.creator.login,
                State: milestone.state,
                Created_At: milestone.created_at,
                Updated_At: milestone.updated_at,
                Due_On: milestone.due_on,
                Closed_At: milestone.closed_at || "Ainda não terminada",
                Milestone_Url: milestone.html_url,
                Repo_Url: milestone.creator.repos_url
            }))

        resp.status(200).json({ json });
    } catch (error) {
       console.error("GitHub API error:", error.response?.data || error);
        const status = error.response?.status;
        if (status === 404) {
            return resp.status(404).json("Repository not found or private and user dont have access");
        }
        if (status === 403) {
            return resp.status(403).json("Access denied (private repo & no GitHub auth)");
        }

        return resp.status(500).json("Failed to fetch milestones");
    }
});

//obtain details from a given repo
app.get("/github/repo", requireAuth, requirePermissions("milestones", "view"), async (req, resp) => {
    const token = req.session.github?.access_token;
    const url = req.query.url;
    if (!url) {
        return resp.status(400).json({ error: "Missing repo URL" });
    }

    const repoInfo = parseRepoUrl(url);
    if (!repoInfo) {
        return resp.status(400).json({ error: "Invalid GitHub repo URL" });
    }

    const { owner, repo } = repoInfo;
    try {
        const response = await axios.get(`https://api.github.com/repos/${owner}/${repo}`, {
            headers: {
                ...(token && { Authorization: `Bearer ${token}` }), // Add only if exists
                Accept: "application/vnd.github+json",
            }
        });
        const json = { //response returns only one object
            Repo: response.data.name,
            Owner: response.data.owner.login,
            Description: response.data.description || "No description",
            Url: response.data.html_url,
            Private: response.data.private,
        };

        console.log("GitHub repo info:", json);

        resp.json(json);
    } catch (err) {
        console.error("GitHub repo error:", err.response?.data || err);
        if (err.response?.status === 404) {
            return resp.status(404).json({ error: "Repository not found" });
        }
        resp.status(500).json("Failed to fetch repos");
    }
});

//repo list from a github user
app.get("/github/userRepos", requireAuth, requirePermissions("milestones", "view"), async (req, resp) => {
    if (!req.session.github || !req.session.github.access_token) {
        return resp.status(401).json("User not authenticated with GitHub");
    }
    const token = req.session.github.access_token;

    try {
        const response = await axios.get("https://api.github.com/user/repos", {
            headers: {
                Authorization: `Bearer ${token}`,
                Accept: "application/vnd.github+json"
            }
        });
        const json = response.data.map(repo => ({
            Repo: repo.name,
            Owner: repo.owner.login,
            Description: repo.description || "No description",
            Url: repo.html_url,
            Private: repo.private,            
        }));

        resp.json(json);
    } catch (err) {
        console.error("GitHub repo error:", err.response?.data || err);
        resp.status(500).json("Failed to fetch repos");
    }
});


// Placeholder for tasks endpoint
app.get("/tasks", requireAuth, requirePermissions("tasks", "view"), async (req, resp) => {
    try {
        const accessToken = await ensureGoogleToken(req);
        const userEmail = req.session.user.email;
        console.log("Fetching tasks for user:", userEmail);

        const response = await axios.get(
             "https://tasks.googleapis.com/tasks/v1/users/@me/lists",
             {
                headers: {
                    Authorization: `Bearer ${accessToken}`
                }
             }
        );  

        let listNumber = 1;
        console.log(response.data);
        userTaskListMap[userEmail] = response.data.items.map(list => ({
            Title: list.title,
            List_Number: listNumber++,      
            Id: list.id,             
            Updated: list.updated
        }));

        const json = userTaskListMap[userEmail].map(item => ({
            Title: item.Title,
            List_Number: item.List_Number,
            Updated: item.Updated
        }));

        resp.status(200).json(json);
    } catch (error) {
        console.error("GitHub Tasks error:", error.response?.data || error);
        resp.status(500).json("Failed to fetch tasks");
    }
});

// Placeholder for specific task list endpoint
app.get('/task/:listNumber', requireAuth, requirePermissions("tasks", "view"), async (req, resp) => {
    try {
        const userEmail = req.session.user.email;
        const value = req.params.listNumber;
        if(value < 0 || value.trim() === ""){
            return resp.status(404).json("List number is required");
        }

        const listNumber = parseInt(value);
        const accessToken = await ensureGoogleToken(req);

        const lists = userTaskListMap[userEmail];
        if (!lists) {
            return resp.status(404).json("No lists loaded for this user");
        }

        const list = lists.find(l => l.List_Number === listNumber);
        if (!list) {
            return resp.status(404).json("List not found");
        }

        // Real GoogleId
        const googleListId = list.Id;

        const response = await axios.get(
            `https://www.googleapis.com/tasks/v1/lists/${googleListId}/tasks`,
            {
                headers: {
                    Authorization: `Bearer ${accessToken}`
                }
            }
        );

        const json = response.data.items?.map(item => ({
            Title: item.title,
            Kind: item.kind,
            Updated: item.updated,
            Status: item.status,
            Due: item.due || "No due date",
            Links: item.links,
            WebViewLink: item.webViewLink
        })) || [];

        resp.status(200).json(json);

    } catch (error) {
        console.error("Error fetching tasks:", error.response?.data || error);
        resp.status(500).json("Error fetching tasks");
    }
});


// Placeholder for creating a new task
app.post('/github/tasks/from-milestone', requireAuth, requirePermissions("tasks", "create_default_list_task"), async (req, resp) => {
    try {
        const user = req.session.user;
        const googleToken = await ensureGoogleToken(req);
        const githubToken = req.session.github?.access_token;
        //if (!githubToken) return resp.status(401).json({ error: "Not authenticated with GitHub" });

        const { owner, repo, milestoneNumber, listName } = req.body;
        console.log("Creating task from milestone:", owner, repo, milestoneNumber, listName);
        if (!owner || !repo || !milestoneNumber) return resp.status(400).json({ error: 'Missing owner, repo or milestoneNumber' });

        // 1. Fetch milestone from GitHub
        const milestoneResp = await axios.get(
            `https://api.github.com/repos/${owner}/${repo}/milestones/${milestoneNumber}`,
            { headers: { ...(githubToken && { Authorization: `Bearer ${githubToken}` }) } }
        );

        const milestone = milestoneResp.data;

        // 2. Prepare Google Tasks payload
        const taskData = {
            title: `Milestone: ${milestone.title}`,
            notes: `GitHub milestone ID: ${milestone.id}\n\n${milestone.description || ""}`,
            due: milestone.due_on ? milestone.due_on : undefined
        };

        // 3. Determine list name by role
        let finalListName;

        if (user.role === "free") {
            return resp.status(403).json({ error: "Free users cannot create tasks" });
        } 
        else if (user.role === "regular") {
            finalListName = DEFAULT_LIST_NAME;
        } 
        else if (user.role === "premium") {
            finalListName = listName || DEFAULT_LIST_NAME;
        }

        // 4. Get or create list in Google Tasks
        const listId = await getOrCreateList(googleToken, finalListName);

        // 5. Create task
        const taskResp = await axios.post(
            `https://tasks.googleapis.com/tasks/v1/lists/${listId}/tasks`,
            taskData,
            { headers: { Authorization: `Bearer ${googleToken}` } }
        );

        resp.status(201).json({ message: "Task created successfully" });

    } catch (error) {
        console.error("Error creating task:", error.response?.data || error);
        return resp.status(500).json({ error: "Failed to create task from milestone:", message: error.response?.data?.message || error.message});
    }
});

app.post('/logout', requireAuth, (req, resp) => {
    req.session.destroy(err => {
        if (err) {
            console.error("Logout error:", err);
            return resp.status(500).json({ error: "Failed to logout" });
        }
        resp.clearCookie('connect.sid');
        resp.status(200).json({ message: "Logged out successfully" });
    });
});

app.listen(PORT, (err) => {
    if (err) {
        return console.log('something bad happened', err)
    }
    console.log(`server is listening on ${PORT}`)
})