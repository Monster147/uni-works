package pt.isel.pc.assignment2

import pt.isel.pc.utils.NodeLinkedList
import java.util.*
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration

class ThreadPoolExecutor(
    private val maxThreadPoolSize: Int,
    private val keepAliveTime: Duration
) {

    init {
        require(maxThreadPoolSize > 0) { "maxThreadPoolSize must be greater than 0" }
        require(keepAliveTime > Duration.ZERO) { "keepAliveTime must be higher than 0" }
    }

    enum class PossibleStates {
        RUNNING,
        SHUTDOWN,
        TERMINATED
    }

    var currentState = PossibleStates.RUNNING

    //private val logger = LoggerFactory.getLogger(ThreadPoolExecutor::class.java)

    private val taskQueue = NodeLinkedList<Continuation<Unit>>()
    private val activeThreads = mutableSetOf<Worker>()
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    /**
     * Submits a continuation for execution by the thread pool.
     *
     * The continuation is added to the internal task queue and may be picked up by an available worker thread.
     * If no worker is available and the number of active workers is less than [maxThreadPoolSize],
     * a new worker thread is created to process tasks.
     *
     * This method returns immediately and does not wait for the continuation to be executed.
     *
     * @param continuation The continuation to be executed.
     * @throws RejectedExecutionException if the executor has been shut down.
     */
    fun execute(continuation: Continuation<Unit>) {
        lock.withLock {
            if (currentState == PossibleStates.SHUTDOWN) {
                throw RejectedExecutionException("Executor has been shut down.")
            }

            taskQueue.addLast(continuation)

            if (activeThreads.size < maxThreadPoolSize) {
                val worker = Worker()
                activeThreads.add(worker)
                worker.start()
            }
        }
    }

    /**
     * Initiates an orderly shutdown of the thread pool executor.
     *
     * In shutdown mode:
     * - No new tasks are accepted (calls to [execute] will throw [RejectedExecutionException]).
     * - Previously submitted tasks continue to be executed.
     * - Worker threads will terminate once all tasks are processed and the [keepAliveTime] expires.
     *
     * If there are no active worker threads and the task queue is empty at the time of shutdown,
     * the executor transitions immediately to the TERMINATED state.
     */
    fun shutdown() {
        lock.withLock {
            //isShutdown = true
            currentState = PossibleStates.SHUTDOWN
            checkTermination()
        }
    }

    /**
     * Blocks the calling thread until the executor reaches the TERMINATED state
     * or the specified [timeout] expires.
     *
     * This is a blocking operation and should not be used inside coroutines.
     *
     * @param timeout Maximum duration to wait.
     * @return `true` if termination occurred in time, `false` otherwise.
     * @throws InterruptedException if the waiting thread is interrupted.
     */
    @Throws(InterruptedException::class)
    fun awaitTermination(timeout: Duration): Boolean {
        val nanos = timeout.inWholeNanoseconds
        lock.withLock {
            var remaining = nanos
            while (currentState != PossibleStates.TERMINATED) {
                try {
                    if (remaining <= 0) return false
                    remaining = condition.awaitNanos(remaining)
                } catch (e: InterruptedException) {
                    throw e
                }
            }
            return true
        }
    }

    /**
     * Suspends the calling coroutine until the executor reaches the TERMINATED state.
     *
     * This method is coroutine-friendly and avoids blocking the thread. If the executor
     * is already terminated, it returns immediately.
     */
    suspend fun awaitTermination(): Unit {
        if (currentState == PossibleStates.TERMINATED) return
        return suspendCoroutine { continuation ->
            lock.withLock {
                if (currentState != PossibleStates.TERMINATED) {
                    condition.await()
                }
                continuation.resume(Unit)
            }
        }
    }

    /**
     * Checks whether the executor has fully terminated.
     *
     * This method is invoked after a worker thread exits or when the executor is shut down.
     * It transitions the executor to the TERMINATED state if both of the following conditions are met:
     * - The executor is in the SHUTDOWN state (i.e., shutdown() was called).
     * - There are no remaining active worker threads and the task queue is empty.
     *
     * When termination is confirmed, it:
     * - Updates the internal state to TERMINATED.
     * - Signals any threads waiting on the condition variable (e.g., in awaitTermination).
     * - Resumes any suspended coroutines waiting via the suspend awaitTermination() function.
     */
    private fun checkTermination() {
        if (activeThreads.isEmpty()) {
            currentState = PossibleStates.TERMINATED
            condition.signalAll()
        }
    }

    /**
     * Represents a worker thread in the thread pool executor.
     *
     * Each worker repeatedly retrieves and executes submitted continuations from the task queue.
     * A worker exits and terminates under the following conditions:
     * - The executor is in SHUTDOWN state and the task queue is empty.
     * - The worker remains idle (i.e., no task available) longer than [keepAliveTime].
     *
     * On termination, the worker removes itself from the active worker set and invokes [checkTermination]
     * to potentially update the executor's state.
     */
    private inner class Worker : Thread() {
        override fun run() {
            try {
                while (true) {
                    val task: Continuation<Unit> = lock.withLock {
                        while (taskQueue.empty) {
                            if (currentState == PossibleStates.SHUTDOWN) {
                                activeThreads.remove(this)
                                checkTermination()
                                return
                            }
                            val waitResult = condition.awaitNanos(keepAliveTime.inWholeNanoseconds)
                            if (waitResult <= 0) {
                                activeThreads.remove(this)
                                checkTermination()
                                return
                            }
                        }
                        taskQueue.getAndRemoveFirst().value
                    }

                    try {
                        task.resumeWith(Result.success(Unit))
                    } catch (_: Throwable) {
                    }
                }
            } finally {
                lock.withLock {
                    activeThreads.remove(this)
                    checkTermination()
                }
            }
        }
    }
}
