export function formatHand(hand?: string) {
    if (!hand) return "—";
    return hand.split(", ").join(" · ");
}

export function formatScore(score?: string | null) {
    if (!score) return "No roll has been made yet";

    return score
        .toLowerCase()
        .split("_")
        .map(w => w[0].toUpperCase() + w.slice(1))
        .join(" ");
}