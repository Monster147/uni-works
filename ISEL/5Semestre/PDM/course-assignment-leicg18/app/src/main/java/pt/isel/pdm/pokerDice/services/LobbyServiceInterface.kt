package pt.isel.pdm.pokerDice.services

import kotlinx.coroutines.flow.Flow
import pt.isel.pdm.pokerDice.domain.Lobby
import pt.isel.pdm.pokerDice.domain.User
import pt.isel.pdm.pokerDice.services.dto.LobbyEvent

interface LobbyServiceInterface {

    suspend fun createLobby(
        name: String,
        description: String,
        maxPlayers: Int,
        host: User,
        rounds: Int,
        ante: Int,
    ): Either<LobbyError, Lobby>

    suspend fun joinLobby(
        lobbyID: Int,
        player: User,
    ): Either<LobbyError, Lobby>

    suspend fun leaveLobby(
        lobbyID: Int,
        player: User,
    ): Either<LobbyError, Lobby>

    fun getAllLobbies(): Flow<List<Lobby>>

    fun getAllAvailableLobbies(): Flow<List<Lobby>>

    suspend fun getLobbyById(
        lobbyID: Int,
    ): Either<LobbyError, Lobby>

    suspend fun deleteLobby(
        lobbyID: Int,
        requester: User,
    ): Either<LobbyError, Boolean>

    fun subscribeToLobbyEvents(
        lobbyID: Int,
    ): Flow<LobbyEvent>
}
