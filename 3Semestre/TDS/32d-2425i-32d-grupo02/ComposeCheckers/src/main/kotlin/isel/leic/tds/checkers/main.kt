
package isel.leic.tds.checkers
import isel.leic.tds.checkers.model.*
import isel.leic.tds.checkers.ui.*
import isel.leic.tds.storage.*
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.*

@Composable
fun FrameWindowScope.CheckersMenuBar(vm: CheckersViewModel, onExit: () -> Unit) = MenuBar{
    Menu("Game", 'G') {
        Item("Start", onClick = vm::startClash)
        Item("Join", onClick = vm::joinClash)
        Item("New board", enabled = vm.isSideTurn, onClick = vm::newBoard)
        Item("Refresh", enabled = vm.hasClash, onClick = vm::refresh)
        Item("Score", enabled = vm.hasClash, onClick = vm::enableScore)
        Item("Exit", onClick = onExit)
    }
    Menu("Options", 'O'){
        CheckboxItem("Show Targets", checked = vm.showTargets,  onCheckedChange = { vm.showTargets() })
        CheckboxItem("Auto-Refresh", checked = vm.autoRefresh, onCheckedChange = { vm.autoRefresh() })
    }
}

@Composable
private fun FrameWindowScope.CheckersApp(vm: CheckersViewModel, onExit: ()->Unit) {
    CheckersMenuBar(vm, onExit = onExit )
    MaterialTheme {
        if (vm.showScore)
            ScoreView(vm.score, onClose = vm::disableScore)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BoardWithLabels(vm.board, sidePlayer = vm.sidePlayer, vm.showTargets ,onClickCell = { from, to -> vm.play(from, to) })
            StatusBar(vm.board, vm.sidePlayer, vm.clashName)
        }
        vm.currentAction?.let {
            ClashNameEdit(it, onCancel = vm::cancelAction, onAction = vm::performAction)
        }
        vm.message?.let { Message(it, onOk = vm::hideMessage) }
        if (vm.isWaiting) Waiting()
    }
}

fun main() {
    MongoDriver("ComposeCheckers").use { driver ->
        application(exitProcessOnExit = false) {
            val scope = rememberCoroutineScope()
            val vm = remember { CheckersViewModel(scope, driver) }
            val onExit = { vm.exit(); exitApplication() }
            Window(
                onCloseRequest = onExit,
                title = "Checkers",
                state = rememberWindowState(size = DpSize.Unspecified)
            ) {
                CheckersApp(vm, onExit = onExit)
            }
        }
    }
}