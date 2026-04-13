package pt.isel.daw.repo.jdbc

import pt.isel.daw.Round
import pt.isel.daw.Turn
import pt.isel.daw.User
import pt.isel.daw.repo.RepositoryRounds
import java.sql.Connection
import java.sql.ResultSet

class RepositoryRoundsJdbc(
    private val con: Connection,
) : RepositoryRounds {
    val repoTurns = RepositoryTurnsJdbc(con)
    val repoUsers = RepositoryUserJdbc(con)

    override fun createRound(round: Round): Round {
        val sql =
            """
            INSERT INTO dbo.rounds (match_id, round_num, current_player_id)
            VALUES (?, ?, ?)
            RETURNING id
            """.trimIndent()
        con.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, round.matchId)
            stmt.setInt(2, round.roundNumber)
            stmt.setInt(3, round.currentPlayer.id)

            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    val id = rs.getInt("id")
                    return Round(id, round.matchId, round.roundNumber, mutableListOf(), round.currentPlayer)
                } else {
                    throw IllegalStateException("Failed to insert round")
                }
            }
        }
    }

    override fun getById(id: Int): Round? {
        val sql = "SELECT * FROM dbo.rounds WHERE id = ?"
        con.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, id)
            stmt.executeQuery().use { rs ->
                return if (rs.next()) mapRowToRound(rs) else null
            }
        }
    }

    override fun getAll(): List<Round> {
        val sql = "SELECT * FROM dbo.rounds"
        con.prepareStatement(sql).use { stmt ->
            stmt.executeQuery().use { rs ->
                val rounds = mutableListOf<Round>()
                while (rs.next()) {
                    rounds.add(mapRowToRound(rs))
                }
                return rounds
            }
        }
    }

    override fun save(entity: Round) {
        val updateRoundSQL =
            """
            UPDATE dbo.rounds
            set current_player_id = ?
            WHERE id = ?
            """.trimIndent()
        con.prepareStatement(updateRoundSQL).use { stmt ->
            stmt.setInt(1, entity.currentPlayer.id)
            stmt.setInt(2, entity.id)
            stmt.executeUpdate()
        }
        entity.turns.forEach { repoTurns.save(it) }
        if (entity.winners.isNotEmpty()) {
            val sql =
                """
                INSERT INTO dbo.round_winners (round_id, user_id) 
                VALUES (?, ?) 
                ON CONFLICT (round_id, user_id) DO NOTHING
                """.trimIndent()
            con.prepareStatement(sql).use { stmt ->
                for (winner in entity.winners) {
                    stmt.setInt(1, entity.id)
                    stmt.setInt(2, winner.id)
                    stmt.executeUpdate()
                }
            }
        }
    }

    override fun deleteById(id: Int): Boolean {
        val sql = "DELETE FROM dbo.rounds WHERE id = ?"
        con.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, id)
            val rowsAffected = stmt.executeUpdate()
            return rowsAffected > 0
        }
    }

    override fun clear() {
        con.prepareStatement("TRUNCATE dbo.rounds CASCADE").use { it.executeUpdate() }
    }

    private fun mapRowToRound(rs: ResultSet): Round {
        val roundId = rs.getInt("id")
        val turns = mutableListOf<Turn>()
        con.prepareStatement("SELECT turn_id FROM dbo.round_turns WHERE round_id=?").use { stmt ->
            stmt.setInt(1, roundId)
            stmt.executeQuery().use { rsTurns ->
                while (rsTurns.next()) {
                    val turnId = rsTurns.getInt("turn_id")
                    repoTurns.getById(turnId)?.let { turns.add(it) }
                }
            }
        }
        val currentPlayerId = rs.getInt("current_player_id")
        return Round(
            id = roundId,
            matchId = rs.getInt("match_id"),
            roundNumber = rs.getInt("round_num"),
            turns = turns,
            currentPlayer = repoUsers.getById(currentPlayerId) ?: throw IllegalStateException("User not found"),
            winners = getRoundWinners(roundId),
        )
    }

    private fun getRoundWinners(roundId: Int): List<User> {
        val winners = mutableListOf<User>()
        val sql = "SELECT user_id FROM dbo.round_winners WHERE round_id = ?"
        con.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, roundId)
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
