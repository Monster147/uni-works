package serie2.part1

import kotlin.test.*

class KElementTest {
    private val original = arrayOf(12, 1, 2, 3, 2, 5, 6, 3, 8, 10, 0)

    @Test
    fun test_NElement_with_empty_array() {
        assertFailsWith<IllegalArgumentException> {
            kElement(emptyArray(), 0, - 1, 1, Int::compareTo)
        }
        assertFailsWith<IllegalArgumentException> {
            kElement(emptyArray(), 0, - 1, 0, Int::compareTo)
        }
    }

    @Test
    fun test_NElement_with_one_element() {
        assertEquals(8, kElement(arrayOf(8), 0, 0, 1, Int::compareTo))
    }

    @Test
    fun test_NElement_at_the_begin() {
        assertEquals(0, kElement(original.copyOf(), 0, original.size - 1, 1, Int::compareTo))
        assertEquals(1,kElement(original.copyOf(), 0, original.size - 2, 1, Int::compareTo))
        assertEquals(12,kElement(original.copyOf(), 0, 0, 1, Int::compareTo))
    }

    @Test
    fun test_NElement_at_the_end() {
        assertEquals(12, kElement(original.copyOf(), 0, original.size - 1, original.size, Int::compareTo))
        assertEquals(10, kElement(original.copyOf(), 1, original.size - 1, original.size-1, Int::compareTo))
    }

    @Test
    fun test_NElement_at_the_middle() {
        assertEquals(5, kElement(original.copyOf(), 0, original.size - 1, 7, Int::compareTo))
        assertEquals(8, kElement(original.copyOf(), 3, original.size - 1, 7, Int::compareTo))
        assertEquals(10, kElement(original.copyOf(), 3, original.size - 2, 7, Int::compareTo))
    }

    @Test
    fun test_NElement_with_n_greater_than_array() {
        assertFailsWith<IllegalArgumentException> {
            kElement(original.copyOf(), 0, original.size - 1, 20, Int::compareTo)
        }
        assertFailsWith<IllegalArgumentException> {
            kElement(original.copyOf(), 0, original.size - 1, 20, Int::compareTo)
        }
    }
}