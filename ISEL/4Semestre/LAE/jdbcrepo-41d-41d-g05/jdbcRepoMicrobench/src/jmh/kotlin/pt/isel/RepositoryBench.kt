package pt.isel

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import pt.isel.chat.Channel
import pt.isel.chat.dao.ChannelRepositoryJdbc
import pt.isel.products.Product
import pt.isel.products.dao.ProductRepositoryJdbc
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.AverageTime) // Measure execution time per operation
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
open class RepositoryBench {
    private val repoProductJdbc = ProductRepositoryJdbc(FakeConnection(productData))

    private val repoProductReflect = RepositoryReflect<Long, Product>(FakeConnection(productData), Product::class)

    private val repoProductDynamic = loadDynamicRepo<Long, Product>(FakeConnection(productData), Product::class)

    @Benchmark
    fun benchRepositoryJdbcGetAllProducts(): List<Product> = repoProductJdbc.getAll()

    @Benchmark
    fun benchRepositoryReflectGetAllProducts(): List<Product> = repoProductReflect.getAll()

    @Benchmark
    fun benchRepositoryDynamicGetAllProducts(): List<Product> = repoProductDynamic.getAll()

    private val repoChannelJdbc = ChannelRepositoryJdbc(FakeConnection(channelData))

    private val repoChannelReflect = RepositoryReflect<String, Channel>(FakeConnection(channelData), Channel::class)

    private val repoChannelDynamic = loadDynamicRepo<String, Channel>(FakeConnection(channelData), Channel::class)

    @Benchmark
    fun benchRepositoryJdbcGetAllChannels(): List<Channel> = repoChannelJdbc.getAll()

    @Benchmark
    fun benchRepositoryReflectGetAllChannels(): List<Channel> = repoChannelReflect.getAll()

    @Benchmark
    fun benchRepositoryDynamicGetAllChannels(): List<Channel> = repoChannelDynamic.getAll()
}
