package pt.isel.daw.service

import org.springframework.stereotype.Component
import pt.isel.daw.Lobby
import pt.isel.daw.User
import pt.isel.daw.repo.TransactionManager

sealed class LobbyError {
    data object LobbyNameAlreadyInUse : LobbyError()

    data object NotFound : LobbyError()

    data object NotHost : LobbyError()
}

const val AUTO_START_MS = 2 * 60 * 1000L

@Component
class LobbyServices(
    private val trxManager: TransactionManager,
) {
    fun createLobby(
        name: String,
        description: String,
        maxPlayers: Int,
        host: User,
        rounds: Int,
        ante: Int,
    ): Either<LobbyError, Lobby> {
        return trxManager.run {
            repoLobbies.findByName(name)?.let {
                return@run failure(LobbyError.LobbyNameAlreadyInUse)
            }
            val lobby = repoLobbies.createLobby(name, description, maxPlayers, host, rounds, ante)
            success(lobby)
        }
    }

    fun joinLobby(
        lobbyID: Int,
        player: User,
    ): Either<LobbyError, Lobby> {
        return trxManager.run {
            repoLobbies.addPlayer(lobbyID, player)?.let {
                val updated = maybeScheduleAutoStart(it)
                if (updated !== it) repoLobbies.updateAutoStartTime(lobbyID, updated.autoStartAt)
                success(updated)
            }
                ?: failure(LobbyError.NotFound)
        }
    }

    fun leaveLobby(
        lobbyID: Int,
        player: User,
    ): Either<LobbyError, Lobby> {
        return trxManager.run {
            val wasHost = wasHost(lobbyID, player)
            val playerRemoved = repoLobbies.removePlayer(lobbyID, player)
            if (wasHost && playerRemoved == null) {
                return@run success(Lobby(lobbyID, "lobbyRemoved", "Lobby $lobbyID was removed", 2, player, 0, mutableListOf(), 0))
            }
            if (playerRemoved != null) {
                if (playerRemoved.players.size < 2 && playerRemoved.autoStartAt != null) {
                    repoLobbies.updateAutoStartTime(lobbyID, null)
                    return@run success(playerRemoved.copy(autoStartAt = null))
                }

                playerRemoved.let {
                    return@run success(it)
                }
            }

            failure(LobbyError.NotFound)
        }
    }

    fun getAllLobbies(): List<Lobby> = trxManager.run { repoLobbies.getAll() }

    fun getAllAvailableLobbies(): List<Lobby> = trxManager.run { repoLobbies.getAllAvailable() }

    fun getLobbyById(lobbyID: Int): Either<LobbyError, Lobby> {
        return trxManager.run {
            val lobby = repoLobbies.getById(lobbyID) ?: return@run failure(LobbyError.NotFound)
            success(lobby)
        }
    }

    fun deleteLobby(
        lobbyID: Int,
        requester: User,
    ): Either<LobbyError, Boolean> {
        return trxManager.run {
            val lobby = repoLobbies.getById(lobbyID) ?: return@run failure(LobbyError.NotFound)
            if (lobby.host.id != requester.id) {
                return@run failure(LobbyError.NotHost)
            }
            success(repoLobbies.deleteById(lobbyID))
        }
    }

    fun maybeScheduleAutoStart(lobby: Lobby): Lobby {
        if (lobby.autoStartAt != null) return lobby
        if (lobby.players.size < 2) return lobby

        return lobby.copy(
            autoStartAt = System.currentTimeMillis() + AUTO_START_MS,
        )
    }

    private fun wasHost(
        lobbyID: Int,
        player: User,
    ): Boolean =
        trxManager.run {
            val lobby = repoLobbies.getById(lobbyID)
            lobby?.host?.id == player.id
        }
}
