package serie2.problema.implementacao1

import java.io.File

fun main(args: Array<String>){
    println("Diga o valor para k")
    val k = readln().toInt()
    //println("$k")
    val init= System.currentTimeMillis()
    val input = File(args[0]).readLines().first()
    val output = File("output.txt")
    //val kMers =  countKMers(input, k).entries.sortedBy { it.key }.map{"${it.key } - ${it.value}"}
    val kMers =  countKMers(input, k)
    for(i in kMers) {
        output.writeText("$i \n")
    }
    val end = System.currentTimeMillis()
    val diff = end - init
    println(kMers)
    println(diff)
}

fun countKMers(input:String, k:Int): Map<String, Int>{
    val map = mutableMapOf<String, Int>()
    for(i in 0 ..< input.length - k + 1){
        val kMer = input.substring(i, i+k)
        map[kMer] = map.getOrDefault(kMer, 0) +1
    }
    return map
}