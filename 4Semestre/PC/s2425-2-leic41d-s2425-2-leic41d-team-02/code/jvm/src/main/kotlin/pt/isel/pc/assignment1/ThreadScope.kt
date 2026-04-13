package pt.isel.pc.assignment1

import java.io.Closeable
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * A thread management scope that allows structured concurrency by grouping threads together.
 *
 * This class provides mechanisms to start threads, create child scopes, and ensure that all threads
 * are properly completed before closing. It also supports cancellation and interruption of threads.
 *
 * ## Thread Management
 * - The [startThread] method starts a new thread within the scope.
 * - The [newChildScope] method creates a nested [ThreadScope], enabling hierarchical thread management.
 *
 * ## Lifecycle Control
 * - The [close] method marks the scope as closed, preventing new threads or child scopes from being created.
 * - The [join] method blocks execution until all threads and child scopes have completed, with an optional timeout.
 * - The [cancel] method interrupts all threads and cancels child scopes, ensuring a clean shutdown.
 *
 * ## Synchronization
 * This class uses a [ReentrantLock] for thread-safe operations, ensuring consistency in state changes.
 *
 * @property name The name of the thread scope.
 * @property threadBuilder A [Thread.Builder] used to create threads in this scope.
 */
class ThreadScope(val name: String, val threadBuilder: Thread.Builder) : Closeable {
    private val threads = mutableListOf<Thread>()
    private val childScopes = mutableListOf<ThreadScope>()
    private var isClosed = false
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    /**
     * Starts a new thread in the scope if it is not closed.
     *
     * @param runnable The task to be executed by the new thread.
     * @return The created [Thread] if successful, or `null` if the scope is closed.
     */
    fun startThread(runnable: Runnable): Thread? {
        if(isClosed) return null
        lock.withLock {
            val thread = threadBuilder.name("$name-thread-${threads.size}").start{
                try {
                    runnable.run()
                } finally {
                    lock.withLock {
                        threads.remove(Thread.currentThread())
                        condition.signalAll()
                    }
                }
            }
            threads.add(thread)
            return thread
        }
    }

    /**
     * Creates a new child scope under the current scope if it is not closed.
     *
     * @param name The name of the child scope.
     * @return The created [ThreadScope] if successful, or `null` if the scope is closed.
     */
    fun newChildScope(name: String): ThreadScope? {
        if(isClosed) return null
        val childScope = ThreadScope(name, threadBuilder)
        lock.withLock {
            childScopes.add(childScope)
        }
        return childScope
    }

    /**
     * Closes the current scope, preventing any further threads or child scopes from being created.
     */
    override fun close() {
        isClosed = true
    }

    /**
     * Waits until all threads and child scopes have completed, or the timeout expires.
     *
     * @param timeout The maximum duration to wait for completion.
     * @return `true` if all threads and child scopes completed within the timeout, `false` otherwise.
     * @throws InterruptedException If the waiting thread is interrupted.
     */
    @Throws(InterruptedException::class)
    fun join(timeout: Duration): Boolean {
        var remainingNanos = timeout.inWholeNanoseconds
        lock.withLock {
            while(!areAllThreadsAndScopesCompleted()){
                remainingNanos = condition.awaitNanos(remainingNanos)
                if (remainingNanos <= 0) {
                    return false
                }
            }
            return true
        }
    }

    /**
     * Cancels the scope by interrupting all threads and cancelling all child scopes.
     * Ensures that all pending operations are signaled to complete.
     */
    fun cancel() {
        close()
        lock.withLock {
            if(areAllThreadsAndScopesCompleted()) {
                condition.signalAll()
            }
            threads.forEach { it.interrupt() }
            childScopes.forEach { it.cancel() }
        }
    }

    /**
     * Checks whether all threads and child scopes have completed.
     *
     * @return `true` if no threads are running and all child scopes are also completed.
     */
    private fun areAllThreadsAndScopesCompleted(): Boolean =
        threads.isEmpty() && childScopes.all { it.areAllThreadsAndScopesCompleted() }
}