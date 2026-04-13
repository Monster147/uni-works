package pt.isel.products.dao

import pt.isel.Column
import pt.isel.Queryable
import pt.isel.QueryableBuilderExplicit
import pt.isel.products.Product
import pt.isel.products.ProductCategory
import pt.isel.products.Supplier
import pt.isel.products.SupplierType
import java.sql.Connection
import java.sql.ResultSet
import kotlin.reflect.full.primaryConstructor

class ProductRepositoryJdbc(
    private val connection: Connection,
) : ProductRepository {
    private val properties =
        Product::class.primaryConstructor!!.parameters.associateWith { param ->
            param.annotations
                .filterIsInstance<Column>()
                .firstOrNull()
                ?.name ?: param.name
        }

    override fun insert(
        id: Long,
        name: String,
        price: Double,
        category: ProductCategory,
        supplierId: Long,
    ) {
        val sql =
            """
            INSERT INTO products (id, name, price, category, supplierid)
            VALUES (?, ?, ?, ?::product_category, ?)
            """.trimIndent()

        connection.prepareStatement(sql).use { stmt ->
            stmt.setLong(1, id)
            stmt.setString(2, name)
            stmt.setDouble(3, price)
            stmt.setString(4, category.name)
            stmt.setLong(5, supplierId)
            stmt.executeUpdate()
        }
    }

    override fun getById(id: Long): Product? {
        val sql =
            """
            SELECT p.*, s.name as supplier_name, s.type as supplier_type 
            FROM products p
            JOIN suppliers s ON p.supplierid = s.supplierid
            WHERE p.id = ?
            """.trimIndent()

        connection.prepareStatement(sql).use { stmt ->
            stmt.setLong(1, id)
            val rs = stmt.executeQuery()
            return if (rs.next()) mapRowToProduct(rs) else null
        }
    }

    override fun getAll(): List<Product> {
        val sql =
            """
            SELECT p.*, s.name as supplier_name, s.type as supplier_type 
            FROM products p
            JOIN suppliers s ON p.supplierid = s.supplierid
            """.trimIndent()

        return connection.prepareStatement(sql).use { stmt ->
            stmt.executeQuery().use { rs ->
                val products = mutableListOf<Product>()
                while (rs.next()) {
                    products.add(mapRowToProduct(rs))
                }
                products
            }
        }
    }

    override fun update(entity: Product) {
        val sql =
            """
            UPDATE products
            SET name = ?, price = ?, category = ?::product_category, supplierid = ?
            WHERE id = ?
            """.trimIndent()

        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, entity.name)
            stmt.setDouble(2, entity.price)
            stmt.setString(3, entity.category.name)
            stmt.setLong(4, entity.supplier.supplierid)
            stmt.setLong(5, entity.id)
            stmt.executeUpdate()
        }
    }

    override fun deleteById(id: Long) {
        val sql = "DELETE FROM products WHERE id = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setLong(1, id)
            stmt.executeUpdate()
        }
    }

    override fun findAll(): Queryable<Product> {
        val sql = "SELECT * FROM products"
        return QueryableBuilderExplicit(connection, sql, properties, ::mapRowToProduct)
    }

    private fun mapRowToProduct(rs: ResultSet): Product {
        val supplier =
            Supplier(
                supplierid = rs.getLong("supplierid"),
                suppliername = rs.getString("suppliername"),
                suppliertype = SupplierType.valueOf(rs.getString("suppliertype")),
            )

        return Product(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            price = rs.getDouble("price"),
            category = ProductCategory.valueOf(rs.getString("category")),
            supplier = supplier,
        )
    }
}
