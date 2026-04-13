package serie2.part2

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class plusOnTwoListsTest {
    // Test sum of two empty lists
    @Test
    fun testEmptyLists(){
        val l1 = emptyList<Int>()
        plusOnTwoLists(l1, emptyList())
        assertTrue( l1.isEmpty() )
    }

    // Test sum of an empty list with a list with one element
    @Test
    fun testEmptyListWithOneElement(){
        val l1 = emptyList<Int>()
        plusOnTwoLists(l1, singletonList(1))
        assertTrue( l1.isEmpty() )
    }

    // Test sum of a list with one element with an empty list
    @Test
    fun testOneElementWithEmptyList(){
        val l1 = singletonList(1)
        plusOnTwoLists(l1, emptyList())
        assertEquals( listOf(1), l1.toList() )
    }

    // Test sum of two lists with one element without carry
    @Test
    fun testOneElementWithOneElement(){
        val l1 = singletonList(1)
        plusOnTwoLists(l1, singletonList(1))
        assertEquals( listOf(2), l1.toList() )
    }

    // Test sum of two lists with two elements without carry
    @Test
    fun testTwoElementsWithTwoElements(){
        val l1 = listOf(1, 2).toLinkedList()
        plusOnTwoLists(l1, listOf(3, 2).toLinkedList())
        assertEquals( listOf(4, 4), l1.toList() )
    }

    // Test sum of two lists with two elements with carry
    @Test
    fun testTwoElementsWithTwoElementsWithCarry(){
        val l1 = listOf(0, 2).toLinkedList()
        plusOnTwoLists(l1, listOf(0, 9).toLinkedList())
        assertEquals(listOf(1, 1), l1.toList() )
        // l1 now is {1, 1 } sum of l1 and l2 with carry
        plusOnTwoLists(l1, listOf(8, 9).toLinkedList())
        assertEquals( listOf(0, 0), l1.toList() )
    }

    // Test sum of two lists with differents sizes (l1 is larger)
    @Test
    fun testLargerSize(){
        val l1 = listOf(9, 2, 3).toLinkedList()
        val l2 = listOf(7, 6).toLinkedList()
        plusOnTwoLists(l1, l2)
        assertEquals( listOf(9, 9, 9), l1.toList() )
        // l1 now is {9, 9, 9} - sum of l1 and l2 with carry
        plusOnTwoLists(l1, singletonList(1))
        assertEquals( listOf(0, 0, 0), l1.toList() )
    }

    // Test sum to the list itself
    @Test
    fun testToItself(){
        val l1 = listOf(3, 6).toLinkedList()
        plusOnTwoLists(l1, l1)
        assertEquals( listOf(7, 2), l1.toList() )
        // l1 now is {7, 2} - sum  with carry
        plusOnTwoLists(l1, l1)
        assertEquals( listOf(4, 4), l1.toList() )
    }
}