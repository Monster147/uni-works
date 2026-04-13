package isel.leic.tds.checkers.model

import isel.leic.tds.checkers.ui.show
import isel.leic.tds.storage.Storage

@JvmInline
value class Name(val value: String) {
    init {
        require(value.isNotEmpty() && value.all { it.isLetterOrDigit() }) { "Name not valid" }
    }
    override fun toString() = value
}

typealias GameStorage = Storage<Name, Game>

open class Clash(val storage: GameStorage)

class ClashRun(
    storage: GameStorage,
    val name: Name, val game: Game, val sidePlayer: Player
) : Clash(storage)

// Função para começar um jogo
fun Clash.start(name: Name): Clash =
    ClashRun(storage, name, Game().newBoard(), Player.WHITE)
    .also { storage.create(name, it.game) }


// Função para juntar-se a um jogo
fun Clash.join(name: Name): Clash = ClashRun(
    storage, name,
    game = checkNotNull(storage.read(name)) { "Clash $name not found" },
    sidePlayer = Player.BLACK
)

/**
 * Utility function to ensure the clash is a ClashRun and
 * return a new ClashRun with the game returned by the function getGame.
 * The getGame function is an extension of ClashRun.
 */
private fun Clash.onClashRun(getGame: ClashRun.() -> Game): Clash {
    check(this is ClashRun) { "There is no clash yet" }
    return ClashRun(storage, name, getGame(), sidePlayer)
}

// Função para obter o novo tabuleiro
fun Clash.refresh() = onClashRun {
    checkNotNull(storage.read(name))
    .also { check(it != game) { "No changes in clash $name" } }
}

// Função para mostrar o tabuleiro local
fun Clash.grid() = onClashRun {
    game
}

// Função que posibilita jogar um jogo
fun Clash.play(from: Square, to: Square) = onClashRun {
    game.play(from, to)
    .also {
        check((game.board as? BoardPlaying)?.currPlayer == sidePlayer) { "Not your turn" }
        storage.update(name, it)
    }
}

// Função para criar um novo tabuleiro de jogo
fun Clash.newBoard() = onClashRun {
    game.newBoard()
    .also { storage.update(name, it) }
}


