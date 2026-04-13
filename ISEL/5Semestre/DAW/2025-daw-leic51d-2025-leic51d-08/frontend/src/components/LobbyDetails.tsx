import {Lobby as LobbyDetailsType, User} from "../types.ts";
import React, {useCallback, useEffect, useReducer, useState} from "react";
import {api, ApiError} from "../api.ts";
import {useNavigate, useParams} from "react-router";
import {useLobbyListener} from "../hooks/useLobbyListener.tsx";
import {SSEMessage, LobbyUpdateData} from "../hooks/SSEUtils.tsx";
import {useAuth} from "../AuthContext.tsx";
import {formatLobbyState} from "./LobbyStateDisplayUtil.tsx";

//State type
type LobbyDetailsState = {
    lobbyDetails: LobbyDetailsType | null;
    isLoading: boolean;
    msRemaining: number;
    error: string | null;
};

//Action type
type LobbyDetailsAction =
    | { type: "load"; isLoading: boolean }
    | { type: "set-lobby-details"; lobbyDetails: LobbyDetailsType }
    | {
    type: "user-joined";
    lobbyId: number;
    user: User;
    playerId: number | null;
    changeTimer: number | null;
}
    | {
    type: "user-left";
    lobbyId: number;
    userId: number;
    playerId: number;
}
    | { type: "error"; message: string | null }

//reduce function
function lobbyDetailsReduce(state: LobbyDetailsState, action: LobbyDetailsAction): LobbyDetailsState {
    switch (action.type) {
        case "load":
            return {...state, isLoading: action.isLoading};
        case "set-lobby-details":
            return {
                ...state,
                lobbyDetails: action.lobbyDetails,
                isLoading: false,
                error: null,
            }
        case "user-joined":
            return handleUserJoined(state, action);
        case "user-left":
            return handleUserLeft(state, action);
        case "error":
            return {...state, error: action.message, isLoading: false};
        default:
            return state;
    }
}

function handleUserJoined(state: LobbyDetailsState, action: Extract<LobbyDetailsAction, { type: "user-joined" }>): LobbyDetailsState {
    if (!state.lobbyDetails) return state;

    //if lobby not open, can add players
    if (state.lobbyDetails.state !== "OPEN") return state;

    let oldNOfPlayers = state.lobbyDetails.nof_players;
    let parts = oldNOfPlayers.split('/');
    parts[0] = String(Number(parts[0]) + 1);
    oldNOfPlayers = parts.join('/')
    const newState = {
        ...state,
        lobbyDetails: {
            ...state.lobbyDetails,
            nof_players: oldNOfPlayers,
            players: [...state.lobbyDetails.players, action.user],
            auto_start_at: action.changeTimer
        },
        msRemaining: state.msRemaining,
    }
    console.log("newState", newState);
    return newState;
}

function handleUserLeft(state: LobbyDetailsState, action: Extract<LobbyDetailsAction, { type: "user-left" }>): LobbyDetailsState {
    if (!state.lobbyDetails) return state;

    // Remove player
    const updatedPlayers = state.lobbyDetails.players.filter(p => p.id !== action.userId);
    let oldNOfPlayers = state.lobbyDetails.nof_players;
    let parts = oldNOfPlayers.split('/');
    parts[0] = String(Number(parts[0]) - 1);
    oldNOfPlayers = parts.join('/')
    return {
        ...state,
        lobbyDetails: {
            ...state.lobbyDetails,
            nof_players: oldNOfPlayers,
            players: updatedPlayers,
            auto_start_at: null
        },
    };
}

const initState: LobbyDetailsState = {
    lobbyDetails: null,
    isLoading: true,
    msRemaining: 0,
    error: null,
}

//Data loading function
async function loadLobbyData(lobbyId: string, dispatch: React.Dispatch<LobbyDetailsAction>) {
    dispatch({ type: "load", isLoading: true});
    try {
        const data: LobbyDetailsType = await api.getLobbyDetails(Number(lobbyId));
        dispatch({ type: "set-lobby-details", lobbyDetails: data });
    } catch (error) {
        if (error instanceof ApiError) {
            dispatch({ type: "error", message: error.message });
        } else {
            dispatch({ type: "error", message: "Failed to load lobby details" });
        }
    }
}

export function LobbyDetails() {
    const { lobbyId } = useParams<{ lobbyId: string }>();
    const [state, dispatch] = useReducer(lobbyDetailsReduce, initState);
    const { user } = useAuth()
    const navigate = useNavigate();
    const [isLeaving, setIsLeaving] = useState(false);
    const [remainingMs, setRemainingMs] = useState<number | null>(null);

    //load lobby data
    useEffect(() => {
        if (!lobbyId) return;
        loadLobbyData(lobbyId, dispatch);
    }, [lobbyId]);

    useEffect(() => {
        if (!state.lobbyDetails || !lobbyId) {
            setRemainingMs(null);
            return;
        }

        const { players, state: lobbyState, auto_start_at } = state.lobbyDetails;

        // Conditions NOT met -> reset timer
        if (!auto_start_at ||lobbyState !== "OPEN" || players.length < 2) {
            setRemainingMs(null);
            return;
        }

        const tid = setTimeout(() => {
            const diff = auto_start_at - Date.now();
            setRemainingMs(diff > 0 ? diff : 0);
        }, 1000);

        return () => clearTimeout(tid);
    }, [state.lobbyDetails, remainingMs, user]);

    useEffect(() => {
        if (remainingMs === 0) {
            handleStartMatch();
        }
    }, [remainingMs]);

    const handleLobbyUpdate = useCallback((message: SSEMessage) => {
        const { lobby, changedUser, matchId } = message.data as LobbyUpdateData;
        const { action } = message
        console.log("Lobby action received:", action);
        switch (action) {
            case ("UserJoined"):
                dispatch({
                    type: "user-joined",
                    lobbyId: lobby.id,
                    user: changedUser,
                    playerId: changedUser.id,
                    changeTimer: lobby.auto_start_at
                });
                if (lobby.auto_start_at) setRemainingMs(lobby.auto_start_at - Date.now())
                break;
            case ("UserLeft"):
                dispatch({
                    type: "user-left",
                    lobbyId: lobby.id,
                    userId: changedUser.id,
                    playerId: changedUser.id,
                });
                break;
            case ("LobbyDeleted"):
                navigate("/");
                break;
            case("MatchStarted"):
                if(!matchId){
                    console.error("MatchStarted event without matchId");
                    return;
                }
                navigate("/matches/" + matchId);
                break;
        }
    }, [dispatch, navigate])

    //setup SSE
    useLobbyListener(lobbyId, handleLobbyUpdate);

    if (state.isLoading && !isLeaving) {
        return <div className="lobby-details-loading">Loading lobby...</div>;
    }

    if (state.error || !state.lobbyDetails) {
        return (
            <div className="lobby-details-error">
                {state.error || "Lobby not found"}
            </div>
        );
    }
    function formatTime(ms: number) {
        const totalSeconds = Math.floor(ms / 1000);
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = totalSeconds % 60;
        return `${minutes}:${seconds.toString().padStart(2, "0")}`;
    }

    const { name, players, description, nof_players, nof_rounds, host, state: lobbyState, ante, match_id } = state.lobbyDetails;

    let maxPlayers = nof_players
    while(maxPlayers.charAt(0) != "/") {
        maxPlayers = maxPlayers.substring(1);
    }
    maxPlayers = maxPlayers.substring(1);
    const max = parseInt(maxPlayers) || 0;
    const isFull = players.length >= max;
    const canJoin = lobbyState === "OPEN" && !isFull;

    let isUserJoined = false;
    for(let i = 0; i < players.length; i++) {
        if(players[i].email===user?.email) {
            isUserJoined = true;
            break;
        }
    }

    const isHost = user?.email === host.email;

    const handleJoin = async () => {
        if (!lobbyId) return;
        dispatch({ type: "load", isLoading: true});
        try {
            await api.joinLobby(Number(lobbyId));
            await loadLobbyData(lobbyId, dispatch);
        } catch (error) {
            if (error instanceof ApiError) {
                dispatch({ type: "error", message: error.message });
            } else {
                dispatch({ type: "error", message: "Failed to join lobby" });
            }
        }
    };

    const handleLeave = async () => {
        if (!lobbyId) return;
        dispatch({ type: "load", isLoading: true});
        try {
            await api.leaveLobby(Number(lobbyId));
            console.log("Left lobby successfully");
            navigate("/");
        } catch (error) {
            if (error instanceof ApiError) {
                dispatch({ type: "error", message: error.message });
            } else {
                dispatch({ type: "error", message: "Failed to leave lobby" });
            }
        }
    }

    const handleStartMatch = async () => {
        if (!lobbyId || !isHost) return;
        dispatch({ type: "load", isLoading: true});
        try {
            setIsLeaving(true);
            const res = await api.startMatch(Number(lobbyId));
            navigate("/matches/" + res);
        } catch (error) {
            if (error instanceof ApiError) {
                dispatch({ type: "error", message: error.message });
            } else {
                dispatch({ type: "error", message: "Failed to start match" });
            }
        }
    }

    const hasMatch = match_id > 0;

    return (
        <div className="lobby-details-container">
            <div className="lobby-details-header">
                <h2 className="lobby-title">Lobby: {name}</h2>
                <p className="lobby-description">{description}</p>
                <div className="lobby-stats">
                    <p><strong>Players:</strong> {nof_players}</p>
                    <p><strong>Rounds:</strong> {nof_rounds}</p>
                    <p><strong>Ante:</strong> {ante}</p>
                </div>
                <div className="lobby-details-meta">
                    <p><strong>Host:</strong> {host.name}</p>
                    <p><strong>State:</strong> {formatLobbyState(lobbyState)}</p>
                    <p>
                        <strong>Players list:</strong>{" "}
                        {players.length > 0
                            ? players.map(p => p.name).join(", ")
                            : "none"}
                    </p>
                </div>
                {remainingMs !== null && (
                    <p className="lobby-timer">
                        Match starts automatically in{" "}
                        <strong>{formatTime(remainingMs)}</strong>
                    </p>
                )}
                <div className="lobby-actions">
                    {!isUserJoined && lobbyState === "OPEN" && (
                        <button onClick={handleJoin} disabled={!canJoin}>
                            {isFull ? "Lobby Full" : "Join Lobby"}
                        </button>
                    )}
                    {isHost && lobbyState === "OPEN" && (
                        <button
                            onClick={handleStartMatch}
                            disabled={players.length < 2}
                        >
                            Start Match
                        </button>
                    )}
                    {isUserJoined && lobbyState === "OPEN" && (
                        <button onClick={handleLeave}>
                            Leave Lobby
                        </button>
                    )}
                    {hasMatch && lobbyState === "FINISHED" && (
                        <button
                            onClick={() =>
                                navigate(`/matches/${match_id}/results`)
                            }
                        >
                            View Match Results
                        </button>
                    )}
                </div>
            </div>
        </div>
    );

}