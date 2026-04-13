package pt.isel.pc.assignment3

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class BoundedStreamFlow {

    @Test
    fun `test boundedStream asFlow function`() {
        val stream = BoundedStreamSuspend<String>(5)
        runBlocking {
        stream.write("Item 1")
        stream.write("Item 2")
        stream.write("Item 3")
        stream.write("Item 4")
        stream.write("5")
        val result = mutableListOf<Pair<Long, String>>()
        stream.asFlow().toList(result)
        println(result)
        assertEquals(
            listOf(
                0L to "Item 1",
                1L to "Item 2",
                2L to "Item 3",
                3L to "Item 4",
                4L to "5"
            ),
            result
        )
    }
}

    @Test
    fun `test collect asFlow`() {
        val stream = BoundedStreamSuspend<String>(5)
        runBlocking {
            stream.write("Item 1")
            stream.write("Item 2")
            stream.write("Item 3")
            stream.write("Item 4")
            stream.write("5")
            val result = mutableListOf<Pair<Long, String>>()
            stream.asFlow().collect { item ->
                result.add(item)
            }
            println(result)
            assertEquals(
                listOf(
                    0L to "Item 1",
                    1L to "Item 2",
                    2L to "Item 3",
                    3L to "Item 4",
                    4L to "5"
                ),
                result
            )
        }
    }

    @Test
    fun `test asFlow with overflow`() {
        val stream = BoundedStreamSuspend<String>(5)
        runBlocking {
            stream.write("Overflow Item")
            stream.write("Item 1")
            stream.write("Item 2")
            stream.write("Item 3")
            stream.write("Item 4")
            stream.write("5")
            val result = mutableListOf<Pair<Long, String>>()
            stream.asFlow().collect { item ->
                result.add(item)
            }
            println(result)
            assertEquals(
                listOf(
                    1L to "Item 1",
                    2L to "Item 2",
                    3L to "Item 3",
                    4L to "Item 4",
                    5L to "5"
                ),
                result
            )
        }
    }

    @Test
    fun `stress test asFlow`() {
        val stream = BoundedStreamSuspend<Int>(100)
        val itemCount = 1_000
        val result = mutableListOf<Pair<Long, Int>>()
        runBlocking {
            val job = launch {
                stream.asFlow().toList(result)
            }
            repeat(itemCount) { i ->
                stream.write(i)
            }
            stream.close()
            job.join()
        }
        assertEquals(100, result.size)
        assertEquals((itemCount - 100 ..< itemCount).map { it.toLong() to it }, result)
    }

    @Test
    fun `bigger stress test asFlow`() {
        val stream = BoundedStreamSuspend<Int>(1000)
        val itemCount = 1_000
        val result = mutableListOf<Pair<Long, Int>>()
        runBlocking {
            val job = launch {
                stream.asFlow().toList(result)
            }
            repeat(itemCount) { i ->
                stream.write(i)
            }
            stream.close()
            job.join()
        }
        assertEquals(itemCount, result.size)
        assertEquals((0 ..< itemCount).map { it.toLong() to it }, result)
        }
}