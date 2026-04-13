package pt.isel

import UserRepoBaseline
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import pt.isel.chat.User
import java.sql.Connection
import java.sql.Date
import java.sql.DriverManager
import java.sql.Statement.RETURN_GENERATED_KEYS
import java.time.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame

class UserRepositoryTest {
    companion object {
        private val connection: Connection = DriverManager.getConnection(DB_URL)

        @JvmStatic
        fun repositories() =
            listOf<Repository<Long, User>>(
                UserRepoBaseline(connection, User::class),
                RepositoryReflect(connection, User::class),
                loadDynamicRepo(connection, User::class),
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
    fun `getAll should return all users`(repository: Repository<Long, User>) {
        val users: List<User> = repository.getAll()
        assertEquals(3, users.size)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `retrieve a user`(repository: Repository<Long, User>) {
        val alice = repository.getAll().first { it.name.contains("Alice") }
        val otherAlice = repository.getById(alice.id)
        assertNotNull(otherAlice)
        assertEquals(alice, otherAlice)
        assertNotSame(alice, otherAlice)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `update a user`(repository: Repository<Long, User>) {
        val bob = repository.getAll().first { it.name.contains("Bob") }
        val updatedBob = bob.copy(email = "bob@marley.dev")
        repository.update(updatedBob)
        val retrieved = repository.getById(bob.id)
        assertNotNull(retrieved)
        assertEquals(updatedBob, retrieved)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `delete a user`(repository: Repository<Long, User>) {
        val sql =
            """
            INSERT INTO users (name, email, birthdate)
            VALUES (?, ?, ?)
            """
        val tarantino =
            connection.prepareStatement(sql, RETURN_GENERATED_KEYS).use { stmt ->
                stmt.setString(1, "Tarantino")
                stmt.setString(2, "pulp@fiction.com")
                stmt.setDate(3, Date.valueOf(LocalDate.of(1994, 1, 1)))
                stmt.executeUpdate() // Executes the INSERT
                val pk =
                    stmt.generatedKeys.use { rs ->
                        rs.next()
                        rs.getLong(1)
                    }
                User(pk, "Tarantino", "pulp@fiction.com", Date.valueOf(LocalDate.of(1994, 1, 1)))
            }
        assertEquals(4, repository.getAll().size)
        repository.deleteById(tarantino.id)
        assertEquals(3, repository.getAll().size)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `findAll with whereEquals - name Alice Johnson`(repository: Repository<Long, User>) {
        val users =
            repository
                .findAll()
                .whereEquals(User::name, "Alice Johnson")
                .iterator()

        assertEquals("Alice Johnson", users.next().name)
        assertFalse { users.hasNext() }
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `findAll with whereEquals - birthdate 1988-09-25`(repository: Repository<Long, User>) {
        val users =
            repository
                .findAll()
                .whereEquals(User::birthdate, Date.valueOf("1988-09-25"))
                .iterator()

        assertEquals("Bob Smith", users.next().name)
        assertFalse { users.hasNext() }
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `findAll ordered by birthdate`(repository: Repository<Long, User>) {
        val users =
            repository
                .findAll()
                .orderBy(User::birthdate)
                .iterator()

        assertEquals("Bob Smith", users.next().name) // 1988-09-25
        assertEquals("Alice Johnson", users.next().name) // 1990-05-12
        assertEquals("Charlie Davis", users.next().name) // 1995-12-10
        assertFalse { users.hasNext() }
    }
}
