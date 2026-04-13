package pt.isel.products

import pt.isel.Column
import pt.isel.Pk
import pt.isel.Table

@Table(name = "products")
data class Product(
    @Pk val id: Long,
    val name: String,
    val price: Double,
    val category: ProductCategory,
    @Column("supplierid") val supplier: Supplier,
)
