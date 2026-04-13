package serie1.part1

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

import java.util.*

class CountInRangeTest {
    @Test
    fun countInRange_onAnEmptyArray_returns0() {
        val v = Array<Int>(0){0}
        val res = countInRange(v, 0, -1, Int.MIN_VALUE, Int.MAX_VALUE)
        assertEquals(0, res)
    }

    @Test
    fun countInRange_withFullRange_returnsArrayLengthx() {
        val r = Random()
        val v = Array<Int>(100){r.nextInt()}
        v.sort()
        val res = countInRange(v, 0, v.size - 1, Int.MIN_VALUE, Int.MAX_VALUE)
        assertEquals(v.size, res)
    }

    @Test
    fun countInRange_withRepeatedValues_returnsCorrectedValue() {
        val v = arrayOf(0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2)
        val res = countInRange(v, 0, v.size - 1, 1, 1)
        assertEquals(7, res)
    }

    @Test
    fun countInRange_withNoValuesInLimits() {
        val v = arrayOf(0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 5, 5, 5, 6, 6, 6)
        val res = countInRange(v, 0, v.size - 1, 2, 3)
        assertEquals(0, res)
    }
}