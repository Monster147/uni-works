package serie2.part2

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class removeNonIncresingTest {
    // Test with one element
    @Test
    fun testOneElement(){
        val l = Node<Int>(1)
        removeNonIncresing(l, Int::compareTo)
        assertTrue( l.value == 1 )
        assertTrue( l.next == null )
    }

    // Test with a list of ten elements sorted in decrescent order.
    // Result should be a list with only the first element.
    @Test
    fun testDecrescent(){
        val elements = List(10) {10-it} // decrescent order (10, 9, 8, ..., 1)
        val l = listToLinked(elements)
        if (l != null) {
            removeNonIncresing(l) { a, b -> b.compareTo(a) } // reverse comparison
            assertEquals(elements, linkedToList(l))

            removeNonIncresing(l, Int::compareTo)            // natural comparison
            assertEquals(listOf(10), linkedToList(l))
        }
    }

    // Test with a list of ten elements sorted in crescent order.
    // Result should be a list with all elements.
    @Test
    fun testCrescent(){
        val elements = List(10) {it} // crescent order (0, 1, 2, ..., 9)
        val l = listToLinked(elements)
        if (l != null) {
            removeNonIncresing(l, Int::compareTo)   // natural comparison
            assertEquals( elements, linkedToList( l ))

            removeNonIncresing(l) { a, b -> b.compareTo(a) } // reverse comparison
            assertEquals( listOf(0), linkedToList(l) )
        }
    }

    // Remove the last elements of a list of five elements.
    // Only the first three elements should remain, because the
    // last are not greater than the greater element until
    // that point.
    @Test
    fun testRemoveLast(){
        val l = listToLinked( listOf(1,2,3,0,1) )
        if (l != null) {
            removeNonIncresing(l, Int::compareTo)
            assertEquals( listOf(1,2,3),  linkedToList( l ))
        }
    }

    //Remove elements in the middle of the list.
    // The result should be a list with the elements in crescent order.
    @Test
    fun testRemoveElementsNaturalOrder(){
        val l = listToLinked( listOf(0, 1, 0, 2, 2, 0, 3, 0) )
        if (l != null) {
            removeNonIncresing(l, Int::compareTo)
            assertEquals( listOf(0, 1, 2, 3), linkedToList(l))
        }
    }

    //Remove elements in the middle of the list.
    // The result should be a list with the elements in decrescent order,
    // because the comparison function is reversed.
    @Test
    fun testRemoveElementsReverseOrder(){
        val l = listToLinked( listOf(5, 3, 4, 2, 2, 0, 3, 0) )
        if (l != null) {
            removeNonIncresing(l){ a, b -> b.compareTo(a) }
            assertEquals( listOf(5, 3, 2, 0), linkedToList(l))
        }
    }
}