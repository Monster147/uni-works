package pt.isel.pc.assignment3

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.io.Closeable
import kotlin.coroutines.resume
import kotlin.time.Duration

/**
 * A coroutine-based thread-safe bounded stream implementation allowing non-blocking writes/reads.
 *
 * This class uses a circular buffer with suspendable access points, automatically discarding oldest
 * elements when full. Designed for concurrent coroutine usage without thread blocking.
 *
 * ## Thread Safety
 * * Uses [Mutex] for synchronization between coroutines
 * * All operations are suspendable and non-blocking
 *
 * ## Suspension Behavior
 * * [write] suspends only during mutex contention, never blocks threads[2][5]
 * * [read] suspends until data becomes available or timeout occurs[8]
 * * Continuations are properly cleaned up on cancellation[3]
 *
 * ## Capacity Management
 * * Overwrites oldest element when full (same as original)
 * * Maintains write/read indices for circular buffer access
 *
 * ## Closing Behavior
 * * [close] resumes all waiting readers with [ReadResult.Closed]
 * * Subsequent operations return closed status immediately
 *
 * @param T Type of elements stored
 * @param capacity Maximum buffer size
 */
class BoundedStreamSuspend<T>(
    capacity: Int,
) : Closeable {
    private val items = arrayOfNulls<Any>(capacity)
    private var closed = false
    private var currentIndex = 0L
    private var writeIndex = 0
    private var readIndex = 0
    private var size = 0
    private var mutex = Mutex()
    private var continuations = mutableListOf<CancellableContinuation<ReadResult<T>>>()
    val publicCurrentIndex get() = currentIndex
    val publicSize get() = size
    val publicReadIndex get() = readIndex
    val publicItems get() = items

    /**
     * Writes an item to the stream.
     *
     * If the buffer is full, the oldest element is overwritten. If the stream is closed, writing fails.
     *
     * @param item The item to be written to the stream.
     * @return A [WriteResult] indicating whether the write was successful or if the stream is closed.
     */
    suspend fun write(item: T): WriteResult {
        mutex.withLock {
            if (closed) {
                return WriteResult.Closed
            }

            if (size == items.size) {
                readIndex = (readIndex + 1) % items.size
                size--
            }

            items[writeIndex] = item
            writeIndex = (writeIndex + 1) % items.size
            currentIndex++
            size++

            val cont = continuations
            continuations.clear()
            cont.forEach {
                it.resume(ReadResult.Success(listOf(item), currentIndex))
            }

            return WriteResult.Success
        }
    }

    /**
     * Reads items from the stream starting from a given index with suspension.
     *
     * If no new data is available, suspends until data arrives or coroutine is cancelled.
     * If the stream is closed during read, returns [ReadResult.Closed] immediately.
     *
     * @param startIndex The index from which to start reading
     * @return [ReadResult.Success] with available items, [ReadResult.Closed] if stream is closed
     * @throws TimeoutCancellationException if timeout occurs when using [read] with timeout parameter
     */
    @Throws(TimeoutCancellationException::class)
    suspend fun read(startIndex: Int): ReadResult<T> {
        val targetIdx = startIndex.toLong()
        mutex.lock()
        // fast-path
        if (closed) {
            mutex.unlock()
            return ReadResult.Closed
        }
        return suspendCancellableCoroutine { continuation ->
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
                continuation.resume(
                    ReadResult.Success(
                        items = resultItems,
                        startIndex = targetIdx,
                    ),
                )
                mutex.unlock()
            } else {
                continuations.add(continuation)
                mutex.unlock()
            }
        }
    }

    /**
     * Reads items from the stream with a timeout constraint.
     *
     * @param startIndex The index from which to start reading
     * @param timeout Maximum duration to wait for new data
     * @return [ReadResult.Success], [ReadResult.Timeout], or [ReadResult.Closed]
     */
    suspend fun read(
        startIndex: Int,
        timeout: Duration,
    ): ReadResult<T> =
        try {
            withTimeout(timeout) {
                read(startIndex)
            }
        } catch (e: TimeoutCancellationException) {
            ReadResult.Timeout
        }

    /**
     * Closes the stream and notifies all waiting readers.
     *
     * Subsequent read/write operations will return closed status immediately.
     */
    override fun close() {
        if (closed) return
        closed = true
        continuations.forEach { it.resume(ReadResult.Closed) }
        continuations.clear()
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

/**
 * Converts the bounded stream to a [Flow] of index-item pairs.
 *
 * The flow emits all current items in the stream at the time of collection,
 * each paired with its global index. Does not update with subsequent writes.
 *
 * @return [Flow] emitting (index, item) pairs from the stream's current state
 */
fun <T> BoundedStreamSuspend<T>.asFlow(): Flow<Pair<Long, T>> =
    flow {
        val logger = LoggerFactory.getLogger("BoundedStreamSuspendFlow")
        logger.info("Before emitting first value")
        val oldestBS = publicCurrentIndex - publicSize
        for (i in 0..<publicSize) {
            val globalIndex = oldestBS + i
            val item = publicItems[(publicReadIndex + i) % publicItems.size]
            logger.info("Before emitting value {} at index {}", item, globalIndex)
            emit(globalIndex to item as T)
            delay(1)
            logger.info("After emitting value {} at index {}", item, globalIndex)
        }
    }

fun main() {
    val logger = LoggerFactory.getLogger("BoundedStreamSuspendTest")
    val stream = BoundedStreamSuspend<String>(5)
    runBlocking(Dispatchers.Default) {
        stream.write("Item 1")
        stream.write("Item 2")
        stream.write("Item 3")
        stream.write("Item 4")
        stream.write("Item 5")
        stream.asFlow().collect {
            logger.info("Item: {}, Index: {}", it.second, it.first)
            delay(500)
        }
    }
}
