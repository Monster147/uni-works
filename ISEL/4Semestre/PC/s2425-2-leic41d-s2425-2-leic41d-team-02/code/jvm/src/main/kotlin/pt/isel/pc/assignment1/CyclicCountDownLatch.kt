package pt.isel.pc.assignment1

import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * Represents a request to acquire the latch, containing a condition variable for waiting.
 * @property condition The condition used to wait for the latch to be released.
 * @property isDone Indicates whether the request has been fulfilled.
 */
data class AcquireRequest(
    val condition: Condition,
    var isDone: Boolean = false,
)

/**
 * A cyclic countdown latch that resets after reaching zero.
 * Threads can wait for the latch to reach zero using [await], and decrement the count using [countDown].
 * Once the count reaches zero, all waiting threads are notified, and the latch resets to its initial value.
 * @param initialCount The initial value of the countdown latch. Must be greater than zero.
 * @throws IllegalArgumentException if [initialCount] is not positive.
 */
class CyclicCountDownLatch(val initialCount: Int) {
    init { require(initialCount > 0) }
    private var counter = initialCount
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private val acquireRequests = mutableListOf<AcquireRequest>()

    /**
     * Decrements the counter by one. If the counter reaches zero, all waiting threads are notified,
     * the latch resets to its initial count, and the number of notified threads is returned.
     * @return The number of threads that were notified when the counter reached zero.
     */
    fun countDown(): Int {
        lock.withLock {
            if (counter > 0) {
                counter--
            }
            if (counter == 0) {
                val requests = acquireRequests.size
                acquireRequests.forEach { request ->
                    request.isDone = true
                    condition.signalAll()
                }
                acquireRequests.clear()
                counter = initialCount
                return requests
            }
            return 0
        }
    }

    /**
     * Waits until the counter reaches zero or the specified timeout expires.
     * @param timeout The maximum duration to wait.
     * @return `true` if the counter reached zero, `false` if the timeout expired before that happened.
     * @throws InterruptedException If the thread is interrupted while waiting.
     */
    @Throws(InterruptedException::class)
    fun await(timeout: Duration): Boolean {
        lock.withLock {
            var remaingNanos = timeout.inWholeNanoseconds
            if(counter == 0) return true
            val selfRequest = AcquireRequest(condition = lock.newCondition())
            acquireRequests.add(selfRequest)
            while(true){
                try{
                    remaingNanos = selfRequest.condition.awaitNanos(remaingNanos)
                    if (selfRequest.isDone) {
                        return true
                    }
                    if (remaingNanos <= 0) {
                        acquireRequests.remove(selfRequest)
                        return false
                    }
                } catch (e: InterruptedException) {
                    if(selfRequest.isDone) {
                        Thread.currentThread().interrupt()
                        return true
                    }
                    acquireRequests.remove(selfRequest)
                    throw e
                }
                finally {
                    if (!selfRequest.isDone) {
                        acquireRequests.remove(selfRequest)
                    }
                }
            }
        }
    }
}