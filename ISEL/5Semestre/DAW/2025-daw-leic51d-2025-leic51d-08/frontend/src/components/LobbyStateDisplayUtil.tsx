import {Lobby as LobbyDetailsType} from "../types.ts";

export const formatLobbyState = (state: LobbyDetailsType["state"]) => {
    switch (state) {
        case "OPEN":
            return "Lobby is open";
        case "MATCH_IN_PROGRESS":
            return "A match is in progress";
        case "FINISHED":
            return "The match in this lobby has already terminated";
        default:
            return state;
    }
};