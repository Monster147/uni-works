package pt.isel

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import pt.isel.products.Supplier
import pt.isel.products.SupplierType
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement.RETURN_GENERATED_KEYS
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull

class SupplierRepositoryTest {
    companion object {
        private val connection: Connection = DriverManager.getConnection(DB_URL)

        @JvmStatic
        fun repositories() =
            listOf<Repository<Long, Supplier>>(
                RepositoryReflect(connection, Supplier::class),
                loadDynamicRepo(connection, Supplier::class),
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
    fun `getAll should return all suppliers`(repository: Repository<Long, Supplier>) {
        val suppliers: List<Supplier> = repository.getAll()
        println(suppliers)
        assertEquals(5, suppliers.size)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `retrieve a supplier`(repository: Repository<Long, Supplier>) {
        val supplier = repository.getAll().first { it.suppliername.contains("Supplier A") }
        val otherSupplier = repository.getById(supplier.supplierid)
        assertNotNull(otherSupplier)
        assertEquals(supplier, otherSupplier)
        assertEquals("Supplier A", otherSupplier.suppliername)
        assertEquals(SupplierType.LOCAL, otherSupplier.suppliertype)
        assertNotSame(supplier, otherSupplier)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `update a supplier`(repository: Repository<Long, Supplier>) {
        val supplier = repository.getAll().first { it.suppliername.contains("Supplier B") }
        val updatedSupplier = supplier.copy(suppliername = "Updated Supplier B")
        repository.update(updatedSupplier)
        val retrieved = repository.getById(supplier.supplierid)
        assertNotNull(retrieved)
        assertEquals(updatedSupplier, retrieved)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `delete a supplier`(repository: Repository<Long, Supplier>) {
        val sql =
            """
            INSERT INTO suppliers (suppliername, suppliertype)
            VALUES (?, ?)
            """
        val supplier =
            connection.prepareStatement(sql, RETURN_GENERATED_KEYS).use { stmt ->
                stmt.setString(1, "Test Supplier")
                stmt.setObject(2, SupplierType.LOCAL.name, java.sql.Types.OTHER)
                stmt.executeUpdate()
                val pk =
                    stmt.generatedKeys.use { rs ->
                        rs.next()
                        rs.getLong(1)
                    }
                Supplier(pk, "Test Supplier", SupplierType.LOCAL)
            }
        assertEquals(6, repository.getAll().size)
        repository.deleteById(supplier.supplierid)
        val retrieved = repository.getById(supplier.supplierid)
        assertNull(retrieved)
        assertEquals(5, repository.getAll().size)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `test findAll with whereEquals - type LOCAL`(repository: Repository<Long, Supplier>) {
        val locals =
            repository
                .findAll()
                .whereEquals(Supplier::suppliertype, SupplierType.LOCAL)
                .iterator()

        assertEquals("Supplier A", locals.next().suppliername)
        assertEquals("Supplier C", locals.next().suppliername)
        assertEquals("Supplier E", locals.next().suppliername)
        assertFalse { locals.hasNext() }
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `test findAll ordered by type then name`(repository: Repository<Long, Supplier>) {
        val ordered =
            repository
                .findAll()
                .orderBy(Supplier::suppliertype)
                .orderBy(Supplier::suppliername)
                .iterator()

        assertEquals("Supplier A", ordered.next().suppliername)
        assertEquals("Supplier C", ordered.next().suppliername)
        assertEquals("Supplier E", ordered.next().suppliername)
        assertEquals("Supplier B", ordered.next().suppliername)
        assertEquals("Supplier D", ordered.next().suppliername)
        assertFalse { ordered.hasNext() }
    }
}
