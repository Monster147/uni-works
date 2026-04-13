package isel.leic.tds.checkers.ui

import androidx.compose.runtime.*
import isel.leic.tds.checkers.model.*
import isel.leic.tds.storage.MongoDriver
import isel.leic.tds.storage.MongoStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


enum class Action(val text: String){
    START("Start"), JOIN("Join")
}

class CheckersViewModel(private val scope: CoroutineScope, driver: MongoDriver) {
    // Model State
    private val driver = MongoDriver("ComposeCheckers")
    //private val storage = TextFileStorage<Name, Game>("games", GameSerializer)
    private val storage = MongoStorage<Name, Game>("Checkers", driver, GameSerializer)
    private var clash by mutableStateOf(Clash(storage))
    private var requestedRefresh by mutableStateOf(false)

    val board: Board? get() = (clash as? ClashRun)?.game?.board
    val score get() = (clash as ClashRun).game.score
    val sidePlayer get() = (clash as? ClashRun)?.sidePlayer
    val hasClash get() = clash is ClashRun
    val isSideTurn get() = clash.isSideTurn
    var autoRefresh by mutableStateOf(false)
    var showTargets by mutableStateOf(false)
    val clashName: String?
        get() = (clash as? ClashRun)?.name?.toString()


    fun newBoard() = oper(Clash::newBoard)

    fun play(from: Square, to:Square) = oper { play(from, to) }
        .also { waitForOtherSide() }

    fun exit() {
        cancelWaiting()
        clash.exit()
        driver.close()
    }

    // View State
    var showScore by mutableStateOf(false)
        private set

    fun enableScore() {
        showScore = true
    }

    fun disableScore() {
        showScore = false
    }

    fun enableTargets() {
        showTargets = true
    }

    fun disableTargets() {
        showTargets = false
    }

    fun showTargets() {
        showTargets = !showTargets
        if (showTargets) enableTargets() else disableTargets()
    }

    fun refresh() {
        oper(Clash::refresh)
        requestedRefresh = true
    }

    private var enableAutoRefresh: Job? = null

    fun autoRefresh(){
        autoRefresh = !autoRefresh
        if (autoRefresh) enableAutoRefresh() else disableAutoRefresh()
    }

    private fun enableAutoRefresh(){
        if (enableAutoRefresh == null) {
            enableAutoRefresh = scope.launch {
                while (autoRefresh) { // Verifica o estado de autoRefresh continuamente
                    try {
                        clash = clash.refresh() // Atualiza o estado do jogo
                    } catch (ex: NoChangeException) {
                        // Não houve mudanças, continua o loop
                    } catch (ex: Exception) {
                        catchException(ex)
                        break
                    }
                    delay(3000) // Espera 3 segundos antes do próximo refresh
                }
            }
        }
    }

    private fun disableAutoRefresh(){
        enableAutoRefresh?.cancel()
        enableAutoRefresh= null
    }

    var currentAction: Action? by mutableStateOf(null)
        private set

    fun startClash() {
        currentAction = Action.START
    }

    fun joinClash() {
        currentAction = Action.JOIN
    }


    fun cancelAction() {
        currentAction = null
    }

    fun performAction(name: Name) {
        cancelWaiting()
        oper {
            when (currentAction as Action) {
                Action.JOIN -> clash.join(name)
                Action.START -> clash.start(name)
            }
        }
        waitForOtherSide()
        currentAction = null
    }

    var message: String? by mutableStateOf(null)
        private set

    fun hideMessage() {
        message = null
    }

    private fun oper(fx: Clash.() -> Clash) {
        try {
            clash = clash.fx()
        } catch (ex: Exception) {
            catchException(ex)
        }
    }

    private fun catchException(ex: Exception) {
        if (ex is IllegalStateException || ex is IllegalArgumentException) {
            message = ex.message
            if (ex is ClashNotFound) clash = Clash(clash.storage)
        } else throw ex
    }

    private var waitingJob by mutableStateOf<Job?>(null)
    val isWaiting get() = waitingJob != null

    private fun cancelWaiting() {
        waitingJob?.cancel()
        waitingJob = null
    }

    private fun waitForOtherSide() {
        if (isSideTurn || autoRefresh) return
        waitingJob?.cancel()
        waitingJob = scope.launch {
            while (true) {
                delay(3000)
                if (requestedRefresh) {
                    try {
                        clash = clash.refresh()
                        if (isSideTurn) break
                    } catch (ex: NoChangeException) { /* continue loop */
                    } catch (ex: Exception) {
                        catchException(ex); break
                    }
                }
                requestedRefresh = false
            }
            waitingJob = null
        }
    }
}