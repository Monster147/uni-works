import isel.leic.utils.Time
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

object Statistics {
    var GAMESCOUNT = 0
    var NUMBERCOINS = 0
    val STATSLIST = File("statistics.txt")
    val PUTSTATS = BufferedWriter(FileWriter(STATSLIST, true))

    // Função que escreve 0's, depois de ter sido apagado o conteúdo do ficheiro
    fun clearStats(){
        PUTSTATS.write("0\n")
        PUTSTATS.write("0\n")
        PUTSTATS.close()
    }

    // Função que recebe a lista e atualiza as variáveis dos jogos e das moedas
    // segundo o que recebeu da lista
    fun getStats(){
        val list = FileAcess.readFromFile("statistics.txt")
        if(list[0].toInt() != 0) {
            if(GAMESCOUNT > list[0].toInt()) GAMESCOUNT
            else GAMESCOUNT = list[0].toIntOrNull() ?: 0
        }
        if(list[1].toInt() != 0)
            if(NUMBERCOINS > list[0].toInt()) NUMBERCOINS
            else NUMBERCOINS = list[0].toIntOrNull() ?: 0
    }
}

fun deleteFileContent(file: File) {
    file.printWriter().use { out -> out.print("") }
}

fun main(){
    //println(Statistics.STATS)
    /*deleteFileContent(Statistics.STATSLIST)
    Statistics.PUTSTATS.write("0")
    Statistics.PUTSTATS.newLine()
    Statistics.PUTSTATS.write("0")
    Statistics.PUTSTATS.flush()
    Time.sleep(500)
    println(Statistics.STATS)*/
    //println(Statistics.STATS)
    println(FileAcess.readFromFile("statistics.txt"))
    Statistics.getStats()
}