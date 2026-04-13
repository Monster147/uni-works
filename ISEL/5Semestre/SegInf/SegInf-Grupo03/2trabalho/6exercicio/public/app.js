async function loadUser() {
    const userInfo = document.getElementById("user-info");
    const roleSpan = document.getElementById("role");
    const googleBtn = document.getElementById("google-btn");
    const githubBtn = document.getElementById("github-btn");
    const logoutBtn = document.getElementById("logout-btn");

    try {
        const role = await fetch("/me");

        if (role.status === 401) {
            roleSpan.textContent = "Not Logged In - No Role Assigned";

            googleBtn.style.display = "block";
            githubBtn.style.display = "block";
            logoutBtn.style.display = "none"; // hide logout if not logged in

            return;
        }

        const data = await role.json();

        // GOOGLE LOGIN CONTROL
        if (data.google) {
            googleBtn.style.display = "none";

            roleSpan.textContent = data.google.role || "Not Logged In - No Role Assigned"; 
            userInfo.innerHTML += `
                <p><strong>Google User:</strong> ${data.google.email}</p>
                <img src="${data.google.picture}" width="64" style="border-radius:50%">
            `;

            if (data.google.role === "regular") {
                document.getElementById("listName").placeholder="GitTasks Default List";
            }

            if (data.google.role === "premium") {
                document.getElementById("listName").disabled=false;
            }

        } else {
            googleBtn.style.display = "block";
        }


        // GITHUB LOGIN CONTROL
        if (data.github) {
            githubBtn.style.display = "none";
            console.log("GitHub user data:", data.github);
            userInfo.innerHTML += `
                <p><strong>GitHub User:</strong> ${data.github.username}</p>
                <img src="${data.github.avatar}" width="64" style="border-radius:50%">
            `;
        } else {
            githubBtn.style.display = "block";
        }

         if (data.google || !data.github) {
            logoutBtn.style.display = "block";
        } else {
            logoutBtn.style.display = "none";
        }

    } catch (err) {
        console.error("Error loading user:", err);
    }
}

async function fetchRepoInfo() {
    const repoLink = document.getElementById("repo-link").value;

    const response = await fetch(`/github/repo?url=${encodeURIComponent(repoLink)}`);
    const data = await response.json();

    document.getElementById("repo-info").textContent = JSON.stringify(data, null, 2);
}

async function fetchMilestones() {
    const owner = document.getElementById("ownerM").value;
    const repo = document.getElementById("repoM").value;

    const response = await fetch(`/milestones?owner=${owner}&repo=${repo}`);
    const date = await response.json();

    document.getElementById("milestones").textContent = JSON.stringify(date, null, 2);
}

async function fetchRepositories() {
    const response = await fetch('/github/userRepos')
    const data = await response.json();

    document.getElementById("repositories").textContent = JSON.stringify(data, null, 2);
}

async function fetchTasks() {
    const response = await fetch('/tasks')
    const data = await response.json();
    document.getElementById("tasks").textContent = JSON.stringify(data, null, 2);
}

async function fetchTask() {
    const listNumber = document.getElementById("listNumber").value || -1;

    const response = await fetch(`/task/${listNumber}`);
    const data = await response.json();

    document.getElementById("task").textContent = JSON.stringify(data, null, 2);
}

async function createTask() {
    const repo = document.getElementById("repoT").value;
    const owner = document.getElementById("ownerT").value;
    const milestoneNumber = document.getElementById("milestoneNumber").value;
    const listName = document.getElementById("listName")?.value;

    console.log({ repo, owner, milestoneNumber, listName });

    const response = await fetch('/github/tasks/from-milestone', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ repo, owner, milestoneNumber, listName })
    });
    const data = await response.json();
    document.getElementById("newTask").textContent = JSON.stringify(data, null, 2);
}

async function logout() {
    await fetch('/logout', { method: 'POST' });
    window.location.reload();
}