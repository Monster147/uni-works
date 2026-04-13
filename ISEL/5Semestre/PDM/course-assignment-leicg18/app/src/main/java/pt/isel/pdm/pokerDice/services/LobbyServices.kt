package pt.isel.pdm.pokerDice.services

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import pt.isel.pdm.pokerDice.domain.Lobby
import pt.isel.pdm.pokerDice.domain.User
import pt.isel.pdm.pokerDice.repo.TransactionManager
import pt.isel.pdm.pokerDice.services.dto.LobbyEvent

sealed class LobbyError {
    data object LobbyNameAlreadyInUse : LobbyError()

    data object NotFound : LobbyError()

    data object NotHost : LobbyError()
}

class LobbyServices(
    private val trxManager: TransactionManager,
) : LobbyServiceInterface {
    override suspend fun createLobby(
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

    override suspend fun joinLobby(
        lobbyID: Int,
        player: User,
    ): Either<LobbyError, Lobby> {
        return trxManager.run {
            repoLobbies.addPlayer(lobbyID, player)?.let { success(it) }
                ?: failure(LobbyError.NotFound)
        }
    }

    override suspend fun leaveLobby(
        lobbyID: Int,
        player: User,
    ): Either<LobbyError, Lobby> {
        return trxManager.run {
            repoLobbies.removePlayer(lobbyID, player)?.let { success(it) }
                ?: failure(LobbyError.NotFound)
        }
    }

    override fun getAllLobbies(): Flow<List<Lobby>> = flow {
        emit(trxManager.run { repoLobbies.getAll() })
    }

    override fun getAllAvailableLobbies(): Flow<List<Lobby>> = flow {
        emit(trxManager.run { repoLobbies.getAllAvailable() })
    }

    override suspend fun getLobbyById(lobbyID: Int): Either<LobbyError, Lobby> {
        return trxManager.run {
            val lobby = repoLobbies.getById(lobbyID) ?: return@run failure(LobbyError.NotFound)
            success(lobby)
        }
    }

    override suspend fun deleteLobby(
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

    private val lobbyEvents = MutableSharedFlow<LobbyEvent>()

    override fun subscribeToLobbyEvents(lobbyID: Int): Flow<LobbyEvent> = lobbyEvents
}
