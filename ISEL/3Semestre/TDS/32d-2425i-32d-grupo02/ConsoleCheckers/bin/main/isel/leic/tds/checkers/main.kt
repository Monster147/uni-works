
package isel.leic.tds.checkers
import isel.leic.tds.checkers.model.*
import isel.leic.tds.checkers.ui.*
import isel.leic.tds.storage.*

fun main() {
    var clash = Clash(TextFileStorage("games", GameSerializer))
    val cmds = getCommands()
    while (true) {
        val (name, args) = readCommand()
        val cmd = cmds[name]
        if (cmd==null) println("Invalid command $name")
        else try {
            clash = cmd.execute(args, clash)
            if (cmd.isTerminate) break
            clash.show()
        } catch (e : IllegalArgumentException) {
            println("${e.message}. Use: $name ${cmd.syntax}")
        } catch (e : IllegalStateException) {
            println(e.message)
        }
    }
}
