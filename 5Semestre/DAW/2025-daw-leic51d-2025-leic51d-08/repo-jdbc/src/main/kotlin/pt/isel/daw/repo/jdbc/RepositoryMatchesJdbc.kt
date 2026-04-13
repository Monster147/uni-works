package pt.isel.daw.repo.jdbc

import pt.isel.daw.Hand
import pt.isel.daw.Lobby
import pt.isel.daw.Match
import pt.isel.daw.MatchState
import pt.isel.daw.Round
import pt.isel.daw.Turn
import pt.isel.daw.TurnState
import pt.isel.daw.User
import pt.isel.daw.repo.RepositoryMatches
import java.sql.Connection
import java.sql.ResultSet

class RepositoryMatchesJdbc(
    private val con: Connection,
) : RepositoryMatches {
    private val repoUsers = RepositoryUserJdbc(con)
    private val repoRounds = RepositoryRoundsJdbc(con)
    private val repoTurns = RepositoryTurnsJdbc(con)

    override fun createMatch(lobby: Lobby): Match {
        val sql =
            """
            INSERT INTO dbo.matches(lobby_id, current_round)
            VALUES (?, ?)
            RETURNING id
            """.trimIndent()
        con.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, lobby.id)
            stmt.setInt(2, 0) // current_round inicial
            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    val matchId = rs.getInt("id")
                    val sql = "INSERT INTO dbo.match_players(match_id, user_id) VALUES (?, ?)"
                    for (player in lobby.players) {
                        con.prepareStatement(sql).use { stmt ->
                            stmt.setInt(1, matchId)
                            stmt.setInt(2, player.id)
                            stmt.executeUpdate()
                        }
                    }
                    val rounds = createRoundsForMatch(matchId, lobby)
                    return Match(matchId, lobby.id, lobby.players, rounds.toMutableList(), 0)
                } else {
                    throw IllegalStateException("Failed to insert match")
                }
            }
        }
    }

    override fun getById(id: Int): Match? {
        val sql = "SELECT * FROM dbo.matches WHERE id = ?"
        con.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, id)
            stmt.executeQuery().use { rs ->
                return if (rs.next()) mapRowToMatch(rs) else null
            }
        }
    }

    override fun getAll(): List<Match> {
        val sql = "SELECT * FROM dbo.matches"
        con.prepareStatement(sql).use { stmt ->
            stmt.executeQuery().use { rs ->
                val matches = mutableListOf<Match>()
                while (rs.next()) {
                    matches.add(mapRowToMatch(rs))
                }
                return matches
            }
        }
    }

    override fun save(entity: Match) {
        val updateMatchSQL =
            """
            UPDATE dbo.matches
            SET current_round = ?, state = ?
            WHERE id=?
            """.trimIndent()

        con.prepareStatement(updateMatchSQL).use { stmt ->
            stmt.setInt(1, entity.currentRound)
            stmt.setString(2, entity.state.name)
            stmt.setInt(3, entity.id)
            stmt.executeUpdate()
        }

        if (entity.winners.isNotEmpty()) {
            val matchWinnersSQL =
                """
                INSERT INTO dbo.match_winners (match_id, user_id)
                VALUES (?, ?)
                ON CONFLICT (match_id, user_id) DO NOTHING
                """.trimIndent()

            con.prepareStatement(matchWinnersSQL).use { stmt ->
                for (winner in entity.winners) {
                    stmt.setInt(1, entity.id)
                    stmt.setInt(2, winner.id)
                    stmt.executeUpdate()
                }
            }
        }

        entity.rounds.forEach { round ->
            repoRounds.save(round)
        }
    }

    override fun deleteById(id: Int): Boolean {
        val sql = "DELETE FROM dbo.matches WHERE id = ?"
        con.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, id)
            val rowsAffected = stmt.executeUpdate()
            return rowsAffected > 0
        }
    }

    override fun clear() {
        con.prepareStatement("TRUNCATE dbo.matches CASCADE").use { it.executeUpdate() }
    }

    private fun mapRowToMatch(rs: ResultSet): Match {
        val matchId = rs.getInt("id")
        val players = getPlayersForMatch(matchId)

        val rounds = mutableListOf<Round>()
        con.prepareStatement("SELECT round_id FROM dbo.match_rounds WHERE match_id=?").use { stmt ->
            stmt.setInt(1, matchId)
            stmt.executeQuery().use { rsRounds ->
                while (rsRounds.next()) {
                    val roundId = rsRounds.getInt("round_id")
                    repoRounds.getById(roundId)?.let { rounds.add(it) }
                }
            }
        }

        return Match(
            id = matchId,
            lobbyId = rs.getInt("lobby_id"),
            players = players,
            rounds = rounds,
            currentRound = rs.getInt("current_round"),
            state = MatchState.valueOf(rs.getString("state")),
            winners = getMatchWinners(matchId),
        )
    }

    override fun createRoundsForMatch(
        matchId: Int,
        lobby: Lobby,
    ): List<Round> {
        return (1..lobby.rounds).map { roundNumber ->
            val round =
                repoRounds.createRound(
                    Round(
                        id = 0,
                        matchId = matchId,
                        roundNumber = roundNumber,
                        turns = mutableListOf(),
                        currentPlayer = lobby.players[0],
                    ),
                )
            // Insert into join table match_rounds
            val sql = "INSERT INTO dbo.match_rounds(match_id, round_id) VALUES (?, ?)"
            con.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, matchId)
                stmt.setInt(2, round.id)
                stmt.executeUpdate()
            }
            round.turns.addAll(createTurnsForRound(round, lobby.players))
            round
        }
    }

    override fun createTurnsForRound(
        round: Round,
        players: List<User>,
    ): List<Turn> {
        val turns =
            players.map {
                repoTurns.createTurn(
                    Turn(
                        id = 0,
                        roundId = round.id,
                        player = it,
                        hand = Hand(),
                        rollCount = 0,
                        state = TurnState.IN_PROGRESS,
                        score = null,
                    ),
                )
            }

        val sql = "INSERT INTO dbo.round_turns(round_id, turn_id) VALUES (?, ?)"
        con.prepareStatement(sql).use { stmt ->
            for (turn in turns) {
                stmt.setInt(1, round.id)
                stmt.setInt(2, turn.id)
                stmt.executeUpdate()
            }
        }
        return turns
    }

    private fun getPlayersForMatch(matchId: Int): List<User> {
        val players = mutableListOf<User>()
        val sql = "SELECT user_id FROM dbo.match_players WHERE match_id=?"
        con.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, matchId)
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    val userId = rs.getInt("user_id")
                    repoUsers.getById(userId)?.let { players.add(it) }
                }
            }
        }
        return players
    }

    private fun getMatchWinners(matchId: Int): List<User> {
        val winners = mutableListOf<User>()
        val sql = "SELECT user_id FROM dbo.match_winners WHERE match_id=?"
        con.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, matchId)
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    val userId = rs.getInt("user_id")
                    repoUsers.getById(userId)?.let { winners.add(it) }
                }
            }
        }
        return winners
    }
}
