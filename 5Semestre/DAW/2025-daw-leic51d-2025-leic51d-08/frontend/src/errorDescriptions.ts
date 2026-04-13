export const errorDescriptions: Record<string, string> = {
    "email-already-in-use":
        "There is already a user with given email address.",
    "lobby-not-found":
        "The specified lobby was not found in the system.",
    "insecure-password":
        "Password must have 8 or more characters, a special character, one uppercase character, one lowercase character and a digit.",
    "invalid-invitation":
        "Invalid invitation code. Please check the code used or request a new invitation.",
    "invalid-request-content":
        "The request content is invalid or malformed. Please check the request body and ensure all required fields are provided with valid values.",
    "user-or-password-are-invalid":
        "The provided email or password is invalid. Please check your credentials and try again.",
    "invalid-kept-dice":
        "Kept dice are not valid.",
    "invitation-already-used":
        "That invite code has already been used. Please request a new invitation.",
    "lobby-name-already-in-use":
        "There is already a lobby with this name. Please choose a different name.",
    "match-not-in-progress":
        "The match is not currently in progress.",
    "no-rounds-defined":
        "No rounds have been defined for this match.",
    "not-your-turn":
        "It's not your turn to play.",
    "not-host":
        "You must be the host to perform this action.",
    "player-already-played-this-round":
        "You've already played this round. Please wait for the next round to play again.",
    "stats-not-found":
        "User stats were not found.",
    "unknown-error":
        "An unknown error has occurred. Please try again later.",
};

export function getErrorDescription(errorType: string): string {
    return errorDescriptions[errorType] || errorType;
}