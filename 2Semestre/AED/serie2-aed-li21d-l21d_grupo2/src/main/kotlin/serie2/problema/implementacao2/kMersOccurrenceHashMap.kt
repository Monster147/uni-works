package serie2.problema.implementacao2

import java.io.File

fun main(args: Array<String>){
    println("Diga o valor para k")
    val k = readln().toInt()
    //println("$k")
    val init= System.currentTimeMillis()
    val input = File(args[0]).readLines().first()
    val output = File("output.txt")
    // val kMers =  countKMers(input, k).entries.sortedBy { it.key }.map{"${it.key } - ${it.value}"}
    val kMers =  countKMers(input, k)
    for(i in kMers) {
        output.writeText("$i \n")
    }
    println(kMers)
    val end = System.currentTimeMillis()
    val diff = end - init
    println(diff)
}

fun countKMers(input:String, k:Int): HashMap<String, Int>{
    val map = HashMap<String, Int>()
    for(i in 0 ..< input.length - k + 1){
        val kMer = input.substring(i, i+k)
        val count = map[kMer]
        if(count == null){
            map.put(kMer, 1)
        }
        else{
            map.put(kMer, count+1)
        }
    }
    return map
}