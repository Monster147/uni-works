import {useAuth} from "../AuthContext";
import "../styles/App.css";
import React, {useEffect, useReducer, useState} from "react";
import {api, ApiError} from "../api.ts";

type CodeState = {
    code?: string;
    stage:"idle" | "posting" | "succeed" | "failed"
};

type CodeAction =
    | { type: "post" }
    | { type: "success"; code?: string}
    | { type: "error"; message: string };

function reduce(state: CodeState, action: CodeAction): CodeState {
    switch(state.stage) {
        case "idle":
            switch (action.type) {
                case "post":
                    return {
                        ...state,
                        stage: "posting"
                    };
                default:
                    return state;
            }
        case "posting":
            switch (action.type) {
                case "success":
                    return {
                        ...state,
                        stage: "succeed",
                        code: action.code};
                case "error":
                    return {
                        ...state,
                        stage: "failed"};
                default:
                    return state;
            }
        case "succeed":
            if (action.type === "post") {
                return {
                    ...state,
                    stage: "posting",
                };
            }
            return state;
        case "failed":
            if (action.type === "post") {
                return {
                    ...state,
                    stage: "posting",
                };
            }
            return state;
        default:
            return state;
    }
}

const initState: CodeState = {code: "", stage: "idle"}

export function UserProfile() {
    const {user} = useAuth();
    const [state, dispatch] = useReducer(reduce, initState)
    const [userStats, setUserStats] = useState<any>(null);
    const [loadingStats, setLoadingStats] = useState(true);
    const [errorStats, setErrorStats] = useState<string | null>(null);

    useEffect(() => {
        if (!user) return;
        api.getUserStats()
            .then((data) => setUserStats(data))
            .catch((err) => setErrorStats(err instanceof ApiError ? err.message : "Failed to load stats"))
            .finally(() => setLoadingStats(false));
    }, [user]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        dispatch({type: "post"});

        try {
            //console.log(code)
            const res = await api.createInvitation()
            dispatch({type: "success", code: res.code})
            console.log(state)
        } catch (e) {
            if (e instanceof ApiError) {
                dispatch({type: "error", message: e.message})
            } else {
                dispatch({
                    type: "error",
                    message: "An error occurred during registration"
                })
            }
        }
    }

    if (!user) return <div>Loading...</div>;
    if (loadingStats) return <div>Loading stats...</div>;
    if (errorStats) return <div>Error loading stats: {errorStats}</div>;

    console.log(state.code)
    return (
        <div className="profile-container">
            <h2>My Profile</h2>
            <div className="profile-info">
                <p><strong>Name:</strong> {user.name}</p>
                <p><strong>Email:</strong> {user.email}</p>
                <p><strong>Balance:</strong> {user.balance}</p>
            </div>
            <div className="profile-invite">
                <button
                    className="submit-button"
                    disabled={state.stage === "posting"}
                    onClick={handleSubmit}
                >
                    {state.stage === "posting"
                        ? "Generating..."
                        : "Generate Invite Code"}
                </button>
                <div className="invite-code">
                    {state.stage === "succeed" ? state.code : "No Code"}
                </div>
            </div>
            <div className="profile-stats">
                <h3>Estatísticas</h3>
                <div className="stats-section">
                    <h4>Rondas</h4>
                    <ul>
                        <li>Rondas ganhas: {userStats.rounds_won}</li>
                        <li>Rondas perdidas: {userStats.rounds_lost}</li>
                        <li>Rondas empatadas: {userStats.rounds_drawn}</li>
                    </ul>
                </div>
                <div className="stats-section">
                    <h4>Partidas</h4>
                    <ul>
                        <li>Total de partidas: {userStats.total_matches}</li>
                        <li>Partidas ganhas: {userStats.matches_won}</li>
                        <li>Partidas perdidas: {userStats.matches_lost}</li>
                        <li>Partidas empatadas: {userStats.matches_drawn}</li>
                    </ul>
                </div>

                <div className="stats-section">
                    <h4>Win Rate</h4>
                    <div
                        className="winrate-circle"
                        style={{
                            background: `conic-gradient(#4caf50 ${
                                userStats.winrate * 100
                            }%, #ddd 0)`
                        }}
                    >
                        {(userStats.winrate * 100).toFixed(1)}%
                    </div>
                    <p className="winrate-description">
                        O teu win rate atual é{" "}
                        {(userStats.winrate * 100).toFixed(2)}%. Baseado no
                        total de partidas jogadas.
                    </p>
                </div>
            </div>
        </div>
    );
}