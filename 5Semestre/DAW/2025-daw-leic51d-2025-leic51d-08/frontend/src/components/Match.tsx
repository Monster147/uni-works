import {useNavigate, useParams} from "react-router";
import {api, ApiError, delay} from "../api.ts";
import {Dispatch, useCallback, useEffect, useReducer} from "react";
import {DiceFace, Lobby, Match as MatchDetails} from "../types.ts";
import {useAuth} from "../AuthContext.tsx";
import {useMatchListener} from "../hooks/useMatchListener.tsx";
import {MatchUpdateData, SSEMessage} from "../hooks/SSEUtils.tsx";
import {formatHand, formatScore} from "./MatchDisplayUtils.tsx";

type MatchState = {
    match: MatchDetails | null
    lobby: Lobby | null
    keptDice: boolean[]
    isHost: boolean
    isLoading: boolean
    error: string | null
}

type MatchAction =
    | { type: "match-started"; match: MatchDetails }
    | { type: "new-round"; match: MatchDetails }
    | { type: "played-turn"; match: MatchDetails }
    | { type: "passed-turn"; match: MatchDetails }
    | { type: "round-ended"; match: MatchDetails }
    | { type: "set-lobby"; lobby: Lobby }
    | { type: "set-host"; isHost: boolean }
    | { type: "update-kept-dice"; keptDice: boolean[] }
    | { type: "set-loading"; isLoading: boolean }
    | { type: "error"; message: string };


function matchReduce(state: MatchState, action: MatchAction): MatchState {
    switch(action.type) {
        case "match-started":
            return {...state, match: action.match };
        case "new-round":
            return {...state, match: action.match, isLoading: false, keptDice: [true, true, true, true, true]};
        case "played-turn":
            return {...state, match: action.match, isLoading: false, keptDice: [true, true, true, true, true]};
        case "passed-turn":
            return {...state, match: action.match, isLoading: false};
        case "round-ended":
            return {...state, match: action.match, isLoading: false};
        case "set-lobby":
            return { ...state, lobby: action.lobby};
        case "set-host":
            return { ...state, isHost: action.isHost };
        case "update-kept-dice":
            return { ...state, keptDice: action.keptDice};
        case "set-loading":
            return { ...state, isLoading: action.isLoading };
        case "error":
            return { ...state, error: action.message, isLoading: false };
        default:
            return state;
    }
}

const initState: MatchState = {
    match: null,
    lobby: null,
    keptDice: [],
    isHost: false,
    isLoading: true,
    error: null,
};

async function startFirstRound(matchId: number, dispatch: Dispatch<MatchAction>) {
    try {
        await api.startNextRound(matchId);
        const updatedMatch = await api.getMatchDetails(matchId);
        dispatch({ type: "new-round", match: updatedMatch });
    } catch (error) {
        if (error instanceof ApiError) {
            dispatch({ type: "error", message: error.message });
        } else {
            dispatch({ type: "error", message: "Failed to start first round" });
        }
    }
}

async function endCurrentRound(match: MatchDetails, dispatch: Dispatch<MatchAction>) {
    const allTurnsPlayed =
        match.rounds[match.current_round - 1].turns.length > 0 &&
        match.rounds[match.current_round - 1].turns.every(t => t.state === "COMPLETED");

    if (allTurnsPlayed) {
        try {
            await api.endCurrentRound(Number(match.match_id));
        } catch (e) {
            if (e instanceof ApiError) dispatch({ type: "error", message: e.message });
            else dispatch({ type: "error", message: "Failed to end round" });
        }
    }
}



export function Match() {
    const { matchId } = useParams<{ matchId: string }>();
    const { user } = useAuth();
    const [state, dispatch] = useReducer(matchReduce, initState)
    const navigate = useNavigate();

    useEffect(() => {
        console.log("Match state updated:", state);
    }, [state]);

    useEffect(() => {
        if (!matchId) return;
        async function fetchData() {
            dispatch({ type: "set-loading", isLoading: true });
            try {
                const matchData = await api.getMatchDetails(Number(matchId));
                dispatch({ type: "match-started", match: matchData });
                const lobbyData = await api.getLobbyDetails(matchData.lobby_id);
                dispatch({ type: "set-lobby", lobby: lobbyData });

                const host = lobbyData.host.id === user?.id
                dispatch({ type: "set-host", isHost: host});

                if (host) await startFirstRound(Number(matchId), dispatch);
                else dispatch({ type: "set-loading", isLoading: false });

            } catch (e) {
                if (e instanceof ApiError) dispatch({ type: "error", message: e.message });
                else dispatch({ type: "error", message: "Failed to load match or lobby" });
            }
        }
        fetchData();
    }, [matchId]);

    const handleMatchListener = useCallback(async (message: SSEMessage) => {
        const { match } = message.data as MatchUpdateData;
        const { action } = message;

        switch (action) {
            case "PlayedTurn":
                dispatch({ type: "played-turn", match });
                if(state.isHost) await endCurrentRound(match, dispatch);
                break
            case "PassedTurn":
                dispatch({ type: "passed-turn", match });
                if(state.isHost) await endCurrentRound(match, dispatch);
                break;
            case "NewRound":
                dispatch({ type: "new-round", match });
                break;
            case "RoundEnded":
                dispatch({ type: "round-ended", match });
                if(state.isHost) {
                    await delay(1000);
                    try {
                        await api.startNextRound(Number(match.match_id));
                    } catch (e) {
                        if (e instanceof ApiError) dispatch({ type: "error", message: e.message });
                        else dispatch({ type: "error", message: "Failed to start next round" });
                    }
                }
                break;
            case "MatchEnded":
                navigate(`/matches/${matchId}/results`);
                break;
            default:
                break;
        }
    }, [dispatch, navigate, state.isHost]);

    useMatchListener(matchId, handleMatchListener);

    const toggleDice = (index: number) => {
        const newKept = [...state.keptDice];
        newKept[index] = !newKept[index];
        dispatch({ type: "update-kept-dice", keptDice: newKept });
    };

    //handle turn
    const handleRoll = async () => {
        if (!state.match) return;
        try {
            const diceToRoll = rollCount > 0 ? state.keptDice : [];
            await api.playTurn(Number(matchId), diceToRoll);
            const matchData = await api.getMatchDetails(Number(matchId));
            dispatch({ type: "played-turn", match: matchData });
        } catch (error) {
            if (error instanceof ApiError) dispatch({ type: "error", message: error.message });
            else dispatch({ type: "error", message: "Failed to roll" });
        }
    };

    if (state.error) {
        return (
            <div className="matc-error">
                {state.error || "Match not found"}
            </div>
        );
    }

    //handle skip turn
    const handleSkip = async () => {
        if (!matchId) return;
        try {
            await api.passTurn(Number(matchId));
            const matchData = await api.getMatchDetails(Number(matchId));
            dispatch({ type: "passed-turn", match: matchData });
        } catch (error) {
            if (error instanceof ApiError) {
                dispatch({ type: "error", message: error.message });
            } else {
                dispatch({ type: "error", message: "Failed to skip" });
            }
        }
    }


    const isMatchReady =
        state.match &&
        state.match.rounds.length > 0 &&
        state.match.current_round > 0 &&
        state.match.rounds[state.match.current_round - 1]?.turns;

    if (state.isLoading || !isMatchReady) return <div>Loading match...</div>;
    if (!state.match) return <div>No match found</div>;

    const match = state.match;
    const currentRound = match.rounds[match.current_round - 1];
    const currentPlayer = currentRound?.current_player;

    const players = match.players;
    const keptDice = state.keptDice;

    const myTurn = currentRound.turns.find(t => t.player.id === user?.id);
    const currentTurn = currentRound.turns.find(t => t.player.id === currentPlayer?.id);

    const isMyTurn = currentPlayer?.id === user?.id;
    const turnToShow = isMyTurn ? currentTurn : myTurn;

    const handFaces: DiceFace[] = turnToShow?.hand
        ? turnToShow.hand.split(", ").map(f => f.trim() as DiceFace)
        : [];

    const rollCount = currentTurn?.roll_count ?? 0;

    const playerTurns = currentRound?.turns ?? [];

    return (
        <div className="match-container">
            <h2>
                Round: {currentRound.round_number}/{match.rounds.length}
            </h2>

            <div className="players-table">
                {players.map(player => {
                    const turn = playerTurns.find(t => t.player.id === player.id);
                    return (
                        <div key={player.id} className="player-row">
                            <span className="player-name">{player.name}</span>
                            <span className="player-hand">{formatHand(turn?.hand)}</span>
                            <span className="player-score">{formatScore(turn?.score)}</span>
                        </div>
                    );
                })}
            </div>

            <p className="current-player">Current Player: {currentPlayer?.name}</p>

            {turnToShow && (
                <>
                    <div className="dice-container">
                        {handFaces.map((value, index) => (
                            <img
                                key={index}
                                src={`/dice_${value.toLowerCase()}.png`}
                                className={!keptDice[index] ? "dice discard" : "dice"}
                                onClick={() => toggleDice(index)}
                            />
                        ))}
                    </div>

                    <p>Hand score: {formatScore(turnToShow?.score)}</p>
                </>
            )}

            {!isMyTurn ? (
                <p>Waiting for {currentPlayer?.name} to play...</p>
            ) : (
                <div>
                    <button className="roll" onClick={handleRoll} disabled={(rollCount > 0 && !keptDice.some(kept => !kept)) || rollCount >= 3}>
                        {rollCount === 0 ? "Roll" : "Re-roll"}
                    </button>

                    <button className="skip" onClick={handleSkip} disabled={rollCount === 0}>
                        Skip
                    </button>

                    <p>Roll count: {rollCount}</p>
                </div>
            )}
        </div>
    );
}