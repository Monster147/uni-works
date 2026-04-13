package pt.isel.pc.assignment1

import pt.isel.pc.utils.TestHelper
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ThreadScopeTest {
    @Test
    fun testStartThread() {
        val scope = ThreadScope("TestScope", Thread.ofVirtual())
        var threadRan = false
        val thread = scope.startThread { threadRan = true }
        assertNotNull(thread, "Thread should be created")
        thread.join()
        assertTrue(threadRan)
        assertEquals("TestScope-thread-0", thread.name)
    }

    @Test
    fun stressTestStartThread() {
        val scope = ThreadScope("StressTestScope", Thread.ofVirtual())
        val threadCount = 10000
        val counter = AtomicInteger(0)
        val testHelper = TestHelper(10.seconds)

        testHelper.createAndStartMultiple(threadCount) { _, isDone ->
            scope.startThread {
                counter.incrementAndGet()
            }
        }

        testHelper.join()
        assertEquals(threadCount, counter.get())
    }

    @Test
    fun testNewChildScope() {
        val parentScope = ThreadScope("ParentScope", Thread.ofVirtual())
        val childScope = parentScope.newChildScope("ChildScope")
        assertNotNull(childScope, "Child should be created")
        assertNotSame(parentScope, childScope, "Child scope should be a separate instance")
        var threadRan = false
        childScope.startThread { threadRan = true }
        childScope.join(1000.milliseconds)
        assertTrue(threadRan)
        parentScope.close()
        assertNull(parentScope.newChildScope("AnotherChild"), "Child should not be created")
    }

    @Test
    fun stressTestNewChildScope() {
        val parentScope = ThreadScope("ParentScope", Thread.ofVirtual())
        val childCount = 10000
        val totalThreads = AtomicInteger(0)
        val testHelper = TestHelper(10.seconds)

        testHelper.createAndStartMultiple(childCount) { index, _ ->
            parentScope.newChildScope("ChildScope$index")
            totalThreads.incrementAndGet()
        }

        testHelper.join()
        val childScope = parentScope.newChildScope("ChildScope")
        assertNotNull(childScope, "Child should not be created")
        assertEquals(childCount, totalThreads.get())
    }

    @Test
    fun testClose() {
        val scope = ThreadScope("TestScope", Thread.ofVirtual())
        scope.close()
        val thread = scope.startThread { }
        assertNull(thread, "Thread should not be created")
        val childScope = scope.newChildScope("Child")
        assertNull(childScope, "Child should not be created")
    }

    @Test
    fun stressTestClose() {
        val scope = ThreadScope("StressTestScope", Thread.ofVirtual())
        val threadCount = 1000
        val testHelper = TestHelper(10.seconds)

        testHelper.createAndStartMultiple(threadCount) { _, _ ->
            scope.startThread { }
        }

        scope.close()
        testHelper.join()
        val thread = scope.startThread { }
        assertNull(thread, "No thread should be created after scope is closed")
    }

    /*
    Teste passa sempre quando inicializar sozinho
    Teste passa só se for realizado depois do stress test quando dar run
    a todos os testes.
     */
    @Test
    fun testJoin() {
        val scope = ThreadScope("scope", Thread.ofVirtual())
        val child = scope.newChildScope("childScope")!!
        val lock = ReentrantLock()
        val condition = lock.newCondition()
        val childCondition = lock.newCondition()

        scope.startThread { lock.withLock { condition.signalAll() } }
        child.startThread { lock.withLock { childCondition.signalAll() } }

        val joined = scope.join(1.seconds)
        assertTrue(joined)
    }

    @Test
    fun stressTestJoin() {
        val rootScope = ThreadScope("root", Thread.ofPlatform())
        val testHelper = TestHelper(20.seconds)

        testHelper.thread {
            repeat(100) { i ->
                val childScope = rootScope.newChildScope("child-$i")
                assertNotNull(childScope)
                repeat(10) { j ->
                    val thread =
                        childScope.startThread {
                            Thread.sleep(10)
                        }
                    assertNotNull(thread)
                }
            }
        }

        val joinResult = rootScope.join(15.seconds)
        assertTrue(joinResult)

        testHelper.join()
    }

    @Test
    fun testCancel() {
        val scope = ThreadScope("TestScope", Thread.ofVirtual())
        var threadInterrupted = false
        var threadCompleted = false

        scope.startThread {
            try {
                Thread.sleep(5000)
                threadCompleted = true
            } catch (e: InterruptedException) {
                threadInterrupted = true
            }
        }
        Thread.sleep(100)
        scope.cancel()
        scope.join(1000.milliseconds)
        assertTrue(threadInterrupted, "Thread should have been interrupted")
        assertFalse(threadCompleted, "Thread should not have completed")
        assertNull(scope.startThread { }, "Should not be able to start new thread after cancellation")
        assertNull(scope.newChildScope("ChildScope"), "Should not be able to create new child scope after cancellation")
    }

    @Test
    fun stressCancelTest() {
        val scope = ThreadScope("StressTestScope", Thread.ofVirtual())
        val threadCount = 10000
        val interruptedThreads = AtomicInteger(0)
        val completedThreads = AtomicInteger(0)
        val testHelper = TestHelper(10.seconds)

        testHelper.createAndStartMultiple(threadCount) { _, isDone ->
            scope.startThread {
                try {
                    while (!isDone()) {
                        Thread.sleep(100)
                    }
                    completedThreads.incrementAndGet()
                } catch (e: InterruptedException) {
                    interruptedThreads.incrementAndGet()
                }
            }
        }

        Thread.sleep(100)
        scope.cancel()
        testHelper.join()

        scope.join(10000.milliseconds)
        assertEquals(
            threadCount,
            interruptedThreads.get() + completedThreads.get(),
            "All threads should be either interrupted or completed",
        )
        assertTrue(interruptedThreads.get() > 0, "Some threads should have been interrupted")
        assertTrue(completedThreads.get() < threadCount, "Not all threads should have completed")
        repeat(100) {
            assertNull(scope.startThread { }, "Should not be able to start new thread after cancellation")
        }
    }
}
