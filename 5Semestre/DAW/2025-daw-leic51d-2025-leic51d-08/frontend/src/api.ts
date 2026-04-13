import {LobbyInput, UserCreateTokenInputModel, UserCreateTokenOutputModel, UserInput,} from "./types";

import {getErrorDescription} from "./errorDescriptions";

const API_BASE_URL = "/api";

class ApiError extends Error {
    constructor(public status: number, message: string) {
        super(message);
    }
}

export function getAuthHeaders(): HeadersInit {
    const token = localStorage.getItem("token");
    return token ? {Authorization: `Bearer ${token}`} : {};
}

export async function fetchApi<T>(
    endpoint: string,
    options: RequestInit = {}
): Promise<T> {
    await delay(1000)
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            ...options.headers,
        },
    });
    if (!response.ok) {
        const error = await response
            .json()
            .catch(() => ({title: "Unknown error"}));
        const errorMessage = error.title
            ? getErrorDescription(error.title)
            : response.statusText;
        throw new ApiError(response.status, errorMessage);
    }

    if (response.status === 204) {
        return undefined as T;
    }
    const text = await response.text();

    if (!text) {
        return undefined as T;
    }

    return JSON.parse(text);

}

export const api = {
    async createUser(input: UserInput): Promise<string> {
        const response = await fetch(`${API_BASE_URL}/users`, {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(input),
        });

        if (!response.ok) {
            const error = await response
                .json()
                .catch(() => ({title: "Unknown error"}));
            const errorMessage = error.title
                ? getErrorDescription(error.title)
                : response.statusText;
            throw new ApiError(response.status, errorMessage);
        }

        return response.headers.get("Location") || "";
    },

    async createToken(
        input: UserCreateTokenInputModel
    ): Promise<UserCreateTokenOutputModel> {
        return fetchApi<UserCreateTokenOutputModel>(`/users/token`, {
            method: "POST",
            body: JSON.stringify(input),
        });
    },

    async logout(): Promise<void> {
        return fetchApi<void>(`/logout`, {
            method: "POST",
            headers: getAuthHeaders(),
        });
    },

    async getUserStats(): Promise<any> {
        return fetchApi<any>(`/me/stats`, {
            headers: getAuthHeaders(),
        });
    },

    async createInvitation(): Promise<any> {
        return fetchApi<any>(`/invitations`, {
            method: "POST",
            headers: getAuthHeaders(),
        });
    },

    //Lobby
    async createLobby(input: LobbyInput): Promise<any> {
        return fetchApi<any>(`/lobbies`, {
            method: "POST",
            headers: getAuthHeaders(),
            body: JSON.stringify(input),
        });
    },

    async getLobbyDetails(lobbyId: number): Promise<any> {
        return fetchApi<any>(`/lobbies/${lobbyId}`)
    },

    async joinLobby(lobbyId: number): Promise<void> {
        return fetchApi<void>(`/lobbies/${lobbyId}/join`, {
            method: "POST",
            headers: getAuthHeaders(),
        })
    },

    async leaveLobby(lobbyId: number): Promise<void> {
        return fetchApi<void>(`/lobbies/${lobbyId}/leave`, {
            method: "POST",
            headers: getAuthHeaders(),
        })
    },

    async startMatch(lobbyId: number): Promise<void> {
        return fetchApi<void>(`/lobbies/${lobbyId}/start`, {
            method: "POST",
            headers: getAuthHeaders(),
        })
    },

    async startNextRound(matchId: number): Promise<void> {
        return fetchApi<void>(`/matches/${matchId}/rounds/start`, {
            method: "POST",
        })
    },

    async endCurrentRound(matchId: number): Promise<void> {
        return fetchApi<void>(`/matches/${matchId}/finish`, {
            method: "POST",
            headers: getAuthHeaders(),
        })
    },

    async getMatchDetails(matchId: number): Promise<any> {
        return fetchApi<any>(`/matches/${matchId}` )
    },

    async playTurn(matchId: number, keptDice: boolean[]): Promise<any> {
        const body = keptDice && keptDice.length > 0 ? JSON.stringify({kept_dice: keptDice}) : JSON.stringify({});
        return fetchApi<void>(`/matches/${matchId}/turns/play`, {
            method: "POST",
            headers: getAuthHeaders(),
            body: body
        })
    },

    async passTurn(matchId: number) : Promise<any> {
        return fetchApi<void>(`/matches/${matchId}/turns/pass`, {
            method: "POST",
            headers: getAuthHeaders(),
        })
    }
}

export {ApiError};

export function delay(delayInMs: number) {
    return new Promise((resolve) => {
        setTimeout(() => resolve(undefined), delayInMs);
    });
}