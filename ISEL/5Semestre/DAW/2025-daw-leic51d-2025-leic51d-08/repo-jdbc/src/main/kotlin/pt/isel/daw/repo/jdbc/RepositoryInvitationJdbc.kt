package pt.isel.daw.repo.jdbc

import pt.isel.daw.Invitation
import pt.isel.daw.repo.RepositoryInvitation
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.UUID

class RepositoryInvitationJdbc(
    private val con: Connection,
) : RepositoryInvitation {
    override fun create(createdBy: Int?): Invitation {
        val code = UUID.randomUUID().toString()
        val sql =
            """
            INSERT INTO dbo.invites (code, create_by, created_at, used)
            VALUES (?, ?, ?, ?)
            """.trimIndent()
        con.prepareStatement(sql).use { stmt ->
            stmt.setString(1, code)
            if (createdBy == null) {
                throw IllegalArgumentException("createdBy cannot be null")
            }
            stmt.setInt(2, createdBy)
            stmt.setTimestamp(3, Timestamp.from(java.time.Instant.now()))
            stmt.setBoolean(4, false)
            stmt.executeUpdate()
        }
        return Invitation(code, createdBy)
    }

    override fun findByCode(code: String): Invitation? {
        val sql = "SELECT code, create_by, created_at, used FROM dbo.invites WHERE code = ?"
        con.prepareStatement(sql).use { stmt ->
            stmt.setString(1, code)
            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    return mapRowToInvitation(rs)
                }
            }
        }
        return null
    }

    override fun consume(code: String): Boolean {
        val sql = "UPDATE dbo.invites SET used = TRUE WHERE code = ? AND used = FALSE"
        con.prepareStatement(sql).use { stmt ->
            stmt.setString(1, code)
            val updated = stmt.executeUpdate()
            return updated > 0
        }
    }

    private fun mapRowToInvitation(rs: ResultSet): Invitation {
        return Invitation(
            code = rs.getString("code"),
            createdBy = rs.getInt("create_by"),
            createdAt = rs.getTimestamp("created_at").toInstant(),
            used = rs.getBoolean("used"),
        )
    }
}
