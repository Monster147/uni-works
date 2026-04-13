package pt.isel.pc.assignment2

import java.io.OutputStream
import java.io.PrintStream
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

class PeriodicMessageTest {
    @Test
    fun `show prints messages in order with correct repetitions`() {
        val output = mutableListOf<String>()
        val originalOut = System.out
        System.setOut(PrintStream(object : OutputStream() {
            private val buffer = StringBuilder()
            override fun write(b: Int) {
                if (b == '\n'.code) {
                    output.add(buffer.toString())
                    buffer.clear()
                } else {
                    buffer.append(b.toChar())
                }
            }
        }))
        val msg = PeriodicMessage(
            delayBetweenStrings = 1.milliseconds,
            nOfRepetitions = 1,
            strings = listOf("A", "B")
        )
        show(msg)
        System.setOut(originalOut) // Restore
        assertEquals(listOf("A", "B"), output.map { it.trim() })
    }

    @Test
    fun `show prints messages in order and repeats`() {
        val output = Collections.synchronizedList(mutableListOf<String>())
        val originalOut = System.out
        System.setOut(PrintStream(object : OutputStream() {
            private val buffer = StringBuilder()
            override fun write(b: Int) {
                if (b == '\n'.code) {
                    output.add(buffer.toString())
                    buffer.clear()
                } else {
                    buffer.append(b.toChar())
                }
            }
        }))
        val msg = PeriodicMessage(
            delayBetweenStrings = 1.milliseconds,
            nOfRepetitions = 10,
            strings = listOf("A", "B")
        )
        show(msg)
        System.setOut(originalOut)
        val expected = List(10) { listOf("A", "B") }.flatten()
        assertEquals(expected, output.map { it.trim() })
    }

    @Test
    fun `periodic message stress test`() {
        val output = Collections.synchronizedList(mutableListOf<String>())
        val originalOut = System.out
        System.setOut(PrintStream(object : OutputStream() {
            private val buffer = StringBuilder()
            override fun write(b: Int) {
                if (b == '\n'.code) {
                    output.add(buffer.toString())
                    buffer.clear()
                } else {
                    buffer.append(b.toChar())
                }
            }
        }))
        val nMessages = 10
        val nReps = 100
        val nStrings = 10
        for (msgIx in 0 until nMessages) {
            val msg = PeriodicMessage(
                delayBetweenStrings = 1.milliseconds,
                nOfRepetitions = nReps,
                strings = List(nStrings) { "M${msgIx}_S$it" }
            )
            show(msg)
        }
        System.setOut(originalOut)
        for (msgIx in 0 until nMessages) {
            val expected = List(nReps) { List(nStrings) { "M${msgIx}_S$it" } }.flatten()
            val actual = output.filter { it.startsWith("M${msgIx}_") }.map { it.trim() }
            assertEquals(expected, actual, "Mismatch for message group $msgIx")
        }
    }
}