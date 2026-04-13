package serie2.part1

import kotlin.random.Random

fun <T> kElement(a: Array<T>, l:Int, r: Int, n:Int, comp: (T, T)-> Int ) : T {
    if(n>a.size) throw IllegalArgumentException()
    if(a.size==0) throw IllegalArgumentException()
    if (l==r) return a[l]
    val q= partition(a, l,r, comp)
    val k= q-l+1
    if(n==k) return a[q]
    return if (n<k) kElement(a,l,q-1,n,comp)
        else  kElement(a,q+1,r,n-k,comp)
}
fun <T> exchange(v:Array<T>,i:Int, j:Int){
    val temp = v[i]
    v[i]=v[j]
    v[j]= temp
}

fun <T> partition(a:Array<T>,l:Int,r:Int,comp: (T, T)-> Int):Int{
    val x = a[r]
    var j = l-1
    for(i in l until r){
        if (comp(a[i], x)<=0) {
            j++
            exchange(a, j,i)
        }
    }
    exchange(a, j+1, r)
    return j+1
}

fun evaluateRPN(exp: String): Int {
    if (exp.isBlank()) return 0
    val expListForm = exp.split(' ')
    val stack = mutableListOf<Int>()
    val operations = "+*-/"
    for (i in expListForm) {
        if (i.toIntOrNull() != null) stack += i.toInt()
        else {
            if (i in operations) {
                if (stack.size < 2) throw IllegalArgumentException()
                val operand2 = stack.removeLast()
                val operand1 = stack.removeLast()
                when (i) {
                    "+" -> stack += operand1 + operand2
                    "-" -> stack += operand1 - operand2
                    "*" -> stack += operand1 * operand2
                    "/" -> if (operand2 != 0) stack += operand1 / operand2 else throw ArithmeticException()
                }
            } else throw NumberFormatException()
        }
    }
    if(stack.size!= 1) throw IllegalArgumentException()
    return stack.last()
}