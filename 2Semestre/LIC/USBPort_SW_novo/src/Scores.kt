import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
object Scores {
    val SCORELIST = File("scores.txt")
    val PUTSCORE = BufferedWriter(FileWriter(SCORELIST, true))
    val LEADERBOARDSIZE = 20
    var GAMESCORES = strListToPairList("scores.txt")

    // Função que vai buscar uma lista de strings de cada linha do ficheiro
    // e associa o nome à pontuação e retira os 20 primeiros
    fun strListToPairList(file:String): List<Pair<String, Int>>{
        val list = FileAcess.readFromFile(file)
        return list.map{
            val parts = it.split("-")
            val name = parts[0]
            val score = parts[1].toInt()
            name to score
        }.sortedByDescending { it.second }.take(LEADERBOARDSIZE)
    }
}

fun main(){
    println(Scores.strListToPairList("scores.txt"))
}

