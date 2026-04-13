require('dotenv').config({override: true});

const axios = require('axios');
const { newEnforcer } = require('casbin');
const path = require('path');

let enforcer;

(async () => {
  enforcer = await newEnforcer(
    path.join(__dirname, "model.conf"),
    path.join(__dirname, "policy.csv")
  );
  console.log("Casbin enforcer loaded");
})();

const pdp = async function(s, o, a) {
  if (!enforcer) {
    throw new Error("Casbin enforcer not loaded yet");
  }
  const r = await enforcer.enforce(s, o, a);
  return {res: r, sub: s, obj: o, act: a};
}

/*
    * Middleware to verify if user is authenticated
    * If authenticated, calls next()
    * If not, responds with 401 Unauthorized
*/
function requireAuth(req, resp, next) {
    console.log("Request received:", req.originalUrl);
    if (!req.session || !req.session.user) {
        console.log("User not authenticated");
        return resp.status(401).json({ error: "Not authenticated" });
    }
    console.log("User authenticated:", req.session.user.email);
    next();
}

/*
    * Middleware to verify if user has permissions for object and action
    * If permitted, calls next()
    * If not, responds with 403 Forbidden
*/
function requirePermissions(object, action) {
    return async function (req, resp, next) {
        const email = req.session.user.email;
        console.log("Casbin checking permissons...");
        console.log(`Checking permissions for user on object: ${object}, action: ${action}`);

        if (!enforcer) {
            console.log("Casbin enforcer not load yet");
            return resp.status(500).send({ error: "Enforcer not ready" });
        }
        const decision = await enforcer.enforce(email, object, action);
        console.log("Casbin decision:", decision);

        if (!decision) {
            console.log("Access denied by Casbin");
           return resp.status(403).send({ error: "Access denied. Need higher role." });
        }
        console.log("Access granted by Casbin");
        next();
    };
}

async function assignRole(email) {
    if (!enforcer) {
      throw new Error("Casbin enforcer not loaded yet");
    }
    const res = await enforcer.getRolesForUser(email);
    return res[0];
}

function parseRepoUrl(url) {
    const regex = /github\.com\/([^\/]+)\/([^\/]+)/;
    const match = url.match(regex);

    if (!match) return null;

    return {
        owner: match[1],
        repo: match[2].replace(/\.git$/, "")
    };
}


/*
  * Refresh Google OAuth2 access token using refresh token
*/
async function refreshGoogleToken(req) {
  const refresh_token = req.session.user.googleTokens.refresh_token;

  const form = new URLSearchParams();
  form.append("client_id", process.env.GOOGLE_CLIENT_ID);
  form.append("client_secret", process.env.GOOGLE_CLIENT_SECRET);
  form.append("refresh_token", refresh_token);
  form.append("grant_type", "refresh_token");

  const resp = await axios.post(
    "https://oauth2.googleapis.com/token",
    form.toString(),
    { headers: { "Content-Type": "application/x-www-form-urlencoded" } }
  );

  req.session.user.googleTokens.access_token = resp.data.access_token;
  req.session.user.googleTokens.expires_at = Date.now() + resp.data.expires_in * 1000;
}

/*
  * Ensure valid Google OAuth2 access token, refreshing if necessary
*/
async function ensureGoogleToken(req) {
  const tokens = req.session.user.googleTokens;

  if (Date.now() > tokens.expires_at - 3000) {
    await refreshGoogleToken(req);
  }

  return req.session.user.googleTokens.access_token;
}

/*
  * Get or create a Google Tasks list with the given name
*/
async function getOrCreateList(accessToken, listName) {
    const lists = await axios.get(
        "https://tasks.googleapis.com/tasks/v1/users/@me/lists",
        { headers: { Authorization: `Bearer ${accessToken}` } }
    );

    const found = lists.data.items.find(list => list.title === listName);
    if (found) return found.id;

    const newList = await axios.post(
        "https://tasks.googleapis.com/tasks/v1/users/@me/lists",
        { title: listName },
        { headers: { Authorization: `Bearer ${accessToken}` } }
    );

    return newList.data.id;
}

module.exports = { pdp, requireAuth, requirePermissions, ensureGoogleToken, assignRole, parseRepoUrl, getOrCreateList };