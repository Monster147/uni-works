package pt.isel.daw.repo

import pt.isel.daw.Lobby
import pt.isel.daw.User

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

    fun updateAutoStartTime(
        lobbyID: Int,
        autoStartAt: Long?,
    )
}
