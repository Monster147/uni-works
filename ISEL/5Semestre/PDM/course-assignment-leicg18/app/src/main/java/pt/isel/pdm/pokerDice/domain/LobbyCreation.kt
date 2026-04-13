package pt.isel.pdm.pokerDice.domain

data class LobbyCreation(
    val name: String,
    val description: String,
    val maxPlayers: Int,
    val nOfRounds: Int,
    val ante: Int,
) {
    init {
        require(
            isValidLobbyCreationData(
                name,
                description,
                maxPlayers,
                nOfRounds,
                ante
            )
        ) {
            "Invalid lobby creation data: $this"
        }
    }
}

fun isValidLobbyCreationData(
    name: String,
    description: String,
    maxPlayers: Int?,
    nOfRounds: Int?,
    ante: Int?
): Boolean =
    name.isNotBlank() &&
            description.isNotBlank() &&
            maxPlayers in 2..6 &&
            nOfRounds in 1..60 &&
            ante?.let { it >= 0 } == true