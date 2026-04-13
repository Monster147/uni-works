package pt.isel.pc.assignment2

import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.seconds

class ThreadPoolExecutorTest {
    private val maxThreadPoolSize = 2
    private val duration = 100.milliseconds

    data class Message(
        val producerIx: Int,
        val messageIx: Int,
    )

    @Test
    fun `execute adds continuation to task list`() {
        var continuationCalled = false
        val executor = ThreadPoolExecutor(maxThreadPoolSize, duration)
        val continuation = (object : Continuation<Unit> {
            override val context
                get() = EmptyCoroutineContext

            override fun resumeWith(result: Result<Unit>) {
                continuationCalled = true
            }
        })
        executor.execute(continuation)
        Thread.sleep(200)
        assertTrue(continuationCalled)
    }

    @Test
    fun `execute throws RejectedExecutionException when executor is shut down`() {
        val executor = ThreadPoolExecutor(maxThreadPoolSize, duration)
        val lock = Object()
        val continuation = (object : Continuation<Unit> {
            override val context
                get() = EmptyCoroutineContext

            override fun resumeWith(result: Result<Unit>) {
                synchronized(lock) {
                    lock.wait(100) // Mantém o executor ocupado por um curto período
                }
            }
        })
        executor.execute(continuation)
        executor.shutdown()
        Assertions.assertEquals(ThreadPoolExecutor.PossibleStates.SHUTDOWN, executor.currentState)
        Assertions.assertThrows(RejectedExecutionException::class.java) {
            executor.execute(continuation)
        }
    }

    @Test
    fun `shutdown changes state to SHUTDOWN when threads are running`() {
        val executor = ThreadPoolExecutor(maxThreadPoolSize, duration)
        val continuation = (object : Continuation<Unit> {
            override val context
                get() = EmptyCoroutineContext

            override fun resumeWith(result: Result<Unit>) {}
        })
        executor.execute(continuation)
        executor.shutdown()
        Assertions.assertEquals(ThreadPoolExecutor.PossibleStates.SHUTDOWN, executor.currentState)
    }

    @Test
    fun `shutdown changes state to TERMINATED when no threads are running`() {
        val executor = ThreadPoolExecutor(maxThreadPoolSize, duration)
        executor.shutdown()
        Assertions.assertEquals(ThreadPoolExecutor.PossibleStates.TERMINATED, executor.currentState)
    }

    @Test
    fun `awaitTermination returns true if already terminated`() {
        val executor = ThreadPoolExecutor(maxThreadPoolSize, duration)
        executor.shutdown()
        val terminated = executor.awaitTermination(100.milliseconds)
        Assertions.assertTrue(terminated)
        Assertions.assertEquals(ThreadPoolExecutor.PossibleStates.TERMINATED, executor.currentState)
    }

    @Test
    fun `awaitTermination returns false if timeout is exceeded`() {
        val executor = ThreadPoolExecutor(maxThreadPoolSize, duration)
        val lock = Object()
        val continuation = (object : Continuation<Unit> {
            override val context
                get() = EmptyCoroutineContext

            override fun resumeWith(result: Result<Unit>) {
                synchronized(lock) {
                    lock.wait(100)
                }
            }
        })
        executor.execute(continuation)
        executor.shutdown()
        val terminated = executor.awaitTermination(10.milliseconds)
        Assertions.assertEquals(ThreadPoolExecutor.PossibleStates.SHUTDOWN, executor.currentState)
        Assertions.assertFalse(terminated)
    }

    @Test
    fun `awaitTermination throws InterruptedException if interrupted`() {
        val executor = ThreadPoolExecutor(maxThreadPoolSize, duration)
        val continuation = (object : Continuation<Unit> {
            override val context
                get() = EmptyCoroutineContext

            override fun resumeWith(result: Result<Unit>) {}
        })
        executor.execute(continuation)
        executor.shutdown()
        val thread = Thread {
            Assertions.assertThrows(InterruptedException::class.java) {
                executor.awaitTermination(1.seconds)
            }
        }
        thread.start()
        Thread.sleep(100)
        thread.interrupt()
        thread.join()
    }

    @Test
    fun `awaitTermination suspends until terminated`() = runBlocking {
        val pool = ThreadPoolExecutor(2, 1.seconds)
        val job = launch {
            pool.awaitTermination()
        }
        assertTrue(job.isActive, "Job should be suspended waiting for termination")
        pool.shutdown()
        pool.awaitTermination(2.seconds)
        job.join()
        assertTrue(job.isCompleted, "Job should be resumed after termination")
    }

    @Test
    fun `threadPoolExecutor stress test (no suspend function)`() {
        val threadPool = ThreadPoolExecutor(MAX_WORKER_THREADS, 1.seconds)
        val synchronizedList = Collections.synchronizedList(mutableListOf<Message>())
        val activeWorkerThreads = AtomicInteger()
        val maxWorkerThreadsExceeded = AtomicBoolean()
        val producers = List(N_OF_PRODUCERS) { producerIx ->
            Thread {
                repeat(N_OF_REPS) { messageIx ->
                    val message = Message(producerIx, messageIx)
                    threadPool.execute(object : Continuation<Unit> {
                        override val context: CoroutineContext
                            get() = EmptyCoroutineContext

                        override fun resumeWith(result: Result<Unit>) {
                            val observedActiveWorkerThreads = activeWorkerThreads.incrementAndGet()
                            if (observedActiveWorkerThreads > MAX_WORKER_THREADS) {
                                maxWorkerThreadsExceeded.set(true)
                            }
                            synchronizedList.addLast(message)
                            Thread.sleep(1)
                            activeWorkerThreads.decrementAndGet()
                        }
                    })
                }
            }.apply { start() }
        }
        producers.forEach { it.join() }
        threadPool.shutdown()
        assertTrue(threadPool.awaitTermination(100.seconds), "Thread pool did not terminate in time")
        assertEquals(N_OF_PRODUCERS * N_OF_REPS, synchronizedList.size)
        assertEquals(N_OF_PRODUCERS * N_OF_REPS, HashSet<Message>(synchronizedList).size)
        assertFalse(maxWorkerThreadsExceeded.get())
    }

    @Test
    fun `stress test for suspend awaitTermination`() = runBlocking {
        val threadPool = ThreadPoolExecutor(MAX_WORKER_THREADS, 1.seconds)
        val synchronizedList = Collections.synchronizedList(mutableListOf<Message>())
        val activeWorkerThreads = AtomicInteger()
        val maxWorkerThreadsExceeded = AtomicBoolean()
        val producers = List(N_OF_PRODUCERS) { producerIx ->
            launch {
                repeat(N_OF_REPS) { messageIx ->
                    val message = Message(producerIx, messageIx)
                    threadPool.execute(object : Continuation<Unit> {
                        override val context: CoroutineContext
                            get() = EmptyCoroutineContext

                        override fun resumeWith(result: Result<Unit>) {
                            val observedActiveWorkerThreads = activeWorkerThreads.incrementAndGet()
                            if (observedActiveWorkerThreads > MAX_WORKER_THREADS) {
                                maxWorkerThreadsExceeded.set(true)
                            }
                            synchronizedList.add(message)
                            Thread.sleep(1)
                            activeWorkerThreads.decrementAndGet()
                        }
                    })
                }
            }
        }
        producers.forEach { it.join() }
        threadPool.shutdown()
        threadPool.awaitTermination()
        assertEquals(N_OF_PRODUCERS * N_OF_REPS, synchronizedList.size)
        assertEquals(N_OF_PRODUCERS * N_OF_REPS, HashSet<Message>(synchronizedList).size)
        assertFalse(maxWorkerThreadsExceeded.get(), "O número máximo de threads foi excedido")
    }

    companion object {
        private const val N_OF_PRODUCERS = 5
        private const val MAX_WORKER_THREADS = 3
        private const val N_OF_REPS = 2000
    }
}