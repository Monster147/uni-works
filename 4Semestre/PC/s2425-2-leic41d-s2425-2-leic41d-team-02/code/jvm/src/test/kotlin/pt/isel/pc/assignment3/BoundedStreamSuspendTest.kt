package pt.isel.pc.assignment3

import kotlinx.coroutines.*
import pt.isel.pc.assignment3.BoundedStreamSuspend.ReadResult
import pt.isel.pc.assignment3.BoundedStreamSuspend.WriteResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class BoundedStreamSuspendTest {
    @Test
    fun `write and read items`() {
        runBlocking {
            val stream = BoundedStreamSuspend<String>(3)
            assertEquals<Any>(WriteResult.Success, stream.write("Item 1"))
            assertEquals<Any>(WriteResult.Success, stream.write("Item 2"))
            assertEquals<Any>(WriteResult.Success, stream.write("Item 3"))
            val readResult = stream.read(0, 2.seconds)
            println(readResult)
            assertTrue(readResult is ReadResult.Success)
            assertEquals(listOf("Item 1", "Item 2", "Item 3"), readResult.items)
        }
    }

    @Test
    fun `write items beyond capacity`() {
        runBlocking {
            val stream = BoundedStreamSuspend<String>(2)
            assertEquals<Any>(WriteResult.Success, stream.write("Item 1"))
            assertEquals<Any>(WriteResult.Success, stream.write("Item 2"))
            assertEquals<Any>(WriteResult.Success, stream.write("Item 3"))
            val readResult = stream.read(0, 2.seconds)
            assertTrue(readResult is ReadResult.Success)
            println(readResult)
            assertEquals(listOf("Item 2", "Item 3"), readResult.items)
        }
    }

    @Test
    fun `close stream and write`() {
        runBlocking {
            val stream = BoundedStreamSuspend<String>(2)
            stream.close()
            assertEquals(WriteResult.Closed, stream.write("Item 1"))
            stream.close()
            assertEquals(WriteResult.Closed, stream.write("Item 2"))
        }
    }

    @Test
    fun `read with timeout`() {
        runBlocking {
            val stream = BoundedStreamSuspend<String>(2)
            val readResult = stream.read(0, 1.seconds)
            assertTrue(readResult is ReadResult.Timeout)
        }
    }

    @Test
    fun `close stream and read`() {
        runBlocking {
            val stream = BoundedStreamSuspend<String>(2)
            stream.close()
            val readResult = stream.read(0, 1.seconds)
            assertTrue(readResult is ReadResult.Closed)
        }
    }

    @Test
    fun `stress test with interleaved writers and readers`() =
        runBlocking {
            val capacity = 100
            val writerCount = 50
            val readerCount = 50
            val messagesPerWriter = 10000
            val stream = BoundedStreamSuspend<String>(capacity)

            val writers =
                List(writerCount) { writerId ->
                    launch(Dispatchers.Default) {
                        repeat(messagesPerWriter) { i ->
                            val msg = "Writer$writerId-Msg$i"
                            val result = stream.write(msg)
                            check(result is WriteResult.Success)
                        }
                    }
                }

            val readers =
                List(readerCount) { readerId ->
                    launch(Dispatchers.Default) {
                        var nextIndex = 0
                        var readCount = 0
                        while (readCount < writerCount * messagesPerWriter / readerCount) {
                            when (val result = stream.read(nextIndex, 2.seconds)) {
                                is ReadResult.Success -> {
                                    readCount += result.items.size
                                    nextIndex = result.startIndex.toInt() + result.items.size
                                }
                                is ReadResult.Closed -> break
                                is ReadResult.Timeout -> continue
                            }
                        }
                        println("Reader $readerId finished with $readCount messages read")
                    }
                }

            writers.forEach { it.join() }
            stream.close()
            readers.forEach { it.join() }
        }
}
