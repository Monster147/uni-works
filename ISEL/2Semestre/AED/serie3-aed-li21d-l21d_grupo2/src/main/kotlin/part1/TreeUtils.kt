package part1

data class Node<E>(var item: E, var left:Node<E>?, var right:Node<E>?)

fun printIf(root:Node<Int>?, predicate: (Int) -> Boolean ):Int {
    if (root == null) return 0
    var count = 0
    if (predicate(root.item)) count++
    count += printIf(root.left, predicate)
    count += printIf(root.right, predicate)
    return count
}

fun countBetween(root: Node<Int>?, min: Int, max: Int): Int {
    if (root == null) return 0
    if (root.item < min) return countBetween(root.right, min, max)
    return if (root.item > max) countBetween(root.left, min, max) else 1 + countBetween(root.right, min, max ) + countBetween(root.left, min, max)
}

fun isChildrenSum(root: Node<Int>?): Boolean {
    if (root == null || (root.left == null && root.right == null))
        return true
    else {
        var sum = 0
        sum = root.left?.item ?: 0
        sum += root.right?.item ?: 0
        return (sum == root.item) && isChildrenSum(root.left) && isChildrenSum(root.right)
    }
}










