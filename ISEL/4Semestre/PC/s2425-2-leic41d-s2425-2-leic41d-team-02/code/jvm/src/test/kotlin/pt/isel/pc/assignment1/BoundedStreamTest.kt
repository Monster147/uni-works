package pt.isel.pc.assignment1

import org.junit.jupiter.api.Assertions
import pt.isel.pc.assignment1.BoundedStream.*
import pt.isel.pc.utils.TestHelper
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

class BoundedStreamTest {
    @Test
    fun `write and read items`() {
        val stream = BoundedStream<String>(3)
        assertEquals(WriteResult.Success, stream.write("Item 1"))
        assertEquals(WriteResult.Success, stream.write("Item 2"))
        assertEquals(WriteResult.Success, stream.write("Item 3"))

        val readResult = stream.read(0, 1.seconds)
        assertTrue(readResult is ReadResult.Success)
        assertEquals(listOf("Item 1", "Item 2", "Item 3"), readResult.items)
    }

    @Test
    fun `write items beyond capacity`() {
        val stream = BoundedStream<String>(2)
        assertEquals(WriteResult.Success, stream.write("Item 1"))
        assertEquals(WriteResult.Success, stream.write("Item 2"))
        assertEquals(WriteResult.Success, stream.write("Item 3"))

        val readResult = stream.read(0, 2.seconds)
        assertTrue(readResult is ReadResult.Success)
        assertEquals(listOf("Item 2", "Item 3"), readResult.items)
    }

    @Test
    fun `read with timeout`() {
        val stream = BoundedStream<String>(2)
        val readResult = stream.read(0, 1.seconds)
        assertTrue(readResult is ReadResult.Timeout)
    }

    @Test
    fun `close stream and write`() {
        val stream = BoundedStream<String>(2)
        stream.close()
        assertEquals(WriteResult.Closed, stream.write("Item 1"))
    }

    @Test
    fun `close stream and read`() {
        val stream = BoundedStream<String>(2)
        stream.close()
        val readResult = stream.read(0, 1.seconds)
        assertTrue(readResult is BoundedStream.ReadResult.Closed)
    }

    @Test
    fun `stress test with interleaved writers and readers`() {
        val stream = BoundedStream<Int>(capacity = 100)
        val elementsToWrite = (1..100).toList()
        val readItems = mutableSetOf<Int>()
        val testHelper = TestHelper(10.seconds)

        testHelper.createAndStartMultiple(10) { _, isDone ->
            while (!isDone()) {
                elementsToWrite.forEach { stream.write(it) }
            }
        }

        testHelper.createAndStartMultiple(10) { _, isDone ->
            while (!isDone()) {
                val result = stream.read(0, 1.seconds)
                if (result is ReadResult.Success) {
                    synchronized(readItems) { readItems.addAll(result.items) }
                }
            }
        }

        testHelper.join()

        Assertions.assertEquals(100, readItems.size)
        Assertions.assertTrue(readItems.all { it in elementsToWrite })
    }

    @Test
    fun `stress test with multiple readers and a single writer`() {
        val stream = BoundedStream<Int>(capacity = 100)
        val elementsToWrite = (1..200).toList()
        val readItems = mutableSetOf<Int>()
        val testHelper = TestHelper(10.seconds)

        testHelper.thread {
            elementsToWrite.forEach { stream.write(it) }
        }

        testHelper.createAndStartMultiple(10) { _, isDone ->
            while (!isDone()) {
                val result = stream.read(0, 2.seconds)
                if (result is ReadResult.Success) {
                    synchronized(readItems) { readItems.addAll(result.items) }
                }
            }
        }

        testHelper.join()

        val expectedWrittenItems = elementsToWrite.takeLast(100)

        Assertions.assertEquals(100, readItems.size)
        Assertions.assertTrue(readItems.all { it in expectedWrittenItems })
    }
}
