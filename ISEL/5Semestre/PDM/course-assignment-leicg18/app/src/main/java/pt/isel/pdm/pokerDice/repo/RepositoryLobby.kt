package pt.isel.pdm.pokerDice.repo

import pt.isel.pdm.pokerDice.domain.Lobby
import pt.isel.pdm.pokerDice.domain.User

interface RepositoryLobby : Repository<Lobby> {
    fun createLobby(
        name: String,
        description: String,
        maxPlayers: Int,
        host: User,
        rounds: Int,
        ante: Int,
    ): Lobby

    fun findByName(name: String): Lobby?

    fun addPlayer(
        lobbyID: Int,
        player: User,
    ): Lobby?

    fun removePlayer(
        lobbyID: Int,
        player: User,
    ): Lobby?

    fun getAllAvailable(): List<Lobby>
}
