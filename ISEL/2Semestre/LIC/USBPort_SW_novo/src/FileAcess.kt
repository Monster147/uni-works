import java.io.File
object FileAcess {

    //Função que lê de um ficheiro e mete as linhas numa lista
    fun readFromFile(file:String):List<String>{
        val list = File(file)
        return list.bufferedReader().readLines()
    }

}

fun main(){
    //println(FileAcess.readScoresFromFile("scores.txt"))
    //println(FileAcess.readStatsFromFile("statistics.txt"))
    val init = System.currentTimeMillis()
    println(FileAcess.readFromFile("scores.txt"))
    val end = System.currentTimeMillis()
    println(end-init)
}