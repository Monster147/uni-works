/**
 * Represents the game action.
 */
enum class Action { WALK_LEFT, WALK_RIGHT, UP_STAIRS, DOWN_STAIRS, JUMP }

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

fun Game.stepFrame(): Game{
    if (man.vel.isZero() && !man.salto) return this
    val man = man.doSteps(this)
    return copy(man=man)
}

// TODO: implement this function

/*
A função "realiza" recebe um valor Game e uma Action retornando uma ação dependente do comando ativado.
Por exemplo: o Homem anda para a direita se WALK_RIGHT for a ação ativada
ou, como outro exemplo, ele salta se a ação executada for JUMP.
*/

fun Man.realiza(game:Game, algo:Action) = when(algo){
        Action.WALK_RIGHT -> andadir(game)
        Action.WALK_LEFT -> andaesq(game)
        Action.DOWN_STAIRS -> desceescada(game.stairs)
        Action.UP_STAIRS -> sobeescada(game.stairs)
        Action.JUMP -> salta(game)
    }


