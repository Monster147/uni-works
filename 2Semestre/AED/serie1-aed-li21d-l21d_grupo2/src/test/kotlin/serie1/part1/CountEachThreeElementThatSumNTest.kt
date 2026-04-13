package serie1

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import serie1.part1.countEachThreeElementsThatSumN

class CountEachThreeElementThatSumNTest {
    @Test
    fun test_countEachThreeElementThatSumTo_in_empty_subsequence() {
        val empty = intArrayOf()
        assertEquals(0, countEachThreeElementsThatSumN(empty, 0, empty.size - 1, 0))
    }

    @Test
    fun test_countEachThreeElementThatSumTo_in_singleton_subsequence() {
        val empty = intArrayOf(1)
        assertEquals(0, countEachThreeElementsThatSumN(empty, 0, empty.size - 1, 1))
    }

    @Test
    fun test_countEachThreeElementThatSumTo_in_unsortedConsecutive_subsequence() {
        val array1 = intArrayOf(5, 12, 3, 4, 9, 2, 1, 8, 7, 10, 6, 11)
        val array2 = intArrayOf(5, 12, 3, 4, 9, 2, 1, 8, 7, 10, 6, 11)
        assertEquals(7, countEachThreeElementsThatSumN(array1, 0, array1.size - 1, 12))
        assertEquals(5, countEachThreeElementsThatSumN(array2, 1, array2.size - 2, 12))
    }

    @Test
    fun test_countEachThreeElementThatSumTo_in_sortConsecutive_subsequence() {
        val array = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
        assertEquals(7, countEachThreeElementsThatSumN(array, 0, array.size - 1, 12))
        assertEquals(3, countEachThreeElementsThatSumN(array, 1, array.size - 2, 12))
    }

    @Test
    fun test_countEachThreeElementThatSumTo_in_unsortNonConsecutive_subsequence() {
        val array1 = intArrayOf(9, 17, 4, 7, 1, 15, 11, 12, 2, 8)
        val array2 = intArrayOf(9, 17, 4, 7, 1, 15, 11, 12, 2, 8)
        assertEquals(2, countEachThreeElementsThatSumN(array1, 0, array1.size - 1, 12))
        assertEquals(1, countEachThreeElementsThatSumN(array2, 1, array2.size - 2, 12))
    }

    @Test
    fun test_countEachThreeElementThatSumTo_in_sortNonConsecutive_subsequence() {
        val array = intArrayOf(1, 2, 4, 7, 8, 9, 11, 12, 15, 17)
        assertEquals(2, countEachThreeElementsThatSumN(array, 0, array.size - 1, 12))
        assertEquals(0, countEachThreeElementsThatSumN(array, 1, array.size - 2, 12))
    }
}

