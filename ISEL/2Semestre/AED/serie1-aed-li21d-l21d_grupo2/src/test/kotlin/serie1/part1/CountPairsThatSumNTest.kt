package serie1

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import serie1.part1.countPairsThatSumN

class CountPairsThatSumNTest {

    @Test
    fun test_countPairsThatSumN_in_empty_subsequence() {
        val empty = intArrayOf()
        assertEquals(0, countPairsThatSumN(empty, 0, empty.size - 1, 0))
    }

    @Test
    fun test_countPairsThatSumN_in_singleton_subsequence() {
        val empty = intArrayOf(1)
        assertEquals(0, countPairsThatSumN(empty, 0, empty.size - 1, 1))
    }

    @Test
    fun countPairsThatSumN_in_consecutive_subsequence() {
        val array = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
        assertEquals(5, countPairsThatSumN(array, 0, array.size - 1, 12))
        assertEquals(4, countPairsThatSumN(array, 1, array.size - 1, 12))
    }

    @Test
    fun countPairsThatSumN_in_sortNonConsecutive_subsequence() {
        val array = intArrayOf(1, 2, 4, 7, 8, 9, 11, 12, 15, 17)
        assertEquals(2, countPairsThatSumN(array, 0, array.size - 1, 12))
        assertEquals(1, countPairsThatSumN(array, 1, array.size - 2, 12))
    }

}

