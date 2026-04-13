package pt.isel.daw

data class UserStats(
    val id: Int,
    val rounds_won: Int = 0,
    val rounds_lost: Int = 0,
    val rounds_drawn: Int = 0,
    val total_matches: Int = 0,
    val matches_won: Int = 0,
    val matches_lost: Int = 0,
    val matches_drawn: Int = 0,
    val winrate: Double = 0.0,
)
