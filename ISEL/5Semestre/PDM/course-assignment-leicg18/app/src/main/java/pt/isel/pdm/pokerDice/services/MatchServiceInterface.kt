package pt.isel.pdm.pokerDice.services

import kotlinx.coroutines.flow.Flow
import pt.isel.pdm.pokerDice.domain.Match
import pt.isel.pdm.pokerDice.domain.Round
import pt.isel.pdm.pokerDice.domain.User
import pt.isel.pdm.pokerDice.services.dto.MatchEvent

interface MatchServiceInterface {
    suspend fun startMatch(lobbyId: Int, host: User): Either<MatchError, Match>

    suspend fun startNextRound(matchId: Int): Either<MatchError, Match>

    suspend fun playTurn(
        matchId: Int,
        player: User,
        keptDice: List<Boolean>? = null,
    ): Either<MatchError, Match>

    suspend fun passTurn(
        matchId: Int,
        player: User,
    ): Either<MatchError, Match>

    suspend fun finishRound(
        matchId: Int,
        host: User,
    ): Either<MatchError, Match>

    suspend fun getMatchById(matchId: Int): Match?

    suspend fun getRoundById(
        matchId: Int,
        roundNumber: Int,
    ): Round?

    fun subscribeToMatchEvents(
        matchId: Int,
    ) : Flow<MatchEvent>
}