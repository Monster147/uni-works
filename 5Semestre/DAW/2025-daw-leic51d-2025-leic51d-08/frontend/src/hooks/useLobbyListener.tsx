import { useEffect } from "react";
import { SSEMessage } from "./SSEUtils.tsx"

export function useLobbyListener(
    lobbyId: string | undefined,
    onMessage: (message: SSEMessage) => void
) {
    useEffect(() => {
        if (!lobbyId) return;

        const lobbySource = new EventSource(`/api/lobbies/${lobbyId}/events`);

        lobbySource.onmessage = (lobby) => {
            try {
                const message: SSEMessage = JSON.parse(lobby.data);
                onMessage(message);
            } catch (error) {
                console.error("Error parsing SSE message:", error);
            }
        };

        lobbySource.onerror = (error) => {
            console.error("SSE Error:", error);
            lobbySource.close();
        };

        // Return cleanup function
        return () => {
            lobbySource.close();
        };
    }, [lobbyId, onMessage]);
}
