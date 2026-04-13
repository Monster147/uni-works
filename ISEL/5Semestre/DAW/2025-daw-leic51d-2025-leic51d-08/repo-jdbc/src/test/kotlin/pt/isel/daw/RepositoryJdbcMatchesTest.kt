package pt.isel.daw

import org.junit.jupiter.api.Test
import pt.isel.daw.repo.jdbc.DatabaseConnection
import pt.isel.daw.repo.jdbc.TransactionManagerJdbc
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RepositoryJdbcMatchesTest {
    companion object {
        val trxManager = TransactionManagerJdbc()
        val con = DatabaseConnection.getConnection()
    }

    @BeforeTest
    fun setup() {
        val sql =
            """TRUNCATE 
            |dbo.round_turns, dbo.turns, dbo.match_rounds, dbo.matches, 
            |dbo.match_players, dbo.lobbies, dbo.lobby_players, dbo.users 
            |RESTART IDENTITY CASCADE;"""
                .trimMargin()
        con.prepareStatement(sql).use { stmt ->
            stmt.executeUpdate()
        }
        con.commit()
    }

    @Test
    fun `createMatch and getById`() {
        trxManager.run {
            val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
            val player2 = repoUsers.createUser("Bob", "bob@isel.com", PasswordValidationInfo("hash2"))
            val lobby = repoLobbies.createLobby("Lobby1", "First Lobby", 4, host, 10, ante = 5)
            val updatedLobby = repoLobbies.addPlayer(lobby.id, player2) ?: lobby
            val match = repoMatches.createMatch(updatedLobby)
            val matchGot = repoMatches.getById(match.id)
            println(match)
            println(matchGot)
            assertEquals(match, matchGot)
        }
    }

    @Test
    fun `getAll returns all matches`() {
        trxManager.run {
            val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
            val player2 = repoUsers.createUser("Bob", "bob@isel.com", PasswordValidationInfo("hash2"))
            val lobby = repoLobbies.createLobby("Lobby1", "First Lobby", 4, host, 10, ante = 5)
            val lobby2 = repoLobbies.createLobby("Lobby2", "Second Lobby", 5, player2, 8, ante = 5)
            val match = repoMatches.createMatch(lobby)
            val match2 = repoMatches.createMatch(lobby2)
            val allMatches = repoMatches.getAll()
            assertEquals(2, allMatches.size)
            assertEquals(listOf(match, match2), allMatches)
        }
    }

    @Test
    fun `createMatch and update its rounds, current round with turns and state`() {
        trxManager.run {
            val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
            val player2 = repoUsers.createUser("Bob", "bob@isel.com", PasswordValidationInfo("hash2"))
            val lobby = repoLobbies.createLobby("Lobby1", "First Lobby", 2, host, 2, ante = 5)
            val updateLobby = repoLobbies.addPlayer(lobby.id, player2) ?: lobby
            val match = repoMatches.createMatch(updateLobby)
            val found = repoMatches.getById(match.id)
            assertEquals(match, found)
            val updatedRounds =
                match.rounds.mapIndexed { index, round ->
                    val turns =
                        if (index == 0) {
                            mutableListOf(
                                Turn(1, round.id, host, Hand(), 1, TurnState.COMPLETED, HandCategory.ONE_PAIR),
                                Turn(2, round.id, player2, Hand(), 1, TurnState.IN_PROGRESS, HandCategory.FULL_HOUSE),
                            )
                        } else {
                            mutableListOf(
                                Turn(3, round.id, host, Hand(), 1, TurnState.COMPLETED, HandCategory.ONE_PAIR),
                                Turn(
                                    4,
                                    round.id,
                                    player2,
                                    Hand(),
                                    1,
                                    TurnState.IN_PROGRESS,
                                    HandCategory.THREE_OF_A_KIND,
                                ),
                            )
                        }
                    round.copy(turns = turns)
                }
            val updatedMatch =
                match.copy(
                    rounds = updatedRounds,
                    currentRound = match.currentRound + 2,
                    state = MatchState.COMPLETED,
                )

            repoMatches.save(updatedMatch)

            val foundMatch = repoMatches.getById(match.id)
            println(updatedMatch)
            println(foundMatch)
            assertEquals(updatedMatch, foundMatch)
        }
    }

    @Test
    fun `deleteById removes the match`() {
        trxManager.run {
            val host = repoUsers.createUser("Alice", "alice@example.com", PasswordValidationInfo("hash"))
            val player2 = repoUsers.createUser("Bob", "bob@isel.com", PasswordValidationInfo("hash2"))
            val lobby = repoLobbies.createLobby("Lobby1", "First Lobby", 4, host, 10, ante = 5)
            repoLobbies.addPlayer(lobby.id, player2)
            val match = repoMatches.createMatch(lobby)
            val found = repoMatches.getById(match.id)
            assertEquals(match, found)
            val turnsRound1 =
                mutableListOf(
                    Turn(1, match.id, host, Hand(), 1, TurnState.COMPLETED, HandCategory.ONE_PAIR),
                    Turn(2, match.id, player2, Hand(), 1, TurnState.IN_PROGRESS, HandCategory.FULL_HOUSE),
                )
            val turnsRound2 =
                mutableListOf(
                    Turn(3, match.id, host, Hand(), 1, TurnState.COMPLETED, HandCategory.ONE_PAIR),
                    Turn(4, match.id, player2, Hand(), 1, TurnState.IN_PROGRESS, HandCategory.THREE_OF_A_KIND),
                )
            val updatedRounds =
                match.rounds.mapIndexed { index, round ->
                    if (index == 0) {
                        round.copy(turns = turnsRound1)
                    } else {
                        round.copy(turns = turnsRound2)
                    }
                }
            val updatedMatch =
                match.copy(
                    rounds = updatedRounds,
                    currentRound = match.currentRound + 2,
                    state = MatchState.COMPLETED,
                )

            repoMatches.save(updatedMatch)
            val deleted = repoMatches.deleteById(match.id)
            val shouldBeNull = repoMatches.getById(match.id)
            val shouldBeNull2 = repoMatches.getById(updatedMatch.id)
            assertEquals(true, deleted)
            assertNull(shouldBeNull)
            assertNull(shouldBeNull2)
        }
    }
}
