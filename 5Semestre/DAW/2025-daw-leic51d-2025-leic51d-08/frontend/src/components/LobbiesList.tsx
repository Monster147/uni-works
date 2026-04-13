import {useNavigate} from "react-router";
import {Lobby} from "../types";
import {useFetch} from "../hooks/useFetch";
import "../styles/App.css";
import {useState} from "react";
import {useAuth} from "../AuthContext.tsx";
import {formatLobbyState} from "./LobbyStateDisplayUtil.tsx";

export function LobbiesList() {
    // Selecionar o tipo de fetch. all para todos os lobbies, independente se estao cheios.
    // Available para só os que ainda não estão cheios
    const { user } = useAuth();
    const navigate = useNavigate();
    const [filter, setFilter] = useState<"all" | "available">("available"); //default é o available
    const [refreshKey, setRefreshKey] = useState(0);
    const endpoint = filter === "all"
        ? "/lobbies/all"
        : "/lobbies/available";
    const state = useFetch<Lobby[]>(endpoint, undefined, [refreshKey]);

    if (state.type === "begin" || state.type === "loading") {
        return <div>Loading lobbies...</div>;
    }

    if (state.type === "error") {
        return <div>{state.error.message}</div>;
    }

    const lobbies = state.payload;

    return (
        <div>
            <h2>All Lobbies</h2>
            <div className="lobbies-controls">
                <button
                    onClick={() => setFilter("available")}
                    disabled={filter === "available"}
                >
                    Show Available
                </button>

                <button
                    onClick={() => setFilter("all")}
                    disabled={filter === "all"}
                >
                    Show All
                </button>

                <button onClick={() => setRefreshKey(k => k + 1)}>
                    Refresh
                </button>
            </div>
            {lobbies.length === 0 ? (
                <p>No lobbies found. Create one to get started!</p>
            ) : (
                <ul className="lobbies-list">
                    {lobbies.map(lobby => {
                        const userInLobby =
                            user &&
                            (lobby.state === "OPEN" ||
                                lobby.state === "MATCH_IN_PROGRESS") &&
                            lobby.players.some(p => p.id === user.id);
                        return (
                            <li key={lobby.id} className="lobby-card">
                                <div className="lobby-header">
                                    <div>
                                        <h3 className="lobby-title">
                                            {lobby.name}
                                        </h3>
                                        <p className="lobby-info">
                                            Host: {lobby.host.name} ·
                                            Players: {lobby.nof_players} ·
                                            Rounds: {lobby.nof_rounds}
                                        </p>

                                        <p className="lobby-state">
                                            {formatLobbyState(lobby.state)}
                                        </p>

                                        {userInLobby && (
                                            <p className="lobby-user-flag">
                                                You're in this lobby
                                            </p>
                                        )}
                                    </div>
                                </div>
                                <div className="lobby-actions">
                                    <button
                                        onClick={() =>
                                            navigate(`/lobbies/${lobby.id}`)
                                        }
                                    >
                                        View Details
                                    </button>

                                    {userInLobby &&
                                        lobby.state === "MATCH_IN_PROGRESS" &&
                                        lobby.match_id && (
                                            <button
                                                onClick={() =>
                                                    navigate(
                                                        `/matches/${lobby.match_id}`
                                                    )
                                                }
                                            >
                                                Go to Match
                                            </button>
                                        )}
                                </div>
                            </li>
                        );
                    })}
                </ul>
            )}
        </div>
    );
}