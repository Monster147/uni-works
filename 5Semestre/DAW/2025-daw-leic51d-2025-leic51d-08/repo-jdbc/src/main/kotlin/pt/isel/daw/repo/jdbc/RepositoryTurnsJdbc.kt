package pt.isel.daw.repo.jdbc

import pt.isel.daw.Dice
import pt.isel.daw.DiceFace
import pt.isel.daw.Hand
import pt.isel.daw.HandCategory
import pt.isel.daw.Turn
import pt.isel.daw.TurnState
import pt.isel.daw.repo.RepositoryTurns
import java.sql.Connection
import java.sql.ResultSet

class RepositoryTurnsJdbc(
    val con: Connection,
) : RepositoryTurns {
    val repoUsers = RepositoryUserJdbc(con)

    override fun createTurn(turn: Turn): Turn {
        val sql =
            """
            INSERT INTO dbo.turns(round_id, player_id, dice_values, roll_count, state, score)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING id
            """.trimIndent()
        val diceValuesArray = con.createArrayOf("INT", turn.hand.dice.map { it.face.ordinal }.toTypedArray())
        con.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, turn.roundId)
            stmt.setInt(2, turn.player.id)
            stmt.setArray(3, diceValuesArray)
            stmt.setInt(4, turn.rollCount)
            stmt.setString(5, turn.state.name)
            stmt.setString(6, turn.score?.name)
            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    val id = rs.getInt("id")
                    diceValuesArray.free()
                    return turn.copy(id = id)
                } else {
                    diceValuesArray.free()
                    throw IllegalStateException("Failed to insert Turn")
                }
            }
        }
    }

    override fun getById(id: Int): Turn? {
        val sql = "SELECT * FROM dbo.turns WHERE id = ?"
        con.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, id)
            stmt.executeQuery().use { rs ->
                return if (rs.next()) mapRowToTurn(rs) else null
            }
        }
    }

    override fun getAll(): List<Turn> {
        val sql = "SELECT * FROM dbo.turns"
        con.prepareStatement(sql).use { stmt ->
            stmt.executeQuery().use { rs ->
                val turns = mutableListOf<Turn>()
                while (rs.next()) {
                    turns.add(mapRowToTurn(rs))
                }
                return turns
            }
        }
    }

    override fun save(entity: Turn) {
        val sql =
            """
            UPDATE dbo.turns
            SET dice_values=?, roll_count=?, state=?, score=?
            WHERE id=?
            """.trimIndent()
        val diceValuesArray = con.createArrayOf("INT", entity.hand.dice.map { it.face.ordinal }.toTypedArray())
        con.prepareStatement(sql).use { stmt ->
            stmt.setArray(1, diceValuesArray)
            stmt.setInt(2, entity.rollCount)
            stmt.setString(3, entity.state.name)
            stmt.setString(4, entity.score?.name)
            stmt.setInt(5, entity.id)
            stmt.executeUpdate()
            diceValuesArray.free()
        }
    }

    override fun deleteById(id: Int): Boolean {
        val sql = "DELETE FROM dbo.turns WHERE id = ?"
        con.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, id)
            val rowsAffected = stmt.executeUpdate()
            return rowsAffected > 0
        }
    }

    override fun clear() {
        con.prepareStatement("TRUNCATE dbo.turns CASCADE").use { it.executeUpdate() }
    }

    private fun mapRowToTurn(rs: ResultSet): Turn {
        val diceArray = (rs.getArray("dice_values")?.array as? Array<Int>) ?: emptyArray()
        val player = repoUsers.getById(rs.getInt("player_id")) ?: throw IllegalStateException("User was not found")
        return Turn(
            id = rs.getInt("id"),
            roundId = rs.getInt("round_id"),
            player = player,
            hand = Hand(diceArray.map { Dice(DiceFace.entries[it]) }),
            rollCount = rs.getInt("roll_count"),
            state = TurnState.valueOf(rs.getString("state")),
            score = rs.getString("score")?.let { HandCategory.valueOf(it) },
        )
    }
}
