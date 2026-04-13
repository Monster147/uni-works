package pt.isel

import ChannelRepoBaseline
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import pt.isel.chat.Channel
import pt.isel.chat.ChannelType
import pt.isel.chat.dao.ChannelRepositoryJdbc
import java.sql.Connection
import java.sql.DriverManager
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

val DB_URL = System.getenv("DB_URL") ?: throw Exception("Missing env var DB_URL")

class ChannelRepositoryTest {
    companion object {
        private val connection: Connection = DriverManager.getConnection(DB_URL)

        @JvmStatic
        fun repositories() =
            listOf<Repository<String, Channel>>(
                ChannelRepoBaseline(connection),
                RepositoryReflect(connection, Channel::class),
                loadDynamicRepo(connection, Channel::class),
            )
    }

    private val channelRandom =
        Channel("Random", ChannelType.PRIVATE, System.currentTimeMillis(), false, 400, 50, false, 0L)

    @BeforeEach
    fun setup() {
        connection.autoCommit = false
        ChannelRepositoryJdbc(connection).run {
            deleteById("Random")
            insert(channelRandom)
        }
    }

    @AfterTest
    fun rollbackTransaction() {
        connection.rollback()
        connection.autoCommit = true
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `retrieve a channel`(repository: Repository<String, Channel>) {
        val retrieved = repository.getById("General")
        assertNotNull(retrieved)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `update a channel`(repository: Repository<String, Channel>) {
        val updatedChannel = channelRandom.copy(maxMembers = 200, isReadOnly = true)
        repository.update(updatedChannel)

        val retrieved = repository.getById(channelRandom.name)
        assertNotNull(retrieved)
        assertEquals(200, retrieved.maxMembers)
        assertEquals(true, retrieved.isReadOnly)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `delete a channel`(repository: Repository<String, Channel>) {
        repository.deleteById(channelRandom.name)
        val retrieved = repository.getById(channelRandom.name)
        assertNull(retrieved)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `getAll should return all channels`(repository: Repository<String, Channel>) {
        val channels: List<Channel> = repository.getAll()
        assertEquals(6, channels.size)
        assertTrue(channelRandom in channels)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `test findAll with whereEquals - isReadOnly true`(repository: Repository<String, Channel>) {
        val channelsPublicAndReadOnly =
            repository
                .findAll()
                .whereEquals(Channel::type, ChannelType.PUBLIC)
                .whereEquals(Channel::isReadOnly, true)
                .iterator()

        // Insert a new public and read-only channel before iterating over the result
        ChannelRepositoryJdbc(connection).insert(
            Channel("Surf", ChannelType.PUBLIC, System.currentTimeMillis(), false, 400, 50, true, 0L),
        )

        // The newly inserted channel will appear during iteration, even though the query was defined earlier
        assertEquals("Support", channelsPublicAndReadOnly.next().name)
        assertEquals("Surf", channelsPublicAndReadOnly.next().name)
        assertFalse { channelsPublicAndReadOnly.hasNext() }
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `test findAll with whereEquals - isReadOnly false`(repository: Repository<String, Channel>) {
        val channelsPublicAndNotReadOnly =
            repository
                .findAll()
                .whereEquals(Channel::type, ChannelType.PUBLIC)
                .whereEquals(Channel::isReadOnly, false)
                .iterator()

        // Insert a new public and read-only channel before iterating over the result
        ChannelRepositoryJdbc(connection).insert(
            Channel("Surf", ChannelType.PUBLIC, System.currentTimeMillis(), false, 400, 50, false, 0L),
        )

        assertEquals("General", channelsPublicAndNotReadOnly.next().name)
        assertEquals("Gaming Chat", channelsPublicAndNotReadOnly.next().name)
        assertEquals("Surf", channelsPublicAndNotReadOnly.next().name)
        assertFalse { channelsPublicAndNotReadOnly.hasNext() }
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `test findAll with orderBy - With Name`(repository: Repository<String, Channel>) {
        val channelsPublicAndReadOnly =
            repository
                .findAll()
                .orderBy(Channel::name)
                .iterator()

        // Insert a new public and read-only channel before iterating over the result
        ChannelRepositoryJdbc(connection).insert(
            Channel("Surf", ChannelType.PUBLIC, System.currentTimeMillis(), false, 400, 50, true, 0L),
        )

        assertEquals("Development", channelsPublicAndReadOnly.next().name)
        assertEquals("Esports Discussion", channelsPublicAndReadOnly.next().name)
        assertEquals("Gaming Chat", channelsPublicAndReadOnly.next().name)
        assertEquals("General", channelsPublicAndReadOnly.next().name)
        assertEquals("Random", channelsPublicAndReadOnly.next().name)
        assertEquals("Support", channelsPublicAndReadOnly.next().name)
        assertEquals("Surf", channelsPublicAndReadOnly.next().name)
        assertFalse { channelsPublicAndReadOnly.hasNext() }
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `test findAll with orderBy - With Type AND CreatedAt`(repository: Repository<String, Channel>) {
        val channelsOrderedByTypeAndCreatedAt =
            repository
                .findAll()
                .orderBy(Channel::type)
                .orderBy(Channel::createdAt)
                .iterator()

        // Insert a new public and read-only channel before iterating over the result
        ChannelRepositoryJdbc(connection).insert(
            Channel("Surf", ChannelType.PUBLIC, System.currentTimeMillis(), false, 400, 50, true, 0L),
        )

        assertEquals("General", channelsOrderedByTypeAndCreatedAt.next().name)
        assertEquals("Support", channelsOrderedByTypeAndCreatedAt.next().name)
        assertEquals("Gaming Chat", channelsOrderedByTypeAndCreatedAt.next().name)
        assertEquals("Surf", channelsOrderedByTypeAndCreatedAt.next().name)
        assertEquals("Development", channelsOrderedByTypeAndCreatedAt.next().name)
        assertEquals("Esports Discussion", channelsOrderedByTypeAndCreatedAt.next().name)
        assertEquals("Random", channelsOrderedByTypeAndCreatedAt.next().name)
        assertFalse { channelsOrderedByTypeAndCreatedAt.hasNext() }
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `test findAll with whereEquals - maxMembers is 50 and orderBy - with Name`(repository: Repository<String, Channel>) {
        val channelsMaxMembersAndOrderedByName =
            repository
                .findAll()
                .whereEquals(Channel::maxMembers, 50)
                .orderBy(Channel::name)
                .iterator()

        // Insert a new public and read-only channel before iterating over the result
        ChannelRepositoryJdbc(connection).insert(
            Channel("Surf", ChannelType.PUBLIC, System.currentTimeMillis(), false, 400, 50, true, 0L),
        )

        assertEquals("Development", channelsMaxMembersAndOrderedByName.next().name)
        assertEquals("Random", channelsMaxMembersAndOrderedByName.next().name)
        assertEquals("Surf", channelsMaxMembersAndOrderedByName.next().name)
        assertFalse { channelsMaxMembersAndOrderedByName.hasNext() }
    }
}
