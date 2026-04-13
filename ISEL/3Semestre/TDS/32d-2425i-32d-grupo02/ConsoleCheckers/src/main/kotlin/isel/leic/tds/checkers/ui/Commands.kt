package isel.leic.tds.checkers.ui
import isel.leic.tds.checkers.model.*

class Command(
    val syntax: String = "",
    val isTerminate: Boolean = false,
    val execute: (args: List<String>, clash: Clash) -> Clash = { _, clash -> clash }
)

private val playCommand = Command("<fromPosition> <toPosition>") { args, clash ->
    require(args.isNotEmpty()) { "Missing position" }
    require(args.size == 2) {"You need to input two positions"}
    val from = requireNotNull(args[0].toSquareOrNull()) { "Illegal position ${args[0]}"}
    val to = requireNotNull(args[1].toSquareOrNull()) { "Illegal position ${args[1]}"}
    clash.play(from, to)
}

private fun commandNoArgs(clashFunction: Clash.()->Clash) = Command { _, clash -> clash.clashFunction() }

private fun commandWithName(clashFunction: Clash.(Name)->Clash) = Command("<name>") { args, clash ->
    require(args.isNotEmpty()) { "Missing name" }
    clash.clashFunction(Name(args[0]))
}

private val helpCommand = Command("") { _, clash ->
    println("Available Commands:")
    getCommands().filter { it.key != "HELP" }.forEach { (commandName, command) ->
        println("$commandName ${command.syntax}")
    }
    clash // Return the clash object unchanged
}

fun getCommands(): Map<String, Command> = mapOf(
    "EXIT" to Command(isTerminate = true),
    "NEW" to commandNoArgs(Clash::newBoard),
    "PLAY" to playCommand,
    "SCORE" to Command { _, clash -> clash.also { (it as? ClashRun)?.game?.showScore() } },
    "START" to commandWithName(Clash::start),
    "JOIN" to commandWithName(Clash::join),
    "REFRESH" to commandNoArgs(Clash::refresh),
    "GRID" to commandNoArgs(Clash::grid),
    "HELP" to helpCommand
)
