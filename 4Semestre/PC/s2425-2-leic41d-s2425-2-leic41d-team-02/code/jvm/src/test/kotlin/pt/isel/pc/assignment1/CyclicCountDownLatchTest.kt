package pt.isel.pc.assignment1

import pt.isel.pc.utils.TestHelper
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class CyclicCountDownLatchTest {
    @Test
    fun `test countDown reduces counter and resets when zero`() {
        val latch = CyclicCountDownLatch(3)
        assertEquals(0, latch.countDown()) // counter = 2
        assertEquals(0, latch.countDown()) // counter = 1
        assertEquals(0, latch.countDown()) // counter = 3 (reset)
    }

    @Test
    fun `test await completes when count reaches zero`() {
        val latch = CyclicCountDownLatch(2)
        val executor = Executors.newSingleThreadExecutor()
        val result =
            executor.submit<Boolean> {
                latch.await(2.seconds)
            }
        Thread.sleep(500)
        latch.countDown() // counter = 1
        latch.countDown() // counter = 2 (reset)
        assertTrue(result.get())
        executor.shutdown()
    }

    @Test
    fun `test await times out if count does not reach zero`() {
        val latch = CyclicCountDownLatch(2)
        val result = latch.await(500.milliseconds)
        assertFalse(result)
    }

    @Test
    fun `test concurrent coundDown and await functions`() {
        val latch = CyclicCountDownLatch(3)
        val executor = Executors.newFixedThreadPool(3)
        val results = mutableListOf<Boolean>()
        repeat(3) { index ->
            executor.submit {
                val result = latch.await(5.seconds)
                synchronized(results) {
                    results.add(result)
                }
            }
        }
        Thread.sleep(500)
        assertEquals(0, latch.countDown()) // counter = 2, no threads released yet
        assertEquals(0, latch.countDown()) // counter = 1, no threads released yet
        assertEquals(3, latch.countDown()) // counter = 3 (reset), 3 threads released
        executor.shutdown()
        executor.awaitTermination(10, TimeUnit.SECONDS)
        assertEquals(3, results.size)
        assert(results.all { it }) // All threads should have completed successfully
    }

    @Test
    fun `stress test countDown with high contention`() {
        val latch = CyclicCountDownLatch(10)
        val testHelper = TestHelper(5.seconds)
        val completedCycles = AtomicInteger(0)
        testHelper.createAndStartMultiple(100) { _, isDone ->
            while (!isDone()) {
                val releasedThreads = latch.countDown()
                if (releasedThreads > 0) {
                    println("Cycle completed, released threads: $releasedThreads")
                    completedCycles.incrementAndGet()
                }
            }
        }
        testHelper.join()
        assertTrue(completedCycles.get() == 0, "No cycles should be complete")
    }

    @Test
    fun `stress test await with high contention`() {
        val latch = CyclicCountDownLatch(10)
        val testHelper = TestHelper(5.seconds)
        val successfulWaits = AtomicInteger(0)
        testHelper.createAndStartMultiple(100) { _, isDone ->
            while (!isDone()) {
                if (latch.await(500.milliseconds)) {
                    successfulWaits.incrementAndGet()
                }
            }
        }
        testHelper.createAndStartMultiple(10) { _, isDone ->
            while (!isDone()) {
                latch.countDown()
            }
        }
        testHelper.join()
        assertTrue(successfulWaits.get() > 0, "No waits were successful")
    }

    @Test
    fun `stress test countDown and await concurrently`() {
        val latch = CyclicCountDownLatch(10)
        val testHelper = TestHelper(5.seconds)
        val completedCycles = AtomicInteger(0)
        val successfulWaits = AtomicInteger(0)
        testHelper.createAndStartMultiple(50) { _, isDone ->
            while (!isDone()) {
                if (latch.await(500.milliseconds)) {
                    successfulWaits.incrementAndGet()
                }
            }
        }
        testHelper.createAndStartMultiple(50) { _, isDone ->
            while (!isDone()) {
                val releasedThreads = latch.countDown()
                if (releasedThreads > 0) {
                    completedCycles.incrementAndGet()
                }
            }
        }
        testHelper.join()
        assertTrue(completedCycles.get() > 0, "No cycles were completed")
        assertTrue(successfulWaits.get() > 0, "No waits were successful")
    }
}
