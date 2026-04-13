package isel.leic.tds.checkers.model

import isel.leic.tds.checkers.ui.*
import isel.leic.tds.storage.Storage

@JvmInline
value class Name(private val value: String) {
    init {
        require(isValid(value)) { "Name not valid" }
    }
    override fun toString() = value
    companion object {
        fun isValid(txt: String) =
            txt.isNotEmpty() && txt.all { it.isLetterOrDigit() }
    }
}


typealias GameStorage = Storage<Name, Game>

open class Clash(val storage: GameStorage)

class ClashRun(
    storage: GameStorage,
    val name: Name, val game: Game, val sidePlayer: Player
) : Clash(storage)


private fun Clash.deleteIfOwner() {
    if (this is ClashRun && sidePlayer==Player.WHITE)
        storage.delete(name)
}

/*
// Função para começar um jogo
fun Clash.start(name: Name): Clash =
    ClashRun(storage, name, Game().newBoard(), Player.WHITE)
    .also { storage.create(name, it.game) }
*/

fun Clash.start(name: Name): Clash =
    ClashRun(storage, name, Game().newBoard(), Player.WHITE)
        .also {
            deleteIfOwner()
            storage.create(name, it.game)
        }

class ClashNotFound(name: Name): IllegalStateException("Clash $name not found")

private fun Clash.read(name: Name) =
    storage.read(name) ?: throw ClashNotFound(name)

/*
// Função para juntar-se a um jogo
fun Clash.join(name: Name): Clash = ClashRun(
    storage, name,
    game = checkNotNull(storage.read(name)) { "Clash $name not found" },
    sidePlayer = Player.BLACK
)*/

fun Clash.join(name: Name): Clash =
    ClashRun(storage, name, read(name), Player.BLACK)
        .also { deleteIfOwner() }

fun Clash.exit() {
    deleteIfOwner()
}

/**
 * Utility function to ensure the clash is a ClashRun and
 * return a new ClashRun with the game returned by the function getGame.
 * The getGame function is an extension of ClashRun.
 */
private fun Clash.onClashRun(getGame: ClashRun.() -> Game): Clash {
    check(this is ClashRun) { "There is no clash yet" }
    return ClashRun(storage, name, getGame(), sidePlayer)
}

class NoChangeException(name: Name): IllegalStateException("No changes in clash $name")

/*
// Função para obter o novo tabuleiro
fun Clash.refresh() = onClashRun {
    checkNotNull(storage.read(name))
    .also { check(it != game) { "No changes in clash $name" } }
}*/

fun Clash.refresh() = onClashRun {
    read(name).also { if(it == game) throw NoChangeException(name) }
}


// Função para mostrar o tabuleiro local
fun Clash.grid() = onClashRun {
    game
}

/*
// Função que posibilita jogar um jogo
fun Clash.play(from: Square, to: Square) = onClashRun {
    game.play(from, to)
    .also {
        check((game.board as? BoardPlaying)?.currPlayer == sidePlayer) { "Not your turn" }
        storage.update(name, it)
    }
}*/

fun Clash.play(from: Square, to: Square) = onClashRun {
    check((game.board as? BoardPlaying)?.currPlayer == sidePlayer) { "Not your turn" }
    game.play(from, to).also { storage.update(name, it) }
}

// Função para criar um novo tabuleiro de jogo
fun Clash.newBoard() = onClashRun {
    game.newBoard()
    .also { storage.update(name, it) }
}

val Clash.isSideTurn: Boolean get() = this is ClashRun &&
    sidePlayer == when (game.board) {
        is BoardPlaying -> game.board.currPlayer
        else -> game.firstPlayer
    }

