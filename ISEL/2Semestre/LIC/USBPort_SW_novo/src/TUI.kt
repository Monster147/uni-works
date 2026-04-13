import isel.leic.utils.Time
import javax.script.Invocable
import javax.swing.table.TableColumn
import kotlin.random.Random

object TUI {
    const val NUMBERCOLUMS=16

    //Limpa a linha desde uma coluna inicial, initColumn, até uma coluna final, lastColumn
    fun clearLine(line: Int, initColumn: Int, lastColumn:Int) {
        for (i in initColumn until lastColumn) {
            LCD.cursor(line, i)
            LCD.write(' ')
        }
    }

    //Remove os invasores, escrevendo ' ' na sua posição
    private fun removeInvaders(line: Int) {
        for (i in 3..<NUMBERCOLUMS) {
            LCD.cursor(line, i)
            LCD.write(' ')
        }
    }

    //Remove os invasores, escrevendo ' ' na sua posição, e por sua vez escreve uma nova lista de invasores
    fun putInvaders(Invaders: MutableList<Char>, line: Int) {
        removeInvaders(line)
        for (i in 0..<Invaders.size) {
            val invaderPos = NUMBERCOLUMS - Invaders.size + i
            LCD.cursor(line, invaderPos)
            LCD.write(Invaders[i])
        }
    }

    //Adiciona os invasores, que são escolhidos aleatoriamente, ao LCD
    fun spawnInvaders(Invaders: MutableList<Char>, line: Int) :String {
        val Invader = Random.nextInt(10).toString()
        Invaders.add(Invader.first())
        putInvaders(Invaders, line)
        return Invaders.joinToString("")
    }

    //Dispõe no LCD quantos créditos temos
    fun coinDisplay(coin: Int) {
        val coinShow = "$coin"
        LCD.cursor(1, NUMBERCOLUMS - 1 - coinShow.length)
        LCD.write("$$coinShow")
    }

    //Escreve a inicialzação para o modo de jogo
    fun start(line: Int) {
        LCD.clear()
        LCD.cursor(0, 0)
        LCD.write("_")
        LCD.cursor(1, 0)
        LCD.write("_")
        LCD.cursor(line, 1)
        LCD.write(">")
    }

    // Retorna de imediato a tecla premida ou NONE se não há tecla premida
    fun getKey() = KBD.getKey()

    // Retorna a tecla premida, caso ocorra antes do ‘timeout’ (representado em milissegundos), ou NONE caso contrário
    fun waitKey(timeout:Long) = KBD.waitKey(timeout)

    // Envia comando para posicionar cursor (‘line’:0..LINES-1 , ‘column’:0..COLS-1)
    fun cursor(line: Int, column: Int) = LCD.cursor(line, column)

    // Envia comando para limpar o ecrã e posicionar o cursor em (0,0)
    fun clear() = LCD.clear()

    // Escreve um caráter na posição corrente
    fun write(c:Char) = LCD.write(c)


    // Escreve uma string na posição corrente
    fun write(text: String) = LCD.write(text)

    // Escreve no LCD quando perdemos o jogo
    fun loseLCD(score:Int){
        LCD.clear()
        LCD.cursor(0, 0)
        LCD.write("*** You Died ***")
        Time.sleep(1)
        LCD.cursor(1, 0)
        LCD.write("Score: $score")
    }

    // Escreve no LCD, "Name:", para podermos escrever o nome e a pontuação
    fun userScore(score: Int){
        LCD.clear()
        LCD.cursor(1, 0)
        LCD.write("Score:$score")
        LCD.cursor(0, 0)
        LCD.write("Name:")
        LCD.write('A')
        LCD.cursor(0,5)
    }

    // Escreve no LCD o ecrã de manuntenção
    fun maintWrite(){
        LCD.cursor(0, 1)
        LCD.write("On Maintenance")
        LCD.cursor(1, 0)
        LCD.write("*-Count  #-ShutD")
    }

    //Inicializa as classes ao qual o TUI acede, inicializando também as classes abaixo daquelas a que ele acede
    fun init() {
        HAL.init()
        KBD.init()
        SerialEmitter.init()
        LCD.init()
    }

    // Escreve no LCD o ecrã de início
    fun begin(){
        LCD.cursor(0, 1)
        LCD.write("Space Invaders")
        LCD.cursor(1, 0)
        clearLine(1,0,2)
        LCD.write("LEIC21D G06")
        clearLine(1, 13, NUMBERCOLUMS)
        Time.sleep(800)
        LCD.cursor(1, 0)
        clearLine(1,0, NUMBERCOLUMS)
    }
}

fun main(){
    TUI.init()
    //TUI.begin()
    //TUI.maintWrite()
    //TUI.userScore(123)
    //TUI.loseLCD(123)
    //TUI.write('A')
    //TUI.write('l')
    //TUI.write("fe142318aq")
    /*TUI.cursor(4, 6)
    TUI.write("48746133fgrb")*/
    //TUI.clear()
    /*while(true){
        val k = TUI.getKey() //TUI.waitKey(10000)
        print(k)
    }*/
    //TUI.start(0)
    //TUI.coinDisplay(26)
    /*var spawnRate = 0
    while(spawnRate != 4){
        TUI.spawnInvaders(mutableListOf(), 0)
        spawnRate++
    }*/
    /*TUI.cursor(0,0)
    TUI.write("fe142318aq")
    TUI.clearLine(0, 0, 5)*/

}