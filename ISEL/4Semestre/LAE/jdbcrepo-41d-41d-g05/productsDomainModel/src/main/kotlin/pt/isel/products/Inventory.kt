package pt.isel.products

import pt.isel.Column
import pt.isel.Pk
import pt.isel.Table

@Table(name = "inventories")
data class Inventory(
    @Pk val id: Long,
    val location: String,
    val name: String,
    @Column("supplierid") val supplier: Supplier,
)
