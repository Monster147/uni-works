package pt.isel.pdm.pokerDice.repo.mem

import pt.isel.pdm.pokerDice.domain.Lobby
import pt.isel.pdm.pokerDice.domain.LobbyState
import pt.isel.pdm.pokerDice.domain.PasswordValidationInfo
import pt.isel.pdm.pokerDice.domain.User
import pt.isel.pdm.pokerDice.repo.RepositoryLobby


class RepositoryLobbiesInMem : RepositoryLobby {

    val users = listOf(
        User(1000, "Alice", "alice@gmail.com", PasswordValidationInfo("hash")),
        User(1001, "Bob", "bob@gmail.com", PasswordValidationInfo("hash")),
        User(1002, "Charlie", "charlie@gmail.com", PasswordValidationInfo("hash")),
        User(1003, "Dave", "dave@gmail.com", PasswordValidationInfo("hash"))
    )
    private val lobbies = mutableListOf(
        Lobby(
            id = 1,
            name = "First Lobby",
            description = "This is the first lobby",
            maxPlayers = 4,
            host = users[0],
            rounds = 5,
            players = mutableListOf(users[0], users[1]),
            state = LobbyState.OPEN
        ),
        Lobby(
            id = 2,
            name = "Second Lobby",
            description = "This is the second lobby",
            maxPlayers = 3,
            host = users[2],
            rounds = 3,
            players = mutableListOf(users[2]),
            state = LobbyState.OPEN
        ),
        Lobby(
            id = 3,
            name = "Third Lobby",
            description = "This is the third lobby",
            maxPlayers = 3,
            host = users[3],
            rounds = 4,
            players = mutableListOf(users[3], users[1]),
            state = LobbyState.OPEN
        ),
        Lobby(
            id = 4,
            name = "My Test Lobby",
            description = "A place to have fun playing poker dice!",
            maxPlayers = 5,
            host = users[0],
            rounds = 10,
            players = mutableListOf(
                users[0],
                users[1],
                users[2]
            ),
            state = LobbyState.OPEN
        )
    )

    override fun createLobby(
        name: String,
        description: String,
        maxPlayers: Int,
        host: User,
        rounds: Int,
        ante: Int
    ): Lobby =
        Lobby(
            lobbies.size + 1,
            name,
            description,
            maxPlayers,
            host,
            rounds,
            mutableListOf(host),
            ante
        ).also {
            lobbies.add(it)
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

    override fun getAll(): List<Lobby> =
        lobbies.toList()

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
}
