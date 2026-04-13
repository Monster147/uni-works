package pt.isel.daw.repo.jdbc

import pt.isel.daw.PasswordValidationInfo
import pt.isel.daw.Token
import pt.isel.daw.TokenValidationInfo
import pt.isel.daw.User
import pt.isel.daw.UserStats
import pt.isel.daw.repo.RepositoryUser
import java.sql.Connection
import java.sql.ResultSet
import java.time.Instant

class RepositoryUserJdbc(
    private val con: Connection,
) : RepositoryUser {
    override fun getById(id: Int): User? {
        val sql = "SELECT * FROM dbo.users WHERE id = ?"
        con.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, id)
            stmt.executeQuery().use { rs ->
                return if (rs.next()) mapUserFromResultSet(rs) else null
            }
        }
    }

    override fun getAll(): List<User> {
        val sql = "SELECT * FROM dbo.users"
        con.prepareStatement(sql).use { stmt ->
            stmt.executeQuery().use { rs ->
                val users = mutableListOf<User>()
                while (rs.next()) {
                    users.add(mapUserFromResultSet(rs))
                }
                return users
            }
        }
    }

    override fun save(entity: User) {
        val sql =
            """
            UPDATE dbo.users
            SET name = ?, email = ?, balance = ?
            WHERE id = ?
            """.trimIndent()
        con.prepareStatement(sql).use { stmt ->
            stmt.setString(1, entity.name)
            stmt.setString(2, entity.email)
            stmt.setInt(3, entity.balance)
            stmt.setInt(4, entity.id)
            stmt.executeUpdate()
        }
    }

    override fun deleteById(id: Int): Boolean {
        con.prepareStatement("DELETE FROM dbo.users WHERE id = ?").use { stmt ->
            stmt.setInt(1, id)
            val rowsAffected = stmt.executeUpdate()
            return rowsAffected > 0
        }
    }

    override fun clear() {
        con.prepareStatement("TRUNCATE dbo.tokens CASCADE").use { it.executeUpdate() }
        con.prepareStatement("TRUNCATE dbo.users CASCADE").use { it.executeUpdate() }
    }

    override fun createUser(
        name: String,
        email: String,
        passwordValidation: PasswordValidationInfo,
    ): User {
        val sql =
            """
            INSERT INTO dbo.users (name, email, password_validation)
            VALUES (?, ?, ?)
            RETURNING id
            """.trimIndent()

        con.prepareStatement(sql).use { stmt ->
            stmt.setString(1, name)
            stmt.setString(2, email)
            stmt.setString(3, passwordValidation.validationInfo)

            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    val id = rs.getInt("id")
                    createStatsForNewUser(id)
                    return User(id, name, email, passwordValidation)
                } else {
                    throw IllegalStateException("Failed to insert user")
                }
            }
        }
    }

    override fun createStatsForNewUser(userId: Int) {
        val sql =
            """
            INSERT INTO dbo.user_stats (user_id)
            VALUES (?)
            """.trimIndent()

        con.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, userId)
            stmt.executeUpdate()
        }
    }

    override fun findByEmail(email: String): User? {
        val sql = "SELECT * FROM dbo.users WHERE email = ?"
        con.prepareStatement(sql).use { stmt ->
            stmt.setString(1, email)
            stmt.executeQuery().use { rs ->
                return if (rs.next()) mapUserFromResultSet(rs) else null
            }
        }
    }

    override fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>? {
        val sql =
            """
            SELECT id, name, email, password_validation, balance, is_admin, token_validation, created_at, last_used_at
            FROM dbo.Users as users 
            INNER JOIN dbo.tokens as tokens 
            ON users.id = tokens.user_id
            WHERE token_validation = ?
            """.trimIndent()
        con.prepareStatement(sql).use { stmt ->
            stmt.setString(1, tokenValidationInfo.validationInfo)
            stmt.executeQuery().use { rs ->
                return if (rs.next()) {
                    val user = mapUserFromResultSet(rs)
                    val token =
                        Token(
                            tokenValidationInfo,
                            rs.getInt("id"),
                            Instant.ofEpochSecond(rs.getLong("created_at")),
                            Instant.ofEpochSecond(rs.getLong("last_used_at")),
                        )
                    Pair(user, token)
                } else {
                    null
                }
            }
        }
    }

    override fun createToken(
        token: Token,
        maxTokens: Int,
    ) {
        // Delete the oldest token when achieved the maximum number of tokens
        val deletions =
            con.prepareStatement(
                """
                DELETE FROM dbo.tokens
                WHERE user_id = ?
                AND token_validation IN (
                    SELECT token_validation FROM dbo.Tokens
                    WHERE user_id = ?
                    ORDER BY last_used_at DESC
                    OFFSET ?
                )
                """.trimIndent(),
            ).use { stmt ->
                stmt.setInt(1, token.userId)
                stmt.setInt(2, token.userId)
                stmt.setInt(3, maxTokens - 1)
                stmt.executeUpdate()
            }

        //  println("Deleted $deletions old tokens")

        con.prepareStatement(
            """
            INSERT INTO dbo.tokens (token_validation, user_id, created_at, last_used_at)
            VALUES (?, ?, ?, ?)
            """.trimIndent(),
        ).use { stmt ->
            stmt.setString(1, token.tokenValidationInfo.validationInfo)
            stmt.setInt(2, token.userId)
            stmt.setLong(3, token.createdAt.epochSecond)
            stmt.setLong(4, token.lastUsedAt.epochSecond)
            stmt.executeUpdate()
        }
    }

    override fun updateTokenLastUsed(
        token: Token,
        now: Instant,
    ) {
        val sql =
            """
            UPDATE dbo.tokens
            SET last_used_at = ?
            WHERE token_validation = ?
            """.trimIndent()
        con.prepareStatement(sql).use { stmt ->
            stmt.setLong(1, token.lastUsedAt.epochSecond)
            stmt.setString(2, token.tokenValidationInfo.validationInfo)
            stmt.executeUpdate()
        }
    }

    override fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int {
        val sql = "DELETE FROm dbo.tokens WHERE token_validation = ?"
        con.prepareStatement(sql).use { stmt ->
            stmt.setString(1, tokenValidationInfo.validationInfo)
            return stmt.executeUpdate()
        }
    }

    override fun getUserStatsById(id: Int): UserStats? {
        val sql =
            """
            SELECT *
            FROM dbo.user_stats
            WHERE user_id = ?
            """.trimIndent()

        con.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, id)
            stmt.executeQuery().use { rs ->
                return if (rs.next()) {
                    UserStats(
                        id = rs.getInt("user_id"),
                        rounds_won = rs.getInt("rounds_won"),
                        rounds_lost = rs.getInt("rounds_lost"),
                        rounds_drawn = rs.getInt("rounds_drawn"),
                        total_matches = rs.getInt("total_matches"),
                        matches_won = rs.getInt("matches_won"),
                        matches_lost = rs.getInt("matches_lost"),
                        matches_drawn = rs.getInt("matches_drawn"),
                        winrate = rs.getDouble("winrate"),
                    )
                } else {
                    null
                }
            }
        }
    }

    override fun saveUserStats(entity: UserStats) {
        val sql =
            """
            UPDATE dbo.user_stats
            SET rounds_won = ?, rounds_lost = ?, rounds_drawn = ?,
                total_matches = ?, matches_won = ?, matches_lost = ?, matches_drawn = ?,
                winrate = ?
            WHERE user_id = ?
            """.trimIndent()

        con.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, entity.rounds_won)
            stmt.setInt(2, entity.rounds_lost)
            stmt.setInt(3, entity.rounds_drawn)

            stmt.setInt(4, entity.total_matches)
            stmt.setInt(5, entity.matches_won)
            stmt.setInt(6, entity.matches_lost)
            stmt.setInt(7, entity.matches_drawn)

            stmt.setDouble(8, entity.winrate)
            stmt.setInt(9, entity.id)

            stmt.executeUpdate()
        }
    }

    override fun userStatsReset(id: Int): Boolean {
        val sql = "DELETE FROM dbo.user_stats WHERE user_id = ?"

        con.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, id)
            return stmt.executeUpdate() > 0
        }
    }

    override fun calculateWinrate(stats: UserStats): UserStats {
        val winrate =
            if (stats.total_matches == 0) {
                0.0
            } else {
                stats.matches_won / stats.total_matches.toDouble()
            }

        return stats.copy(winrate = winrate)
    }

    fun mapUserFromResultSet(rs: ResultSet): User =
        User(
            id = rs.getInt("id"),
            name = rs.getString("name"),
            email = rs.getString("email"),
            passwordValidation = PasswordValidationInfo(rs.getString("password_validation")),
            balance = rs.getInt("balance"),
            isAdmin = rs.getBoolean("is_admin"),
        )
}
