package pt.isel.daw.repo.jdbc

import pt.isel.daw.Lobby
import pt.isel.daw.LobbyState
import pt.isel.daw.User
import pt.isel.daw.repo.RepositoryLobby
import java.sql.Connection
import java.sql.ResultSet

class RepositoryLobbiesJdbc(
    private val con: Connection,
) : RepositoryLobby {
    private val repoUsers = RepositoryUserJdbc(con)

    override fun getById(id: Int): Lobby? {
        val sql = "SELECT * FROM dbo.lobbies WHERE id = ?"
        con.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, id)
            stmt.executeQuery().use { rs ->
                return if (rs.next()) mapRowToLobby(rs) else null
            }
        }
    }

    override fun getAll(): List<Lobby> {
        val sql =
            """
            SELECT * FROM dbo.lobbies
            """.trimIndent()
        con.prepareStatement(sql).use { stmt ->
            stmt.executeQuery().use { rs ->
                val result = mutableListOf<Lobby>()
                while (rs.next()) result.add(mapRowToLobby(rs))
                return result
            }
        }
    }

    override fun getAllAvailable(): List<Lobby> {
        val sql =
            """
            SELECT * FROM dbo.lobbies
            WHERE (state = 'OPEN') AND
            (SELECT COUNT(*) FROM dbo.lobby_players WHERE lobby_id = dbo.lobbies.id) < max_players
            """.trimIndent()
        con.prepareStatement(sql).use { stmt ->
            stmt.executeQuery().use { rs ->
                val result = mutableListOf<Lobby>()
                while (rs.next()) result.add(mapRowToLobby(rs))
                return result
            }
        }
    }

    override fun save(entity: Lobby) {
        val sql =
            """
            UPDATE dbo.lobbies
            SET name=?, description=?, max_players=?, host_id=?, rounds=?, state=?, match_id=?, auto_start_at=?
            WHERE id=?
            """.trimIndent()
        println("Executing save for Lobby: $entity")
        con.prepareStatement(sql).use { stmt ->
            stmt.setString(1, entity.name)
            stmt.setString(2, entity.description)
            stmt.setInt(3, entity.maxPlayers)
            stmt.setInt(4, entity.host.id)
            stmt.setInt(5, entity.rounds)
            stmt.setString(6, entity.state.name)
            stmt.setInt(7, entity.matchId)
            entity.autoStartAt
                ?.also { stmt.setLong(8, it) }
                ?: stmt.setNull(8, java.sql.Types.BIGINT)
            stmt.setInt(9, entity.id)
            stmt.executeUpdate()
        }

        val sqlDeletePlayers = "DELETE FROM dbo.lobby_players WHERE lobby_id=?"
        con.prepareStatement(sqlDeletePlayers).use { stmt ->
            stmt.setInt(1, entity.id)
            stmt.executeUpdate()
        }

        val sqlInsertPlayer = "INSERT INTO dbo.lobby_players(lobby_id, user_id) VALUES (?, ?)"
        con.prepareStatement(sqlInsertPlayer).use { stmt ->
            for (player in entity.players) {
                stmt.setInt(1, entity.id)
                stmt.setInt(2, player.id)
                stmt.addBatch()
            }
            stmt.executeBatch()
        }
    }

    override fun deleteById(id: Int): Boolean {
        val sql = "DELETE FROM dbo.lobbies WHERE id=?"
        con.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, id)
            return stmt.executeUpdate() > 0
        }
    }

    override fun clear() {
        val sql = "TRUNCATE dbo.lobbies CASCADE"
        con.prepareStatement(sql).use { stmt ->
            stmt.executeUpdate()
        }
    }

    override fun createLobby(
        name: String,
        description: String,
        maxPlayers: Int,
        host: User,
        rounds: Int,
        ante: Int,
    ): Lobby {
        val sql =
            """
            INSERT INTO dbo.lobbies(name, description, max_players, host_id, rounds, state, ante)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """.trimIndent()
        con.prepareStatement(sql).use { stmt ->
            stmt.setString(1, name)
            stmt.setString(2, description)
            stmt.setInt(3, maxPlayers)
            stmt.setInt(4, host.id)
            stmt.setInt(5, rounds)
            stmt.setString(6, LobbyState.OPEN.name)
            stmt.setInt(7, ante)
            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    val id = rs.getInt("id")

                    val sqlInsertPlayer = "INSERT INTO dbo.lobby_players(lobby_id, user_id) VALUES (?, ?)"
                    con.prepareStatement(sqlInsertPlayer).use { stmtPlayer ->
                        stmtPlayer.setInt(1, id)
                        stmtPlayer.setInt(2, host.id)
                        stmtPlayer.executeUpdate()
                    }

                    return Lobby(id, name, description, maxPlayers, host, rounds, listOf(host), ante, LobbyState.OPEN)
                } else {
                    throw IllegalStateException("Failed to create lobby")
                }
            }
        }
    }

    override fun findByName(name: String): Lobby? {
        val sql = "SELECT * FROM dbo.lobbies WHERE name = ?"
        con.prepareStatement(sql).use { stmt ->
            stmt.setString(1, name)
            stmt.executeQuery().use { rs ->
                return if (rs.next()) mapRowToLobby(rs) else null
            }
        }
    }

    override fun addPlayer(
        lobbyID: Int,
        player: User,
    ): Lobby? {
        val lobby = getById(lobbyID) ?: return null
        if (lobby.players.any { it.id == player.id }) return lobby

        val sql = "INSERT INTO dbo.lobby_players(lobby_id, user_id) VALUES (?, ?)"
        con.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, lobbyID)
            stmt.setInt(2, player.id)
            stmt.executeUpdate()
        }

        return lobby.copy(players = lobby.players + player)
    }

    override fun removePlayer(
        lobbyID: Int,
        player: User,
    ): Lobby? {
        val lobby = getById(lobbyID) ?: return null

        return if (lobby.host.id == player.id) {
            val deleteLobbySql = "DELETE FROM dbo.lobbies WHERE id=?"
            con.prepareStatement(deleteLobbySql).use { stmt ->
                stmt.setInt(1, lobbyID)
                stmt.executeUpdate()
            }
            val deletePlayersSql = "DELETE FROM dbo.lobby_players WHERE lobby_id=?"
            con.prepareStatement(deletePlayersSql).use { stmt ->
                stmt.setInt(1, lobbyID)
                stmt.executeUpdate()
            }
            null
        } else {
            val sql = "DELETE FROM dbo.lobby_players WHERE lobby_id=? AND user_id=?"
            con.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, lobbyID)
                stmt.setInt(2, player.id)
                stmt.executeUpdate()
            }

            return lobby.copy(players = lobby.players.filter { it.id != player.id })
        }
    }

    override fun updateAutoStartTime(
        lobbyID: Int,
        autoStartAt: Long?,
    ) {
        val sql = "UPDATE dbo.lobbies SET auto_start_at=? WHERE id=?"
        con.prepareStatement(sql).use { stmt ->
            if (autoStartAt == null) {
                stmt.setNull(1, java.sql.Types.BIGINT)
            } else {
                stmt.setLong(1, autoStartAt)
            }
            stmt.setInt(2, lobbyID)
            stmt.executeUpdate()
        }
    }

    private fun mapRowToLobby(rs: ResultSet): Lobby {
        val lobbyId = rs.getInt("id")
        val hostUser = repoUsers.getById(rs.getInt("host_id")) ?: throw IllegalStateException("Host user not found")
        val players = mutableListOf<User>()
        val sqlPlayers = "SELECT user_id FROM dbo.lobby_players WHERE lobby_id=?"
        con.prepareStatement(sqlPlayers).use { stmt ->
            stmt.setInt(1, lobbyId)
            stmt.executeQuery().use { rsPlayers ->
                while (rsPlayers.next()) {
                    val userId = rsPlayers.getInt("user_id")
                    repoUsers.getById(userId)?.let { players.add(it) }
                }
            }
        }
        return Lobby(
            id = lobbyId,
            name = rs.getString("name"),
            description = rs.getString("description"),
            maxPlayers = rs.getInt("max_players"),
            host = hostUser,
            rounds = rs.getInt("rounds"),
            players = players,
            state = LobbyState.valueOf(rs.getString("state")),
            ante = rs.getInt("ante"),
            matchId = rs.getInt("match_id"),
            autoStartAt = rs.getLong("auto_start_at").takeIf { !rs.wasNull() },
        )
    }
}
