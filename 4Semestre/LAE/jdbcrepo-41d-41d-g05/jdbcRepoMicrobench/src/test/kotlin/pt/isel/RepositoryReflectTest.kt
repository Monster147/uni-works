package pt.isel

import pt.isel.chat.Channel
import pt.isel.products.Product
import kotlin.test.Test
import kotlin.test.assertEquals

class RepositoryReflectTest {
    private val repository: Repository<String, Channel> =
        RepositoryReflect(FakeConnection(channelData), Channel::class) // ChannelRepositoryJdbc(connection)

    @Test
    fun `getAll should return all channels`() {
        val channels: List<Channel> = repository.getAll()
        println(channels)
        assertEquals(5, channels.size)
    }

    @Test
    fun `getAll should return all products`() {
        val products: List<Product> = RepositoryReflect<Long, Product>(FakeConnection(productData), Product::class).getAll()
        println(products)
        assertEquals(5, products.size)
    }
}
