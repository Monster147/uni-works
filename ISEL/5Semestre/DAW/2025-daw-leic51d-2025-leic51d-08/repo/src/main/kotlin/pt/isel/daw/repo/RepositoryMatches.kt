package pt.isel.daw.repo

import pt.isel.daw.Lobby
import pt.isel.daw.Match
import pt.isel.daw.Round
import pt.isel.daw.Turn
import pt.isel.daw.User

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
