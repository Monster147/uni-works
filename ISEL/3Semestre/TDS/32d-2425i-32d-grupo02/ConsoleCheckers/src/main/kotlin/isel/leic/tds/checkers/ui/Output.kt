package isel.leic.tds.checkers.ui
import isel.leic.tds.checkers.model.*

// Função para mostrar a disputa, que em si, mostra o jogo
fun Clash.show() {
    val clash = this as? ClashRun ?: return
    println("Clash: ${clash.name} you are ${clash.sidePlayer}")
    clash.game.show()
}

// Função para mostrar o jogo, que em si, mostra o tabuleiro
fun Game.show() = board?.show()

// Função para mostrar a ponutação do jogo
fun Game.showScore() {
    (Player.entries+null).forEach { player ->
        println("${player?:"Draw"} = ${score[player]}")
    }
}

// Função para mostrar o tabuleiro no output
fun Board.show() {
        writeLine()
        for (row in BOARD_DIM downTo 1) {
            print("$row | ")
            for (col in 'a'..<('a' + BOARD_DIM)) {
                val sqr = "$row$col".toSquareOrNull()
                checkNotNull(sqr) { "Your board was not drawn correctly." }
                print(
                    when (val piece = this.places.values.find { it.pos == sqr }) {
                        null -> if (sqr.black) "- " else "  "
                        else -> "${piece.symbol} "
                    }
                )
            }
            print("|")
            if (row != BOARD_DIM && row != BOARD_DIM - 1) {
                println()
            }

            if (row == BOARD_DIM) {
                println(when (this) {
                    is BoardPlaying -> " Current player = ${currPlayer.symbol}"
                    is WinnerBoard -> " Winner: ${winner.symbol}"
                    is DrawBoard -> " Draw"
                })
            }

            if (row == BOARD_DIM - 1) {
                println(when (this) {
                    is BoardPlaying -> " Turn = ${currPlayer.symbol}"
                    else -> " "
                })
            }
        }
        writeLine()
        writeLetter()
    }

// Função para escrever linhas delimitadores do tabuleiro
fun writeLine() {
        val spaces = 2
        print(writeSpaces(spaces))
        print("+")
        repeat(BOARD_DIM) {
            print("--")
        }
        println("-+")
    }

// Função para escrever espaços
fun writeSpaces(n: Int) = " ".repeat(n)

// Função para escrever as letras para cada coluna
fun writeLetter() {
    // Cria uma lista de letras que começam em 'A' até 'A' + (BOARD_DIM-1)
    val letterList = ('A'..'A' + (BOARD_DIM - 1)).toList()
    val spaceBetween = 1
    print(writeSpaces(4))

    // Loop pelas colunas através de BOARD_DIM
    for (col in 0 ..< BOARD_DIM) {
        print(letterList[col])
        print(writeSpaces(spaceBetween))
    }
    println(writeSpaces(spaceBetween))
}
