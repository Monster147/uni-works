package pt.isel

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import pt.isel.products.Product
import pt.isel.products.ProductCategory
import pt.isel.products.Supplier
import pt.isel.products.SupplierType
import pt.isel.products.dao.ProductRepositoryJdbc
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

class ProductsRepositoryTest {
    companion object {
        private val connection: Connection = DriverManager.getConnection(DB_URL)

        @JvmStatic
        fun repositories() =
            listOf<Repository<Long, Product>>(
                RepositoryReflect(connection, Product::class),
                loadDynamicRepo(connection, Product::class),
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
    fun `getAll should return all products`(repository: Repository<Long, Product>) {
        val products: List<Product> = repository.getAll()
        println(products)
        assertEquals(12, products.size)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `retrieve a product`(repository: Repository<Long, Product>) {
        val product = repository.getAll().first { it.name.contains("Product 1") }
        val otherProduct = repository.getById(product.id)
        assertNotNull(otherProduct)
        assertEquals(product, otherProduct)
        assertEquals("Product 1", otherProduct.name)
        assertEquals(ProductCategory.ELECTRONICS, otherProduct.category)
        assertNotSame(product, otherProduct)
        val products: List<Product> = repository.getAll()
        println(products)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `update a product`(repository: Repository<Long, Product>) {
        val product1 = repository.getAll().first { it.name.contains("Product 1") }
        val updatedProduct1 = product1.copy(price = 12.5)
        repository.update(updatedProduct1)
        val retrieved = repository.getById(product1.id)
        assertNotNull(retrieved)
        assertEquals(updatedProduct1, retrieved)
        val products: List<Product> = repository.getAll()
        println(products)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `delete a product`(repository: Repository<Long, Product>) {
        val sql =
            """
            INSERT INTO products (name, price, category, supplierid)
            VALUES (?, ?, ?, ?)
            """
        val product =
            connection.prepareStatement(sql, RETURN_GENERATED_KEYS).use { stmt ->
                stmt.setString(1, "Test Product")
                stmt.setDouble(2, 99.99)
                stmt.setObject(3, ProductCategory.ELECTRONICS.name, java.sql.Types.OTHER)
                stmt.setLong(4, 1)
                stmt.executeUpdate()
                val pk =
                    stmt.generatedKeys.use { rs ->
                        rs.next()
                        rs.getLong(1)
                    }
                Product(pk, "Test Product", 99.99, ProductCategory.ELECTRONICS, Supplier(1, "Supplier A", SupplierType.LOCAL))
            }
        assertEquals(13, repository.getAll().size)
        repository.deleteById(product.id)
        val retrieved = repository.getById(product.id)
        assertNull(retrieved)
        assertEquals(12, repository.getAll().size)
        val products: List<Product> = repository.getAll()
        println(products)
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `test findAll with whereEquals - category ELECTRONICS`(repository: Repository<Int, Product>) {
        val electronics =
            repository
                .findAll()
                .whereEquals(Product::category, ProductCategory.ELECTRONICS)
                .iterator()

        ProductRepositoryJdbc(connection).insert(
            Product(
                13,
                "Product X",
                15.0,
                ProductCategory.ELECTRONICS,
                Supplier(
                    1,
                    "Supplier A",
                    SupplierType.LOCAL,
                ),
            ),
        )

        assertEquals("Product 1", electronics.next().name)
        assertEquals("Product 4", electronics.next().name)
        assertEquals("Product 8", electronics.next().name)
        assertEquals("Product 11", electronics.next().name)
        assertEquals("Product X", electronics.next().name)
        assertFalse { electronics.hasNext() }
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `test findAll with whereEquals - supplierId 1 and orderBy price`(repository: Repository<Int, Product>) {
        val fromSupplier1OrderedByPrice =
            repository
                .findAll()
                .whereEquals(Product::supplier, 1)
                .orderBy(Product::price)
                .iterator()

        ProductRepositoryJdbc(connection).insert(
            Product(
                13,
                "Product X",
                15.0,
                ProductCategory.ELECTRONICS,
                Supplier(
                    1,
                    "Supplier A",
                    SupplierType.LOCAL,
                ),
            ),
        )

        assertEquals("Product 1", fromSupplier1OrderedByPrice.next().name)
        assertEquals("Product X", fromSupplier1OrderedByPrice.next().name)
        assertEquals("Product 2", fromSupplier1OrderedByPrice.next().name)
        assertEquals("Product 9", fromSupplier1OrderedByPrice.next().name)
        assertFalse { fromSupplier1OrderedByPrice.hasNext() }
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `test findAll with orderBy - category and name`(repository: Repository<Int, Product>) {
        val orderedByCategoryAndName =
            repository
                .findAll()
                .orderBy(Product::category)
                .orderBy(Product::name)
                .iterator()

        ProductRepositoryJdbc(connection).insert(
            Product(
                13,
                "Product X",
                15.0,
                ProductCategory.ELECTRONICS,
                Supplier(
                    1,
                    "Supplier A",
                    SupplierType.LOCAL,
                ),
            ),
        )

        assertEquals("Product 1", orderedByCategoryAndName.next().name)
        assertEquals("Product 11", orderedByCategoryAndName.next().name)
        assertEquals("Product 4", orderedByCategoryAndName.next().name)
        assertEquals("Product 8", orderedByCategoryAndName.next().name)
        assertEquals("Product X", orderedByCategoryAndName.next().name)
        assertEquals("Product 10", orderedByCategoryAndName.next().name)
        assertEquals("Product 3", orderedByCategoryAndName.next().name)
        assertEquals("Product 5", orderedByCategoryAndName.next().name)
        assertEquals("Product 7", orderedByCategoryAndName.next().name)
        assertEquals("Product 12", orderedByCategoryAndName.next().name)
        assertEquals("Product 2", orderedByCategoryAndName.next().name)
        assertEquals("Product 6", orderedByCategoryAndName.next().name)
        assertEquals("Product 9", orderedByCategoryAndName.next().name)
        assertFalse { orderedByCategoryAndName.hasNext() }
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `test findAll with where - price = 15 ordered by price`(repository: Repository<Int, Product>) {
        val productsWithPriceAbove20 =
            repository
                .findAll()
                .whereEquals(Product::price, 15.0)
                .orderBy(Product::price)
                .iterator()

        ProductRepositoryJdbc(connection).insert(
            Product(
                13,
                "Product X",
                15.0,
                ProductCategory.ELECTRONICS,
                Supplier(
                    1,
                    "Supplier A",
                    SupplierType.LOCAL,
                ),
            ),
        )

        assertEquals("Product 3", productsWithPriceAbove20.next().name)
        assertEquals("Product X", productsWithPriceAbove20.next().name)
        assertFalse { productsWithPriceAbove20.hasNext() }
    }

    @ParameterizedTest
    @MethodSource("repositories")
    fun `test findAll with where - price = 10 ordered by price`(repository: Repository<Int, Product>) {
        val productsWithPriceAbove10 =
            repository
                .findAll()
                .whereEquals(Product::price, 10.0)
                .orderBy(Product::price)
                .iterator()

        ProductRepositoryJdbc(connection).insert(
            Product(
                13,
                "Product X",
                15.0,
                ProductCategory.ELECTRONICS,
                Supplier(
                    1,
                    "Supplier A",
                    SupplierType.LOCAL,
                ),
            ),
        )

        assertFalse { productsWithPriceAbove10.hasNext() }
    }
}
