import React, {useReducer} from "react";
import {useNavigate} from "react-router";
import {useAuth} from "../AuthContext";
import {api, ApiError} from "../api";

type RegisterState = {
    name: string;
    email: string;
    password: string;
    inviteCode: string;
    error: string | undefined;
    stage: "editing" | "posting" | "succeed" | "failed";
};

type RegisterAction =
    | { type: "input-change"; name: string; email: string; password: string; inviteCode: string }
    | { type: "post" }
    | { type: "success" }
    | { type: "error"; message: string };

function reduce(state: RegisterState, action: RegisterAction): RegisterState {
    switch (action.type) {
        case "input-change":
            return {
                ...state,
                name: action.name,
                email: action.email,
                password: action.password,
                inviteCode: action.inviteCode,
            };
        case "post":
            return {
                ...state,
                stage: "posting",
                error: undefined,
            };
        case "success":
            return {
                name: "",
                email: "",
                password: "",
                inviteCode: "",
                error: undefined,
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

const initState: RegisterState = {
    name: "",
    email: "",
    password: "",
    inviteCode: "",
    error: undefined,
    stage: "editing",
};

export function Register() {
    const [state, dispatch] = useReducer(reduce, initState)
    const navigate = useNavigate();
    const {login} = useAuth();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        dispatch({type: "post"});

        try {
            await api.createUser({
                name: state.name,
                email: state.email,
                password: state.password,
                invite_code: state.inviteCode
            });
        } catch (e) {
            if (e instanceof ApiError) {
                dispatch({type: "error", message: e.message})
            } else {
                dispatch({
                    type: "error",
                    message: "An error occurred during registration"
                })
            }
        }

        try {
            const response = await api.createToken({
                email: state.email,
                password: state.password
            });
            await login(response.token);
            dispatch({type: "success"});
            navigate("/");
        } catch (e) {
            if (e instanceof ApiError) {
                dispatch({type: "error", message: e.message})
            } else {
                dispatch({
                    type: "error",
                    message: "An error occurred during login"
                })
            }
        }
    }

    return (
        <div className="auth-container">
            <h2>Register</h2>
            <form className="auth-form" onSubmit={handleSubmit}>
                <div className="form-field">
                    <label>
                        Name
                        <input
                            type="text"
                            value={state.name}
                            onChange={(e) =>
                                dispatch({
                                    type: "input-change",
                                    name: e.target.value,
                                    email: state.email,
                                    password: state.password,
                                    inviteCode: state.inviteCode,
                                })
                            }
                            required
                        />
                    </label>
                </div>
                <div className="form-field">
                    <label>
                        Email
                        <input
                            type="email"
                            value={state.email}
                            onChange={(e) =>
                                dispatch({
                                    type: "input-change",
                                    name: state.name,
                                    email: e.target.value,
                                    password: state.password,
                                    inviteCode: state.inviteCode,
                                })
                            }
                            required
                        />
                    </label>
                </div>
                <div className="form-field">
                    <label>
                        Password
                        <input
                            type="password"
                            value={state.password}
                            onChange={(e) =>
                                dispatch({
                                    type: "input-change",
                                    name: state.name,
                                    email: state.email,
                                    password: e.target.value,
                                    inviteCode: state.inviteCode,
                                })
                            }
                            required
                        />
                    </label>
                </div>
                <div className="form-field">
                    <label>
                        Invite Code
                        <input
                            type="text"
                            value={state.inviteCode}
                            onChange={(e) =>
                                dispatch({
                                    type: "input-change",
                                    name: state.name,
                                    email: state.email,
                                    password: state.password,
                                    inviteCode: e.target.value,
                                })
                            }
                            required
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
                        ? "Registering..."
                        : "Register"}
                </button>
            </form>
        </div>
    );
}