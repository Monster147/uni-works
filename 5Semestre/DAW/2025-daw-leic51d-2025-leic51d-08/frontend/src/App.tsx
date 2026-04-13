// @ts-ignore
import React from "react";
import {createRoot} from "react-dom/client";
import {createBrowserRouter, RouterProvider} from "react-router";
import {Home} from "./components/Home";
import {Login} from "./components/Login";
import {Register} from "./components/Register";
import {ProtectedRoute} from "./components/ProtectedRoute";
import {AuthProvider} from "./AuthContext";
import {CreateLobby} from "./components/CreateLobby";
import {UserProfile} from "./components/UserProfile";
import {LobbiesList} from "./components/LobbiesList";
import {LobbyDetails} from "./components/LobbyDetails";
import {Match} from "./components/Match.tsx";
import {MatchResults} from "./components/MatchResults.tsx";

const router = createBrowserRouter([
    {
        path: "/",
        element: <Home/>,
        children: [
            {
                index: true,
                element: <LobbiesList/>
            },
            {
                path: "login",
                element: <Login/>
            },
            {
                path: "register",
                element: <Register/>,
            },
            {
                path: "profile",
                element: (
                    <ProtectedRoute>
                        <UserProfile/>
                    </ProtectedRoute>
                ),
            },
            {
                path: "create-lobby",
                element: (
                    <ProtectedRoute>
                        <CreateLobby/>
                    </ProtectedRoute>
                ),
            },
            {
                path: "lobbies/:lobbyId",
                element:
                    <ProtectedRoute>
                        <LobbyDetails/>
                    </ProtectedRoute>
            },
            {
                path: "matches/:matchId",
                element:
                    <ProtectedRoute>
                        <Match />
                    </ProtectedRoute>
            },
            {
                path: "matches/:matchId/results",
                element:
                    <MatchResults />
            }
        ],
    },
]);

createRoot(document.getElementById("container")!).render(
    <AuthProvider>
        <RouterProvider router={router}/>
    </AuthProvider>
)


