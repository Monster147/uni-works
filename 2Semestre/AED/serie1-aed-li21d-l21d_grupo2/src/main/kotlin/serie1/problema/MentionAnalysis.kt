package serie1.problema

import java.io.BufferedReader
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

data class Twts(val created_at:String, val hst: List<String>, val id:Long,  val uid:Long)

data class Hastags(val hastag : String, var count : Int)

fun createReader(fileName: String): BufferedReader {
    return BufferedReader(FileReader(fileName))
}

fun readTWTS(fileName: String):List<Twts>{
    val listofTWTS = mutableListOf<Twts>()
    val read = createReader(fileName)
    read.use{t -> t.forEachLine {
                line -> val content = line.split("; ")
                        if(content.size == 4){
                            val creat = content[0].split(": ")[1].trim('\"')
                            val hsts = content[1].split(": ")[1].trim('[', ']', '\"').split(", ").map{it.trim('"')}
                            val id = content[2].split(": ")[1].toLong()
                            val uid = content[3].split(": ")[1].trim( '}', '\"').toLong()
                            listofTWTS.add(Twts(creat, hsts, id, uid))
                        }
                }
    }
    return listofTWTS
}

fun countMentions(twts:List<Twts>):Hastags{
    val Hsts = mutableListOf<Hastags>()
    val read = createReader("hastags.txt")
    read.use { h -> h.forEachLine { l ->
            var count = 0
            for(k in 0 until twts.size){
                if(twts[k].hst.contains(l)) count++
            }
            Hsts.add(Hastags(l, count))
        }
    }
    return Hsts.maxBy { H -> H.count } //Retorna o elemento com maior número no count
}

fun nearDate(twts: List<Twts>, n:Int, time:String): List<Twts>{
    val data = dateStringToSeconds(time)
    val datas = twts.map{it to abs(dateStringToSeconds(it.created_at) - data)}.toTypedArray()
    heapSort(datas, datas.size - 1)
    return datas.take(n).map{it.first}
}
fun dateStringToSeconds(dateString: String): Long {
    // Define the date format of your input string
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    // Set the timezone if necessary
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    try {
        // Parse the date string into a Date object
        val date = dateFormat.parse(dateString)
        // Calculate the number of milliseconds since the epoch (January 1, 1970, 00:00:00 UTC)
        val milliseconds = date.time
        // Convert milliseconds to seconds
        return milliseconds / 1000
    } catch (e: Exception) {
        // Handle parsing exceptions, if any
        e.printStackTrace()
    }
    // Return -1 if parsing fails
    return -1
}

fun left(i:Int)= (2*i) + 1
fun right(i:Int)= (2*i) + 2
fun parent(i:Int)=(i-1) / 2
fun exchange(array:Array<Pair<Twts, Long>>, i:Int, j:Int){
    val temp= array[i]
    array[i]=array[j]
    array[j]=temp
}

fun transform(array: Array<Pair<Twts, Long>>, n: Int){
    var s=n
    while(s>0){
        exchange(array,0,--s)
        maxHeapify(array,s,0)
    }
}
fun maxHeapify(array: Array<Pair<Twts, Long>>, n:Int, pos:Int){
    val l= left(pos)
    val r= right(pos)
    var pai = pos
    if(l<n && array[l].second>array[pai].second) pai = l
    if(r<n && array[n].second>array[pos].second) pai = r
    if(pai!=pos){
        exchange(array,pai,pos)
        maxHeapify(array,n,pai)
    }
}
fun buildMaxHeap(array: Array<Pair<Twts, Long>>, n: Int){
    var pai = parent(n-1)
    while (pai>=0){
        maxHeapify(array, n, pai)
        pai--
    }
}
fun heapSort(array: Array<Pair<Twts, Long>>, n: Int){
    buildMaxHeap(array,n)
    transform(array,n)
}
fun main(args: Array<String>){
    val funDirector = readln()
    when(funDirector){

        "countMentions" -> {
            val init= System.currentTimeMillis()
            println(countMentions(readTWTS(args[0])))
            val end = System.currentTimeMillis()
            val diff = end - init
            print(diff)
        }

        "nearDate" -> {
            val time = readln()
            val init= System.currentTimeMillis()
            nearDate(readTWTS(args[0]), args[1].toInt(), time)
            val end = System.currentTimeMillis()
            val diff = end - init
            print(diff)
        }
    }
}
