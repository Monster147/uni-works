// SSE Lobby Message types
import {Lobby, Match, User} from "../types.ts";

type LobbyUpdateAction = "UserJoined" | "UserLeft" | "LobbyDeleted" | "MatchStarted";

// SSE Lobby Message data
export interface LobbyUpdateData {
    lobby: Lobby;
    changedUser: User;
    matchId?: number;
}

// SSE Match Message types
type MatchUpdateAction = "PlayedTurn" | "PassedTurn"| "NewRound" | "RoundEnded" | "MatchEnded";

// SSE Match Message data
export interface MatchUpdateData {
    match: Match
}

// SSE Message
export interface SSEMessage {
    id: number;
    data: LobbyUpdateData | MatchUpdateData;
    action: LobbyUpdateAction | MatchUpdateAction;
}