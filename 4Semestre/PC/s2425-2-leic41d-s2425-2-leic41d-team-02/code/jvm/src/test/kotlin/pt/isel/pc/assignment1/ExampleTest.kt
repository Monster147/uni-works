package pt.isel.pc.assignment1

import kotlin.test.Test
import kotlin.test.assertEquals

class ExampleTest {
    @Test
    fun `example test`() {
        // given: two numbers
        val x = 1
        val y = 2

        // when: adding those two numbers
        val result = x + y

        // then: the result is the expected one
        assertEquals(3, result)
    }
}
