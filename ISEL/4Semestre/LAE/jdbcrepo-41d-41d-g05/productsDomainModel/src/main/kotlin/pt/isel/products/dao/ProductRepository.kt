package pt.isel.products.dao

import pt.isel.Repository
import pt.isel.products.Product
import pt.isel.products.ProductCategory

interface ProductRepository : Repository<Long, Product> {
    fun insert(
        id: Long,
        name: String,
        price: Double,
        category: ProductCategory,
        supplierId: Long,
    )

    fun insert(product: Product) {
        insert(
            product.id,
            product.name,
            product.price,
            product.category,
            product.supplier.supplierid,
        )
    }
}
