package pt.isel.pdm.pokerDice.repo

import pt.isel.pdm.pokerDice.domain.Lobby
import pt.isel.pdm.pokerDice.domain.Match
import pt.isel.pdm.pokerDice.domain.Round
import pt.isel.pdm.pokerDice.domain.Turn
import pt.isel.pdm.pokerDice.domain.User


interface RepositoryMatches : Repository<Match> {
    fun createMatch(lobby: Lobby): Match

    fun createRoundsForMatch(
        matchId: Int,
        lobby: Lobby,
    ): List<Round>

    fun createTurnsForRound(
        round: Round,
        players: List<User>,
    ): List<Turn>
}
