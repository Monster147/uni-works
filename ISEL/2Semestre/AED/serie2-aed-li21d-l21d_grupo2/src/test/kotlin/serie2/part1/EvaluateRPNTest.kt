package serie2.part1

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EvaluateRPNTest {
    @Test
    fun calculate_empty_String() {
        assertEquals(0, evaluateRPN(""))
    }

    @Test
    fun calculate_spaces_String() {
        assertEquals(0, evaluateRPN("       "))
    }

    @Test
    fun calculate_only_a_single_operand() {
        assertEquals(0, evaluateRPN("0"))
        assertEquals(200, evaluateRPN("200"))
    }

    @Test
    fun calculate_only_operands() {
        assertFailsWith<IllegalArgumentException>{  evaluateRPN("1 2") }
        assertFailsWith<IllegalArgumentException>{  evaluateRPN("1 2 3") }
    }

    @Test
    fun calculate_only_operations() {
        assertFailsWith<IllegalArgumentException> { evaluateRPN("*") }
        assertFailsWith<IllegalArgumentException> { evaluateRPN("* +") }
        assertFailsWith<IllegalArgumentException> { evaluateRPN("* + -") }
    }

    @Test
    fun calculate_missing_operands() {
        assertFailsWith<IllegalArgumentException>  { evaluateRPN("1 *") }
        assertFailsWith<IllegalArgumentException>  { evaluateRPN("1 2 * +") }
        assertFailsWith<IllegalArgumentException>  { evaluateRPN("1 2 3 * + -") }
        assertFailsWith<IllegalArgumentException>  { evaluateRPN("1 2 * 3 4 + - + /") }
    }

    @Test
    fun calculate_divide_by_zero() {
        assertFailsWith<ArithmeticException> { evaluateRPN("1 0 /") }
        assertFailsWith<ArithmeticException> { evaluateRPN("4 2 2 - /") }
    }

    @Test
    fun calculate_illegal_expressions() {
        assertFailsWith<NumberFormatException> { evaluateRPN("1 a") }
        assertFailsWith<NumberFormatException> { evaluateRPN("1l 22 +") }
        assertFailsWith<NumberFormatException> { evaluateRPN("1 x 1 * + -") }
        assertFailsWith<NumberFormatException> { evaluateRPN("1 2 p 4 5 * + - /") }
    }

    @Test
    fun calculate_legal_expressions() {
        assertEquals(3, evaluateRPN("1 2 +"))
        assertEquals(6, evaluateRPN("1 2 3 + +"))
        assertEquals(-1, evaluateRPN("1 2 -"))
        assertEquals(1, evaluateRPN("2 1 -"))
        assertEquals(2, evaluateRPN("3 2 1 - -"))
        assertEquals(2, evaluateRPN("1 2 *"))
        assertEquals(6, evaluateRPN("1 2 3 * *"))
        assertEquals(0, evaluateRPN("1 2 /"))
        assertEquals(2, evaluateRPN("2 1 /"))
        assertEquals(2, evaluateRPN("4 2 1 / /"))
        assertEquals(6, evaluateRPN("1 3 4 * 2 / + 1 -"))
        assertEquals(2075, evaluateRPN("5 9 8 + 4 6 * * 7 + *"))
    }
}