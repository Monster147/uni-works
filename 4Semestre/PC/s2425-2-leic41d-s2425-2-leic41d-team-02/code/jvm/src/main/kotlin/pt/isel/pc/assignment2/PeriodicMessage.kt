package pt.isel.pc.assignment2

import java.time.Instant
import java.util.*
import kotlin.coroutines.*
import kotlin.coroutines.Continuation
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Represents a structured periodic message that emits a list of strings with a fixed delay
 * between each string, repeated for a configured number of iterations.
 *
 * This data class is used as a configuration input for [show] and [showPeriodicMessage] functions,
 * which handle the asynchronous scheduling and display of these messages using coroutine-style
 * suspension and manual continuation management.
 *
 * @property delayBetweenStrings The delay to wait between each string emission in the list.
 * Must be a non-negative [Duration].
 * @property nOfRepetitions The number of times to repeat the full list of [strings]. Must be strictly positive.
 * @property strings The list of strings to display during each repetition. This list must not be empty.
 *
 * Example usage:
 * ```
 * val msg = PeriodicMessage(
 *     delayBetweenStrings = 1.seconds,
 *     nOfRepetitions = 3,
 *     strings = listOf("Hello", "World")
 * )
 * show(msg)
 * ```
 *
 * @throws IllegalArgumentException if [nOfRepetitions] is not strictly positive.
 */
data class PeriodicMessage(
    val delayBetweenStrings: Duration,
    val nOfRepetitions: Int,
    val strings: List<String>,
) {
    init {
        require(nOfRepetitions > 0)
    }
}

private val taskQueue = PriorityQueue<Pair<Instant, Continuation<Unit>>>(
    compareBy { it.first } // Sort by execution time
)

private object Continuation : Continuation<Unit> {
    override val context: CoroutineContext = EmptyCoroutineContext

    override fun resumeWith(result: Result<Unit>) {}
}

/**
 * Schedules and executes the display of multiple [PeriodicMessage]s using manual
 * coroutine suspension and a time-based priority queue.
 *
 * This function does not use real concurrency or structured coroutine dispatching.
 * Instead, it simulates asynchronous behavior by manually suspending and resuming
 * coroutines at the appropriate future [Instant] using a global queue of continuations.
 *
 * The calling thread is blocked until all periodic messages have finished execution.
 *
 * @param periodicMessages A vararg list of [PeriodicMessage] instances to be displayed.
 */
fun show(vararg periodicMessages: PeriodicMessage) {
    periodicMessages.forEach {
        ::showPeriodicMessage.startCoroutine(it, Continuation)
    }
    while(taskQueue.isNotEmpty()) {
        val (executionTime, _) = taskQueue.peek()
        val now = Instant.now()
        if(now.isBefore(executionTime)){
            val sleepDuration = executionTime.toEpochMilli() - now.toEpochMilli()
            Thread.sleep(sleepDuration)
        }
        taskQueue.poll().second.resumeWith(Result.success(Unit))
    }
}

/**
 * Displays the contents of a [PeriodicMessage], repeating the message list
 * the specified number of times, and waiting the given delay between each string.
 *
 * This function suspends between each message using the [sleep] function, which simulates
 * coroutine delay by scheduling the continuation in a priority queue.
 *
 * @param msg The message configuration to execute.
 */
suspend fun showPeriodicMessage(msg: PeriodicMessage) {
    repeat(msg.nOfRepetitions) {
        msg.strings.forEach { string ->
            println(string)
            sleep(msg.delayBetweenStrings)
        }
    }
}

/**
 * Suspends the current coroutine for the specified [duration] by enqueuing
 * its continuation in a priority queue ordered by resume time.
 *
 * This is a custom scheduling mechanism that mimics coroutine delay behavior
 * without relying on kotlinx.coroutines. Continuations are resumed by [show]
 * when their scheduled execution time is reached.
 *
 * @param duration The duration for which the coroutine should be suspended.
 */

suspend fun sleep(duration: Duration) {
    suspendCoroutine { continuation ->
        val executionTime = Instant.now().plusMillis(duration.inWholeMilliseconds)
        taskQueue.add(executionTime to continuation)
    }
}

fun main() {
    val periodicMessage1 = PeriodicMessage(
        delayBetweenStrings = 1.seconds,
        nOfRepetitions = 3,
        strings = listOf("Message 1.1", "Message 1.2", "Message 1.3")
    )

    val periodicMessage2 = PeriodicMessage(
        delayBetweenStrings = 2.seconds,
        nOfRepetitions = 2,
        strings = listOf("Message 2.1", "Message 2.2")
    )

    val periodicMessage3 = PeriodicMessage(
        delayBetweenStrings = 3.seconds,
        nOfRepetitions = 1,
        strings = listOf("Message 3.1")
    )

    println("Starting periodic messages...")
    show(periodicMessage1, periodicMessage2, periodicMessage3)
    println("All periodic messages have been shown.")
}