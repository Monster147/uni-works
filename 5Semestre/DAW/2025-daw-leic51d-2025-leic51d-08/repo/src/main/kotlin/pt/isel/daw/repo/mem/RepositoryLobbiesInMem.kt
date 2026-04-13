package pt.isel.daw.repo.mem

import pt.isel.daw.Lobby
import pt.isel.daw.LobbyState
import pt.isel.daw.User
import pt.isel.daw.repo.RepositoryLobby

class RepositoryLobbiesInMem : RepositoryLobby {
    private val lobbies = mutableListOf<Lobby>()

    override fun createLobby(
        name: String,
        description: String,
        maxPlayers: Int,
        host: User,
        rounds: Int,
        ante: Int,
    ): Lobby =
        Lobby(lobbies.size + 1, name, description, maxPlayers, host, rounds, mutableListOf(host), ante).also {
            lobbies.add(it)
            println(lobbies)
        }

    override fun findByName(name: String): Lobby? = lobbies.find { it.name == name }

    override fun addPlayer(
        lobbyID: Int,
        player: User,
    ): Lobby? {
        val lobby = getById(lobbyID) ?: return null
        if (lobby.players.size >= lobby.maxPlayers) throw IllegalStateException("Lobby is full")
        if (lobby.players.any { it.id == player.id }) return lobby
        val updatedLobby = lobby.copy(players = (lobby.players + player))
        save(updatedLobby)
        return updatedLobby
    }

    override fun removePlayer(
        lobbyID: Int,
        player: User,
    ): Lobby? {
        val lobby = getById(lobbyID) ?: return null
        if (lobby.players.none { it.id == player.id }) return lobby
        if (lobby.host.id == player.id) {
            deleteById(lobbyID)
            return null
        }
        val updatedPlayers = lobby.players.filter { it.id != player.id }.toMutableList()
        val updatedLobby = lobby.copy(players = updatedPlayers)
        save(updatedLobby)
        return updatedLobby
    }

    override fun getById(id: Int): Lobby? = lobbies.find { it.id == id }

    override fun getAll(): List<Lobby> = lobbies.toList()

    override fun save(entity: Lobby) {
        lobbies.removeIf { it.id == entity.id }
        lobbies.add(entity)
    }

    override fun deleteById(id: Int): Boolean = lobbies.removeIf { it.id == id }

    override fun clear() = lobbies.clear()

    override fun getAllAvailable(): List<Lobby> =
        lobbies.filter {
            it.players.size < it.maxPlayers && it.state == LobbyState.OPEN
        }.toList()

    override fun updateAutoStartTime(
        lobbyID: Int,
        autoStartAt: Long?,
    ) {
        val lobby = getById(lobbyID) ?: return
        val updatedLobby = lobby.copy(autoStartAt = autoStartAt)
        save(updatedLobby)
    }
}
