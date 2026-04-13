package serie2.part2


class Node<T> (
    var value: T = Any() as T,
    var next: Node<T>? = null,
    var previous: Node<T>? = null) {
}

fun plusOnTwoLists(list1: Node<Int>, list2: Node<Int>) {
    var n = 0
    var node1: Node<Int>? = list1.previous
    var node2: Node<Int>? = list2.previous
    while (node1 != list1 && node2 != list2) {
        val sum = node1!!.value + node2!!.value + n
        val digit = sum % 10
        n = sum / 10
        node1.value=digit
        node1 = node1.previous
        node2 = node2.previous
    }
    while(list1!=node1 /*&& list2==node2*/) {
        val sum = node1!!.value  + n
        val digit = sum % 10
        n = sum / 10
        node1.value = digit
        node1 = node1.previous
    }

}

fun <T> removeNonIncresing(list: Node<T>, cmp: Comparator<T>) {
    var currVal = list.next
    var currNode: Node<T>? = list
    while(currVal != null){
        if(cmp.compare(currNode!!.value, currVal.value) >= 0){
            while(currVal!=null && cmp.compare(currNode.value, currVal.value)>=0){
                currVal = currVal.next
            }
            currNode.next = currVal
            currVal?.previous = currNode
        }
        currNode = currVal
        currVal = currNode?.next
    }
}