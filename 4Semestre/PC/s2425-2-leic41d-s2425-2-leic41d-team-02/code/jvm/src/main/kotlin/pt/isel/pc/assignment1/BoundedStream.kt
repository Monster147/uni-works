package pt.isel.pc.assignment1

import java.io.Closeable
import java.util.concurrent.locks.ReentrantLock
import kotlin.time.Duration
import kotlin.concurrent.withLock
/**
 * A thread-safe bounded stream implementation that allows writing and reading elements with support for concurrency.
 *
 * This class uses a circular buffer to store elements up to a fixed capacity, automatically discarding the oldest
 * elements when the buffer is full.
 *
 * ## Thread Safety
 * * This implementation uses a [ReentrantLock] for synchronization, ensuring that multiple threads can safely read and
 * * write concurrently. The [condition] variable is used to allow threads to wait for new data.
 *
 * ## Writing to the Stream
 * * The [write] method allows adding elements to the stream. If the buffer is full, the oldest element is overwritten.
 * * If the stream is closed, writing is no longer possible.
 *
 * ## Reading from the Stream
 * * The [read] method allows retrieving elements from the stream starting from a given index. If there are no new elements,
 * * the method blocks until new data is available or the timeout expires.
 *
 * ## Closing the Stream
 * * When the [close] method is called, the stream is marked as closed, and all waiting readers are notified.
 * * Any further read or write operations return a closed result.
 *
 * @param T The type of elements stored in the stream.
 * @param capacity The maximum number of elements that can be stored in the buffer.
 */
class BoundedStream<T>(capacity: Int) : Closeable {
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private val items = arrayOfNulls<Any>(capacity)
    private var closed = false
    private var currentIndex = 0L
    private var writeIndex = 0
    private var readIndex = 0
    private var size = 0


    /**
     * Writes an item to the stream.
     *
     * If the buffer is full, the oldest element is overwritten. If the stream is closed, writing fails.
     *
     * @param item The item to be written to the stream.
     * @return A [WriteResult] indicating whether the write was successful or if the stream is closed.
     */
    fun write(item: T): WriteResult {
        lock.withLock {
            if (closed) return WriteResult.Closed
            if (size == items.size) {
                readIndex = (readIndex + 1) % items.size
                size--
            }
            items[writeIndex] = item
            writeIndex = (writeIndex + 1) % items.size
            currentIndex++
            size++
            condition.signalAll()
            return WriteResult.Success
        }
    }

    /**
     * Reads items from the stream starting from a given index.
     *
     * If no new data is available, this method blocks until new data arrives or the timeout expires.
     * If the stream is closed, reading fails.
     *
     * @param startIndex The index from which to start reading.
     * @param timeout The maximum duration to wait for new data before timing out.
     * @return A [ReadResult] indicating success with the retrieved items, a timeout, or a closed stream.
     * @throws InterruptedException If the thread is interrupted while waiting for data.
     */
    @Throws(InterruptedException::class)
    fun read(startIndex: Int, timeout: Duration): ReadResult<T> {
        lock.withLock {
            if (closed) return ReadResult.Closed
            var remainingNanos = timeout.inWholeNanoseconds

            while (true) {
                remainingNanos = condition.awaitNanos(remainingNanos)

                val targetIdx = startIndex.toLong()
                val resultItems = mutableListOf<T>()

                // Iterate through the circular array
                for (i in 0 until size) {
                    val globalIndex = currentIndex - size + i
                    if (globalIndex >= targetIdx) {
                        val item = items[(readIndex + i) % items.size]
                        item?.let {
                            resultItems.add(it as T)
                        }
                    }
                }

                if (resultItems.isNotEmpty()) {
                    return ReadResult.Success(
                        items = resultItems,
                        startIndex = targetIdx
                    )
                }

                if (remainingNanos <= 0) return ReadResult.Timeout

                if (closed) return ReadResult.Closed
            }
        }
    }

    /**
     * Closes the stream, preventing further writes and reads.
     * Notifies all waiting threads to unblock.
     */
    override fun close() =
        lock.withLock {
            closed = true
            condition.signalAll()
        }

    /**
     * Represents the result of a write operation.
     */
    sealed interface WriteResult {
        /** Indicates a successful write. */
        data object Success : WriteResult
        /** Indicates that the stream is closed and cannot be written to. */
        data object Closed : WriteResult
    }

    /**
     * Represents the result of a read operation.
     */
    sealed interface ReadResult<out T> {
        /** Indicates that the stream is closed and cannot be read from. */
        data object Closed : ReadResult<Nothing>
        /** Indicates that the read operation timed out waiting for new data. */
        data object Timeout : ReadResult<Nothing>
        /**
         * Indicates a successful read operation.
         *
         * @param items The list of retrieved items.
         * @param startIndex The index from which reading started.
         */
        data class Success<T>(
            val items: List<T>,
            val startIndex: Long,
        ) : ReadResult<T>
    }
}