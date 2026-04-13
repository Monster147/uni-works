package pt.isel.pdm.pokerDice.domain

data class Lobby(
    val id: Int,
    val name: String,
    val description: String,
    val maxPlayers: Int,
    val host: User,
    val rounds: Int,
    val players: List<User>,
    val ante: Int = 1,
    val state: LobbyState = LobbyState.OPEN,
    val matchId: Int = 0,
    val autoStartAt: Long? = null
) {
    init {
        require(name.isNotBlank()) { "Name must not be blank" }
        require(description.isNotBlank()) { "Description must not be blank" }
        require(maxPlayers in 2..10) { "Max players must be between 2 and 10" }
    }
}
