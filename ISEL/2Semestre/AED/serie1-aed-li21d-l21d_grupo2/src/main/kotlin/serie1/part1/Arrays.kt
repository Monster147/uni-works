package serie1.part1

fun countPairsThatSumN(v: IntArray, l: Int, r: Int, s: Int): Int {
    var n = 0
    var left = l
    var right = r
    var count = 0
    if(v.isEmpty()) return 0
    while(left!=right){
        count = v[left] + v[right]
        if (count < s) left++
        if (count > s) right--
        if(count == s) {
            n++
            left++
            right--
        }
    }
    return n
}

/*2.1 - n^3*/
fun countEachThreeElementsThatSumN(v:IntArray,l:Int,r:Int,s:Int):Int {
    var count = 0
    for(i in l until r-1){
        for(j in i+1 until r){
            for(k in j+1 until r+1){
                val sum=v[i]+v[j]+v[k]
                if(sum==s)count++
            }
        }
    }
    return count
}

/*2.2 - n^2 log n
fun countEachThreeElementsThatSumN(v:IntArray,l:Int,r:Int,s:Int):Int {
    var count = 0
    mergeSort(v,l,r)
    for (i in l .. r - 1){
        for (j in i + 1 .. r){
            val k = s - v[i] - v[j]
            val bs = binarySearch(v, j + 1, r, k)
            if (bs != -1) count++
        }
    }
    return count
}*/

/*2.3 - n^2
fun countEachThreeElementsThatSumN(v:IntArray,l:Int,r:Int,s:Int):Int {
    mergeSort(v,l,r)
    var count = 0
    for (i in l until r-1) {
        var left = i + 1
        var right = r
        while (left < right) {
            if (i > l && v[i] == v[right-1]) break
            val sum = v[i] + v[left] + v[right]
            if (sum < s) left++
            if (sum > s) right--
            if (sum == s) {
                count++
                left++
                right--
            }
        }
    }
    return count
}*/


fun merge(a:IntArray, left:Int,right:Int,b:IntArray,c:IntArray){
    var iB = 0
    var iC = 0
    var iA = left
    while(iB < b.size && iC< c.size){
        if(b[iB] <= c[iC]){
            a[iA] = b[iB]
            iB++
        }
        else{
            a[iA] = c[iC]
            iC++
        }
        iA++
    }
    while(iB<b.size) a[iA++] = b[iB++]
    while(iC<c.size) a[iA++] = c[iC++]
}

fun mergeSort(a:IntArray, l:Int, r:Int){
    if(l<r){
        val mid = (l+r)/2
        mergeSort(a,l,mid)
        mergeSort(a,mid+1,r)
        merge(a,l,r,mid)
    }
}

private fun merge(a:IntArray, left:Int, right:Int,mid:Int){
    val b = IntArray(mid-left+1)
    val c = IntArray(right-mid)
    for(i in b.indices){
        b[i] = a[i+left]
    }
    for(i in c.indices){
        c[i] = a[i+mid+1]
    }
    merge(a,left,right,b,c)
}
fun insertionSort(a:IntArray, l: Int, r: Int){
    var v= 0
    for(i in l+1..r){
        v = a[i]
        var j = i
        while(j > l && v < a[j-1]){
            a[j] = a[j-1]
            j--
        }
        a[j] = v
    }
}

fun binarySearch(a: IntArray, l: Int, r: Int, elem: Int): Int {
    if(r < l) return -1
    val mid = (l+r)/2
    return when {
        a[mid] == elem  -> mid
        a[mid] > elem   -> binarySearch(a, l, mid - 1, elem)
        else            -> binarySearch(a, mid+1, r, elem)
    }
}

fun countInRange(v: Array<Int>, l: Int, r: Int, min: Int, max: Int): Int {
    if(v.isEmpty()) return 0
    val min = lowerbound(v, l, r, min)
    val max = upperbound(v, l, r, max)
    return if (min >= 0 && max >= 0) max - min else min - max
}

fun lowerbound(array:Array<Int>, left:Int, right:Int, element:Int):Int{
    var l = left
    var r = right
    while(l <= r){
        val mid = (l+r)/2
        if(array[mid] >= element) r = mid - 1 else l = mid + 1
    }
    return l
}

fun upperbound(array:Array<Int>, left:Int, right:Int, element:Int):Int{
    var l = left
    var r = right
    while(l <= r){
        val mid = (l+r)/2
        if(array[mid] <= element) l = mid + 1 else r = mid - 1
    }
    return l
}