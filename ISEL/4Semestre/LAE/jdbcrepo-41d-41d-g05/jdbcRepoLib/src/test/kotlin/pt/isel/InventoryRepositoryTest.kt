package pt.isel

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import pt.isel.products.Inventory
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

class InventoryRepositoryTest {
    companion object {
        private val connection: Connection = DriverManager.getConnection(DB_URL)

        @JvmStatic
        fun repositories() =
            listOf<Repository<Long, Inventory>>(
                RepositoryReflect(connection, Inventory::class),
                loadDynamicRepo(connection, Inventory::class),
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
    fun `getAll should return all inventories`(repository: Repository<Long, Inventory>) {
        val inventories: List<Inventory> = repository.getAll()
        println(inventories)
        assertEquals(8, inventories.size)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `retrieve an inventory`(repository: Repository<Long, Inventory>) {
        val inventory = repository.getAll().first { it.name.contains("Main Warehouse Inventory") }
        val otherInventory = repository.getById(inventory.id)
        assertNotNull(otherInventory)
        assertEquals(inventory, otherInventory)
        assertEquals("Main Warehouse Inventory", otherInventory.name)
        assertEquals("Warehouse A", otherInventory.location)
        assertNotSame(inventory, otherInventory)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `update a inventory`(repository: Repository<Long, Inventory>) {
        val inventory = repository.getAll().first { it.name.contains("Main Warehouse Inventory") }
        val updatedInventory = inventory.copy(location = "New York")
        repository.update(updatedInventory)
        val retrieved = repository.getById(inventory.id)
        assertNotNull(retrieved)
        assertEquals(updatedInventory, retrieved)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `delete an inventory`(repository: Repository<Long, Inventory>) {
        val sql =
            """
        INSERT INTO inventories (location, name, supplierid)
        VALUES (?, ?, ?)
        """
        val inventory =
            connection.prepareStatement(sql, RETURN_GENERATED_KEYS).use { stmt ->
                stmt.setString(1, "New Location")
                stmt.setString(2, "Temporary Inventory")
                stmt.setLong(3, 1)
                stmt.executeUpdate()
                val pk =
                    stmt.generatedKeys.use { rs ->
                        rs.next()
                        rs.getLong(1)
                    }
                Inventory(pk, "New Location", "Temporary Inventory", Supplier(1, "Supplier A", SupplierType.LOCAL))
            }
        assertEquals(9, repository.getAll().size)
        repository.deleteById(inventory.id)
        val retrieved = repository.getById(inventory.id)
        assertNull(retrieved)
        assertEquals(8, repository.getAll().size)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `test findAll with whereEquals - supplierId 1`(repository: Repository<Long, Inventory>) {
        val supplier1Inventories =
            repository
                .findAll()
                .whereEquals(Inventory::supplier, 1)
                .iterator()

        assertEquals("Main Warehouse Inventory", supplier1Inventories.next().name)
        assertEquals("Seasonal Inventory", supplier1Inventories.next().name)
        assertFalse { supplier1Inventories.hasNext() }
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `test findAll with whereEquals - location Warehouse D`(repository: Repository<Long, Inventory>) {
        val backupStorage =
            repository
                .findAll()
                .whereEquals(Inventory::location, "Warehouse D")
                .iterator()

        assertEquals("Backup Storage", backupStorage.next().name)
        assertFalse { backupStorage.hasNext() }
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `test findAll with orderBy - name`(repository: Repository<Long, Inventory>) {
        val inventoriesByName =
            repository
                .findAll()
                .orderBy(Inventory::name)
                .iterator()

        assertEquals("Backup Storage", inventoriesByName.next().name)
        assertEquals("Main Warehouse Inventory", inventoriesByName.next().name)
        assertEquals("Mobile Unit Storage", inventoriesByName.next().name)
        assertEquals("Online Fulfillment Center", inventoriesByName.next().name)
        assertEquals("Outlet Inventory", inventoriesByName.next().name)
        assertEquals("Regional Distribution Center", inventoriesByName.next().name)
        assertEquals("Retail Store Inventory", inventoriesByName.next().name)
        assertEquals("Seasonal Inventory", inventoriesByName.next().name)
        assertFalse { inventoriesByName.hasNext() }
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `test findAll with orderBy - location and name`(repository: Repository<Long, Inventory>) {
        val inventoriesOrdered =
            repository
                .findAll()
                .orderBy(Inventory::location)
                .orderBy(Inventory::name)
                .iterator()

        // WareHouse A
        assertEquals("Main Warehouse Inventory", inventoriesOrdered.next().name)
        assertEquals("Regional Distribution Center", inventoriesOrdered.next().name)

        // WareHouse B
        assertEquals("Retail Store Inventory", inventoriesOrdered.next().name)
        assertEquals("Seasonal Inventory", inventoriesOrdered.next().name)

        // WareHouse C
        assertEquals("Mobile Unit Storage", inventoriesOrdered.next().name)
        assertEquals("Online Fulfillment Center", inventoriesOrdered.next().name)
        assertEquals("Outlet Inventory", inventoriesOrdered.next().name)

        // WareHouse D
        assertEquals("Backup Storage", inventoriesOrdered.next().name)
        assertFalse { inventoriesOrdered.hasNext() }
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `test findAll with whereEquals - supplierId 3 and orderBy location and name`(repository: Repository<Long, Inventory>) {
        val filteredAndOrdered =
            repository
                .findAll()
                .whereEquals(Inventory::supplier, 3)
                .orderBy(Inventory::location)
                .orderBy(Inventory::name)
                .iterator()

        assertEquals("Regional Distribution Center", filteredAndOrdered.next().name)
        assertEquals("Mobile Unit Storage", filteredAndOrdered.next().name)
        assertFalse { filteredAndOrdered.hasNext() }
    }
}
