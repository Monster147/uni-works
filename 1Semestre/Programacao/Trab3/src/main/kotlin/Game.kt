/**
 * Represents the game action.
 */
enum class Action { WALK_LEFT, WALK_RIGHT, UP_STAIRS, DOWN_STAIRS, JUMP }
enum class Estado {EMJOGO, PARADO}

/**
 * Represents all game information.
 * @property man information about man
 * @property floor positions of floor cells
 * @property stairs positions of stairs cells
 */
data class Game(
    val man: Man,
    val floor: List<Cell>,
    val stairs: List<Cell>,
    val ovos: List<Cell>,
    val comida: List<Cell>,
    val pontuacao: Int,
    val tempo: Int,
    val victory:Boolean = false,
    val lose:Boolean = false,
    val state: Estado
)

/**
 * Loads a game from a file.
 * @param fileName the name of the file with the game information.
 * @return the game loaded.
 */
fun loadGame(fileName: String) : Game {
    val cells: List<CellContent> = loadLevel(fileName)
    return Game(
        man = createMan(cells.first { it.type == CellType.MAN }.cell),
        floor = cells.ofType(CellType.FLOOR),
        stairs = cells.ofType(CellType.STAIR),
        ovos = cells.ofType(CellType.EGG),
        comida = cells.ofType(CellType.FOOD),
        pontuacao = 0,
        tempo = 2666,
        state = Estado.EMJOGO
    )
}

/**
 * Performs an action to the game.
 * If the action is null, returns current game.
 * @param action the action to perform.
 * @receiver the current game.
 * @return the game after the action performed.
 */
fun Game.doAction(action: Action?): Game =
    if (action == null) this
    else copy(man = man.realiza(this, action))


// TODO: implement this function

/**
 * Computes the next game state.
 * If the man is stopped, returns current game.
 * @receiver the current game.
 * @return the game after the next frame.
 */
/*
Se o valor victory for verdadeiro, o valor da pontuação será aumentado com o tempo que ficou no contador,
após todos os ovos apanhados. Depois do incremento, o valor no contador fica a zero.
Quando o valor victory ou o valor lose forem verdadeiros, o Homem não se mexe mais.
*/
fun Game.stepFrame(): Game{
    if (victory == true) return copy(pontuacao = this.pontuacao + this.tempo, tempo = 0, state = Estado.PARADO)
    if (lose == true) return copy(state = Estado.PARADO)
    if (man.vel.isZero() && !man.salto) return this
    val man = man.doSteps(this)

    return copy(man=man)
}

/*
Função que controla a pontuação.
Existem dois objetos (ovo e comida) com uma certa pontuação.
Quando o Homem passa por um dos objetos, a pontuação aumenta com o valor do mesmo, retirando-o da arena.
Quando todos os ovos são apanhados, o valor victory passa a ser verdadeiro.
Se o tempo chega a zero, o valor lose fica verdadeiro.
 */
fun Game.pontosFrame():Game{
    return when{
        man.pos.toCell() in this.ovos -> copy(ovos = ovos - man.pos.toCell(), pontuacao =  this.pontuacao + 100)
        man.pos.toCell() in this.comida -> copy(comida = comida - man.pos.toCell(), pontuacao =  this.pontuacao + 50)
        this.ovos.isEmpty() -> copy(victory = true)
        tempo == 0 -> copy(lose = true)
        else -> this
    }
}

/*
Função que controla o decorrer do tempo. Vai sempre retirando um valor ao tempo a cada frame que passa.
Quando victory for true ou o lose for true ou o state mudar para o Estado.Parado, o contador congela.
 */
fun Game.tempoFrame():Game{
    return when{
        victory == false && lose == false && state == Estado.EMJOGO -> copy(tempo = tempo - 1)
        else -> this
    }
}

// TODO: implement this function

/*
A função "realiza" recebe um valor Game e uma Action retornando uma ação dependente do comando ativado.
Por exemplo: o Homem anda para a direita se WALK_RIGHT for a ação ativada
ou, como outro exemplo, ele salta se a ação executada for JUMP.
O homem só realiza estas ações se o estado do jogo for Estado.EMJOGO, ou seja, se o jogo ainda decorre.
*/

fun Man.realiza(game:Game, algo:Action):Man{
    return if (game.state == Estado.EMJOGO) {
        when (algo) {
            Action.WALK_RIGHT -> andadir(game)
            Action.WALK_LEFT -> andaesq(game)
            Action.DOWN_STAIRS -> desceescada(game.stairs)
            Action.UP_STAIRS -> sobeescada(game.stairs)
            Action.JUMP -> salta(game)
        }
    }
    else this
}


