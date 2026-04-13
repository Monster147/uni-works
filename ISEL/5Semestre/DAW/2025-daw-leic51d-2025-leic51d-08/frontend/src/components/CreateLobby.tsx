import React, {useReducer} from "react";
import {useNavigate} from "react-router";
import {api, ApiError} from "../api";
import {useAuth} from "../AuthContext";

const MIN_VALUE_PLAYERS = 2
const MAX_VALUE_PLAYERS = 10
const MIN_ROUNDS = 1
const MIN_ANTE = 1

type LobbyState = {
    name: string;
    description: string;
    maxPlayers: number;
    rounds: number;
    ante: number;
    error: string | undefined;
    stage: "editing" | "posting" | "succeed" | "failed";
};

type LobbyAction =
    | { type: "input-change"; field: keyof LobbyState; value: string | number }
    | { type: "post" }
    | { type: "success" }
    | { type: "error"; message: string };

function reduce(state: LobbyState, action: LobbyAction): LobbyState {
    switch (action.type) {
        case "input-change":
            return {
                ...state,
                [action.field]: action.value
            };
        case "post":
            return {
                ...state,
                stage: "posting",
                error: undefined,
            };
        case "success":
            return {
                ...state,
                stage: "succeed",
            };
        case "error":
            return {
                ...state,
                stage: "failed",
                error: action.message,
            };
        default:
            return state;
    }
}

function handleNumberInput(
    e: React.ChangeEvent<HTMLInputElement>,
    min: number,
    max: number | null,
    dispatch: any,
    field: keyof LobbyState
) {
    const raw = e.target.value;

    if (raw === "") return;          // impede ficar vazio
    const num = Number(raw);

    if (Number.isNaN(num)) return;   // impede strings

    let clamped = Math.max(min, num);
    // aplica max apenas se existir
    if (max !== null) {
        clamped = Math.min(max, clamped);
    }

    dispatch({ type: "input-change", field, value: clamped });
}


const initialState: LobbyState = {
    name: "",
    description: "",
    maxPlayers: MIN_VALUE_PLAYERS,
    rounds: MIN_ROUNDS,
    ante: MIN_ANTE,
    error: undefined,
    stage: "editing",
};

export function CreateLobby() {
    const [state, dispatch] = useReducer(reduce, initialState);
    const {user} = useAuth();
    const navigate = useNavigate();

    if (!user) {
        navigate("/login");
        return null;
    }

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        dispatch({type: "post"});

        try {
            const lobbyId = await api.createLobby({
                name: state.name,
                description: state.description,
                max_players: state.maxPlayers,
                rounds: state.rounds,
                ante: state.ante
            });
            dispatch({type: "success"});
            navigate(`/lobbies/${lobbyId}`);
        } catch (err) {
            if (err instanceof ApiError) {
                dispatch({type: "error", message: err.message});
            } else {
                dispatch({
                    type: "error",
                    message: "An error occurred while creating the lobby",
                });
            }
        }
    };

    return (
        <div className="create-lobby-container">
            <h2>Create Lobby</h2>
            <form className="create-lobby-form" onSubmit={handleSubmit}>
                <div className="form-field">
                    <label>
                        Name
                        <input
                            type="text"
                            value={state.name}
                            onChange={(e) =>
                                dispatch({
                                    type: "input-change",
                                    field: "name",
                                    value: e.target.value,
                                })
                            }
                            required
                        />
                    </label>
                </div>
                <div className="form-field">
                    <label>
                        Description
                        <textarea
                            rows={4}
                            value={state.description}
                            onChange={(e) =>
                                dispatch({
                                    type: "input-change",
                                    field: "description",
                                    value: e.target.value,
                                })
                            }
                            required
                        />
                    </label>
                </div>
                <div className="form-field">
                    <label>
                        Max Players
                        <input
                            type="number"
                            min={MIN_VALUE_PLAYERS}
                            max={MAX_VALUE_PLAYERS}
                            value={state.maxPlayers}
                            onChange={(e) =>
                                handleNumberInput(
                                    e,
                                    MIN_VALUE_PLAYERS,
                                    MAX_VALUE_PLAYERS,
                                    dispatch,
                                    "maxPlayers"
                                )
                            }
                        />
                    </label>
                </div>
                <div className="form-field">
                    <label>
                        Rounds
                        <input
                            type="number"
                            min={MIN_ROUNDS}
                            value={state.rounds}
                            onChange={(e) =>
                                handleNumberInput(
                                    e,
                                    MIN_ROUNDS,
                                    null,
                                    dispatch,
                                    "rounds"
                                )
                            }
                        />
                    </label>
                </div>
                <div className="form-field">
                    <label>
                        Ante
                        <input
                            type="number"
                            min={MIN_ANTE}
                            value={state.ante}
                            onChange={(e) =>
                                handleNumberInput(
                                    e,
                                    MIN_ANTE,
                                    null,
                                    dispatch,
                                    "ante"
                                )
                            }
                        />
                    </label>
                </div>
                {state.error && (
                    <div className="form-error">{state.error}</div>
                )}
                <button
                    className="submit-button"
                    type="submit"
                    disabled={state.stage === "posting"}
                >
                    {state.stage === "posting"
                        ? "Creating..."
                        : "Create Lobby"}
                </button>
            </form>
        </div>
    );
}