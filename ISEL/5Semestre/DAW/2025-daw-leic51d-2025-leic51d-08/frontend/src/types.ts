export interface User {
    id: number;
    name: string;
    email: string;
    balance: number;
}

export interface UserStats {
    id: number;
    rounds_won: number;
    rounds_lost: number;
    rounds_drawn: number;
    total_matches: number;
    matches_won: number;
    matches_lost: number;
    matches_drawn: number;
    winrate: number;
}

export interface Lobby {
    id: number,
    name: string,
    description: string,
    nof_players: string,
    host: User,
    players: User[],
    state: "OPEN" | "MATCH_IN_PROGRESS" | "FINISHED",
    ante: number,
    nof_rounds: number,
    match_id: number,
    auto_start_at: number | null,
}

export interface Match {
    match_id: number;
    lobby_id: number;
    players: User[];
    rounds: Round[];
    state: "IN_PROGRESS" | "COMPLETED";
    winners: User[];
    current_round: number;
}

export interface Round{
    round_number: number;
    turns: Turn[];
    current_player: User;
    winners: User[];
}

export interface Turn{
    player: User;
    hand: string;
    roll_count: number;
    state: "IN_PROGRESS" | "COMPLETED";
    score: string | null;
}

export type DiceFace = "NINE" | "TEN" | "JACK" | "QUEEN" | "KING" | "ACE";

export interface Dice {
    value: number;
    selected: boolean;
}

export interface UserInput {
    name: string;
    email: string;
    password: string;
    invite_code: string;
}

export interface LobbyInput {
    name: string;
    description: string;
    max_players: number;
    rounds: number;
    ante: number;
}

export interface UserCreateTokenInputModel {
    email: string;
    password: string;
}

export interface UserCreateTokenOutputModel {
    token: string;
}
