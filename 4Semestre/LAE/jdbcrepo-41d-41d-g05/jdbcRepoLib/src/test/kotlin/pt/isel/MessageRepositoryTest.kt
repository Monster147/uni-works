package pt.isel

import MessageRepoBaseline
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import pt.isel.chat.Message
import pt.isel.chat.dao.ChannelRepositoryJdbc
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement.RETURN_GENERATED_KEYS
import java.time.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame

class MessageRepositoryTest {
    companion object {
        private val connection: Connection = DriverManager.getConnection(DB_URL)

        @JvmStatic
        fun repositories() =
            listOf<Repository<Long, Message>>(
                MessageRepoBaseline(connection),
                RepositoryReflect(connection, Message::class),
                loadDynamicRepo(connection, Message::class),
            )
    }

    @BeforeTest
    fun beginTransaction() {
        connection.autoCommit = false
    }

    @AfterTest
    fun rollbackTransaction() {
        connection.rollback()
        connection.autoCommit = true
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `getAll should return all messages`(repository: Repository<Long, Message>) {
        val users: List<Message> = repository.getAll()
        assertEquals(20, users.size)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `retrieve a message`(repository: Repository<Long, Message>) {
        val esportsMsg = repository.getAll().first { it.channel.name == "Esports Discussion" }
        val otherMsg = repository.getById(esportsMsg.id)
        assertNotNull(otherMsg)
        assertEquals(otherMsg, esportsMsg)
        assertNotSame(otherMsg, esportsMsg)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `retrieve message with PK three`(repository: Repository<Long, Message>) {
        val msg = repository.getById(3)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `update a message content`(repository: Repository<Long, Message>) {
        val devMsg = repository.getAll().first { it.channel.name == "Development" }
        val updatedMsg = devMsg.copy(content = "Only include relevant and reliable contents.")
        repository.update(updatedMsg)
        val retrieved = repository.getById(updatedMsg.id)
        assertNotNull(retrieved)
        assertEquals(updatedMsg, retrieved)
        assertNotSame(updatedMsg, retrieved)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `update a message channel`(repository: Repository<Long, Message>) {
        val devMsg = repository.getAll().first { it.channel.name == "Development" }
        val general = ChannelRepositoryJdbc(connection).getById("General")
        assertNotNull(general)
        val updatedMsg = devMsg.copy(channel = general)
        repository.update(updatedMsg)
        val retrieved = repository.getById(updatedMsg.id)
        assertNotNull(retrieved)
        assertEquals(updatedMsg, retrieved)
        assertNotSame(updatedMsg, retrieved)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `delete a message`(repository: Repository<Long, Message>) {
        val sql =
            """
            INSERT INTO messages (content, timestamp, user_id, channel_name)
            VALUES (?, ?, ?, ?)
            """
        val values =
            arrayOf<Any>(
                "With Reflection we can provide an auto implementation of SQL operations via JDBC.",
                LocalDate.now().toEpochDay(),
                2,
                "Development",
            )
        val pk =
            connection
                .prepareStatement(sql, RETURN_GENERATED_KEYS)
                .use { stmt ->
                    values.forEachIndexed { index, it -> stmt.setObject(index + 1, it) }
                    stmt.executeUpdate() // Executes the INSERT
                    stmt.generatedKeys.use { rs ->
                        rs.next()
                        rs.getLong(1)
                    }
                }
        assertEquals(21, repository.getAll().size)
        repository.deleteById(pk)
        assertEquals(20, repository.getAll().size)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `test findAll with whereEquals - channel name is Development`(repository: Repository<Long, Message>) {
        val devMessages =
            repository
                .findAll()
                .whereEquals(Message::channel, "Development")
                .iterator()

        assertEquals("This is a private discussion", devMessages.next().content)
        assertEquals("Any updates on the project?", devMessages.next().content)
        assertEquals("Any new code changes?", devMessages.next().content)
        assertEquals("Need help with debugging", devMessages.next().content)
        assertEquals("Excited for the upcoming features!", devMessages.next().content)
        assertFalse { devMessages.hasNext() }
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `test findAll with whereEquals - user id is 1`(repository: Repository<Long, Message>) {
        val user1Messages =
            repository
                .findAll()
                .whereEquals(Message::user, 1)
                .iterator()

        assertEquals("Hello everyone!", user1Messages.next().content)
        assertEquals("How can I help you?", user1Messages.next().content)
        assertEquals("This is a private discussion", user1Messages.next().content)
        assertEquals("Esports tournament starts soon!", user1Messages.next().content)
        assertEquals("Any new code changes?", user1Messages.next().content)
        assertEquals("This is a pinned message", user1Messages.next().content)
        assertEquals("Support requests should go here", user1Messages.next().content)
        assertFalse { user1Messages.hasNext() }
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `test findAll with orderBy - timestamp`(repository: Repository<Long, Message>) {
        val orderedMessages =
            repository
                .findAll()
                .orderBy(Message::timestamp)
                .iterator()

        assertEquals("Hello everyone!", orderedMessages.next().content)
        assertEquals("Welcome to the channel!", orderedMessages.next().content)
        assertEquals("How can I help you?", orderedMessages.next().content)
        assertEquals("This is a private discussion", orderedMessages.next().content)
        assertEquals("Gaming is awesome!", orderedMessages.next().content)
        assertEquals("Any updates on the project?", orderedMessages.next().content)
        assertEquals("The support team is here to assist", orderedMessages.next().content)
        assertEquals("Esports tournament starts soon!", orderedMessages.next().content)
        assertEquals("Join our game night!", orderedMessages.next().content)
        assertEquals("What’s the best gaming setup?", orderedMessages.next().content)
        assertEquals("Please keep the chat professional", orderedMessages.next().content)
        assertEquals("Any new code changes?", orderedMessages.next().content)
        assertEquals("Patch notes for the game are live", orderedMessages.next().content)
        assertEquals("This is a pinned message", orderedMessages.next().content)
        assertEquals("Happy to be part of this channel!", orderedMessages.next().content)
        assertEquals("Need help with debugging", orderedMessages.next().content)
        assertEquals("Tech talk happening today!", orderedMessages.next().content)
        assertEquals("Looking for teammates!", orderedMessages.next().content)
        assertEquals("Support requests should go here", orderedMessages.next().content)
        assertEquals("Excited for the upcoming features!", orderedMessages.next().content)
        assertFalse { orderedMessages.hasNext() }
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `test findAll with whereEquals - channel name Esports Discussion and orderBy timestamp`(repository: Repository<Long, Message>) {
        val esportsMessages =
            repository
                .findAll()
                .whereEquals(Message::channel, "Esports Discussion")
                .orderBy(Message::timestamp)
                .iterator()

        assertEquals("Esports tournament starts soon!", esportsMessages.next().content)
        assertEquals("What’s the best gaming setup?", esportsMessages.next().content)
        assertEquals("Looking for teammates!", esportsMessages.next().content)
        assertFalse { esportsMessages.hasNext() }
    }
}
