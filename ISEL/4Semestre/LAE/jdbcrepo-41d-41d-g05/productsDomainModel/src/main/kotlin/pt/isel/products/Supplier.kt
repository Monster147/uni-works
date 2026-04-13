package pt.isel.products

import pt.isel.Pk
import pt.isel.Table

@Table(name = "suppliers")
data class Supplier(
    @Pk val supplierid: Long,
    val suppliername: String,
    val suppliertype: SupplierType,
)
