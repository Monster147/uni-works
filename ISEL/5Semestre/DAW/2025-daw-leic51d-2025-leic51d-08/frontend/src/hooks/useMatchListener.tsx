import { useEffect } from "react";
import { SSEMessage } from "./SSEUtils.tsx"

export function useMatchListener(
    matchId: string | undefined,
    onMessage: (message: SSEMessage) => void
) {
    useEffect(() => {
        if (!matchId) return;

        const matchSource = new EventSource(`/api/matches/${matchId}/events`);

        matchSource.onmessage = (match) => {
            try {
                const message: SSEMessage = JSON.parse(match.data);
                onMessage(message);
            } catch (error) {
                console.error("Error parsing SSE message:", error);
            }
        };

        matchSource.onerror = (error) => {
            console.error("SSE Error:", error);
            matchSource.close();
        };

        // Return cleanup function
        return () => {
            matchSource.close();
        };
    }, [matchId, onMessage]);
}
