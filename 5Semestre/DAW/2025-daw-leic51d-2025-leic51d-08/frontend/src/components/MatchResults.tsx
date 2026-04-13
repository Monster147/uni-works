import {useParams} from "react-router";
import { Match as MatchDetails } from "../types.ts";
import {formatHand, formatScore} from "./MatchDisplayUtils.tsx";
import React, {useEffect} from "react";
import {api} from "../api.ts";


export function MatchResults(){
    const { matchId } = useParams<{ matchId: string }>();
    const [matchDetails, setMatchDetails] = React.useState<MatchDetails | null>(null);

    useEffect(() => {
        if(!matchId) return;
        async function fetchMatchDetails() {
            try {
                const fetchedMatchDetails = await api.getMatchDetails(Number(matchId))
                setMatchDetails(fetchedMatchDetails);
            } catch (e) {
                console.error("Failed to load match", e);
            }
        }
        fetchMatchDetails()
    }, [matchId]);

    if (!matchDetails) return <div>Loading match results...</div>;

    const playerStats: Record<string, { won: number; tied: number }> = {};
    matchDetails.players.forEach(p => (playerStats[p.name] = { won: 0, tied: 0 }));

    matchDetails.rounds.forEach(round => {
        if (round.winners.length === 1) {
            playerStats[round.winners[0].name].won += 1;
        } else if (round.winners.length > 1) {
            round.winners.forEach(w => {
                playerStats[w.name].tied += 1;
            });
        }
    });

    return (
        <div className="match-results">
            <h2>Match Results</h2>
            {matchDetails.rounds.map((round, idx) => (
                <div
                    key={idx}
                    className="match-results-round"
                >
                    <h3>Round {round.round_number}</h3>
                    <div className="players-table">
                        {round.turns.map(turn => (
                            <div key={turn.player.id} className="player-row">
                                <span className="player-name">{turn.player.name}</span>
                                <span className="player-hand">{formatHand(turn.hand)}</span>
                                <span className="player-score">{formatScore(turn.score)}</span>
                            </div>
                        ))}
                    </div>
                    <p className="round-winner">
                        {round.winners.length === 1
                            ? `Winner: ${round.winners[0].name}`
                            : `Round ended in a draw between: ${round.winners.map(w => w.name).join(", ")}`}
                    </p>
                </div>
            ))}
            <h2>Match Winner</h2>
            {matchDetails.winners.length === 1 ? (
                <p className="match-winner">
                    {matchDetails.winners[0].name} (
                    {playerStats[matchDetails.winners[0].name].won}{" "}
                    {playerStats[matchDetails.winners[0].name].won === 1 ? "round" : "rounds"} won,{" "}
                    {playerStats[matchDetails.winners[0].name].tied}{" "}
                    {playerStats[matchDetails.winners[0].name].tied === 1 ? "round" : "rounds"} tied)
                </p>
            ) : (
                <p className="match-winner">
                    Match ended in a draw between:{" "}
                    {matchDetails.winners
                        .map(
                            w =>
                                `${w.name} (${playerStats[w.name].won} rounds won, ${playerStats[w.name].tied} rounds tied)`
                        )
                        .join(", ")}
                </p>
            )}
        </div>
    );
}