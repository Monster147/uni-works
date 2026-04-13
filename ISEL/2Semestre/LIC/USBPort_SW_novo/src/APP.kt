import isel.leic.utils.Time
import java.io.File
import kotlin.random.Random

object APP {
    private var CURRLINE = 0 // Linha atual do cursor
    private var MAINTENANCE = false // Indica se o modo de manutenção está ativado
    private var CURRKEY = ' ' // Tecla atual pressionada
    private val INVADERSLINE0 = "" // Lista de invasores na linha 0
    private val INVADERSLINE1 = "" // Lista de invasores na linha 1
    val INVADERSLINES = arrayOf(INVADERSLINE0, INVADERSLINE1)
    private var SPAWNRATE = 0 // Taxa de aparecimento dos invasores
    private val LETTER = ('A'..'Z').toList().toCharArray() // Array de letras de A a Z
    private var ACTUALLETTER = 0 // Índice da letra atual
    private var ACTUALCURSOR = 5 // Posição atual do cursor na linha de entrada do nome
    var LEADERBOARD = mutableListOf<Pair<String, Int>>() // Lista de pontuações
    var PLAYERNAME = "           " // Nome do jogador
    var COIN = 0 // Número de moedas
    var SCORE = 0 // Pontuação atual
    var STARTGAME = false // Indica se o jogo começou
    var SHUTDOWN = false // Indica se o sistema deve ser desligado
    var stackcounter = 0
    var LOSE = false
    var TEXT = arrayOf("?Clear Counters?", "   ?Shutdown?   ", "5-Yes   Other-No ")

    // Função de confirmação que recebe 2 parâmetros, duas strings, uma para a linha 0 e outra para a linha 1.
    // Se carregarmos na tecla '5', a função retorna true, caso contrário retorna falso
    fun confirm(textline0: String, textline1: String):Boolean{
        TUI.clear()
        TUI.cursor(0,0)
        TUI.write(textline0)
        TUI.cursor(1,0)
        TUI.write(textline1)
        val key = TUI.waitKey(5000) // Aguarda uma tecla pressionada num intervalo de tempo até 5 segundos
        return if(key == '5') true else false
    }

    // Elimina o conteúdo do ficheiro recebido como parâmetro
    fun deleteFileContent(file: File) {
        file.printWriter()
            .use { out -> out.print("") } // Escreve um conteúdo vazio no arquivo, ou seja, apaga o seu conteúdo
    }

    // Escreve a Leaderboard no ficheiro "scores.txt"
    fun writeLeaderboard() {
        for (i in 0 until LEADERBOARD.size) {
            if(i == LEADERBOARD.size - 1 && LEADERBOARD[i] != LEADERBOARD[i-1]){
                Scores.PUTSCORE.write("${LEADERBOARD[i].first}-${LEADERBOARD[i].second}") // Escreve cada par (nome, pontuação) no arquivo de pontuações
                Scores.PUTSCORE.newLine() // Adiciona uma nova linha após cada pontuação
            }
            if(i != LEADERBOARD.size - 1 && LEADERBOARD[i] != LEADERBOARD[i+1]){
                Scores.PUTSCORE.write("${LEADERBOARD[i].first}-${LEADERBOARD[i].second}") // Escreve cada par (nome, pontuação) no arquivo de pontuações
                Scores.PUTSCORE.newLine() // Adiciona uma nova linha após cada pontuação
            }
        }
        Scores.PUTSCORE.flush() // Garante que todos os dados são escritos no arquivo
    }

    // Põe as 20 melhores pontuações na leaderboard
    fun putScoresinLeaderboard() {
        for (i in 0 until Scores.GAMESCORES.size) { // Itera sobre todas as pontuações
                LEADERBOARD.add(Scores.GAMESCORES[i]) // Adiciona a pontuação à leaderboard
        }

    }

    // Inicializa todas as classes
    fun init() {
        TUI.init() // Inicializa a interface do usuário
        ScoreDisplay.init() // Inicializa o display de pontuação
        CoinAcceptor.init() // Inicializa o aceitador de moedas
        Maintenance.init() // Inicializa o modo de manutenção
        ScoreDisplay.off(false) // Desativa o scoredisplay
        putScoresinLeaderboard() // Adiciona as pontuações à leaderboard
        Statistics.getStats()
    }

    fun deleteStats() {
        Statistics.GAMESCOUNT = 0 // Define o número de jogos como 0
        Statistics.NUMBERCOINS = 0 // Define o número de moedas como 0
        deleteFileContent(Statistics.STATSLIST) // Apaga o conteúdo do arquivo de estatísticas
        Statistics.clearStats()
        println("ESTADO - ${Statistics.GAMESCOUNT}, ${Statistics.NUMBERCOINS}") // Imprime o estado atual das estatísticas
    }

    /*
 Menu para apagarmos o conteúdo do ficheiro das estatísticas e mete os valores a 0, caso queiramos apagar.
 Assim acontecerá, caso não, voltamos para o menu de manutenção.
 */
    private fun clearCounters() {
        if(confirm(TEXT[0], TEXT[2])){
            deleteStats()
            TUI.clear()
            TUI.maintWrite() // Retorna ao menu de manutenção
        }
        else {
            TUI.clear()
            TUI.maintWrite() // Retorna ao menu de manutenção
        }
    }

    fun maintenanceGame() {
        // Inicia o jogo
        TUI.start(CURRLINE)
        STARTGAME = true
        ScoreDisplay.zero() // Inicializa a exibição da pontuação do jogo (será 0)
        while (STARTGAME) { // Enquanto o jogo estiver em andamento e não houver derrota
            ScoreDisplay.off(false) // Desliga o placar
            Time.sleep(175) // Aguarda um curto período de tempo(175 ms)
            spaceInvaders()   // Executa o jogo de Space Invaders
            if (inferLose()) {  // Verifica se houve derrota
                STARTGAME = false  // Sai do loop do jogo e volta para o menu de manutenção
                TUI.clear()
                TUI.maintWrite()
            }
        }
    }

    fun statsScreenWrite(games: Int, coins: Int){
        TUI.clear()  // Limpa a tela
        // Exibe informações sobre jogos e moedas
        TUI.cursor(0, 0)
        TUI.write("Games:$games")
        TUI.cursor(1, 0)
        TUI.write("Coins:$coins")
    }

    fun statsScreen(games: Int, coins: Int) {
        statsScreenWrite(games, coins)
        TUI.clearLine(1, 6 + Statistics.NUMBERCOINS.toString().length, TUI.NUMBERCOLUMS)// Limpa a segunda linha da tela a partir da coluna 6 acrescida do comprimento da representação em string do número de moedas (para compensar o texto "Coins:")
        val actualkey = TUI.waitKey(5000)// Aguarda uma tecla pressionada num intervalo de tempo até 5 segundos
        if (actualkey == '#') { // Se a entrada for '#', limpa os contadores
            clearCounters()
        } else {  // Caso contrário, volta para o menu de manutenção
            TUI.clear()
            TUI.maintWrite()
        }
    }

    fun shutdownScreen() {
        if(confirm(TEXT[1], TEXT[2])) SHUTDOWN = true
        else{
            TUI.clear()
            TUI.maintWrite()
        }
    }

    /* Menu de manuntenção, dependo da tecla podemos ir para diferentes partes da manutenção. Com a tecla '*'
    * vamos para o menu onde vemos os jogos e moedas, com a tecla '#' vamos para o menu de shutdown, ao carregar
    * noutra tecla qualquer fazemos um jogo */
    fun maintenanceMenu() {
        // Calcula o total de jogos e moedas
        while (Maintenance.maintenanceOn()) { // Enquanto o modo de manutenção estiver ativado e a manutenção estiver a ocorrer
            val key = TUI.getKey()  // Obtém a tecla pressionada
            when (key) { // Realiza ações com base na tecla pressionada
                // Se a tecla estiver no intervalo de '0' a '9'
                in '0'..'9' -> maintenanceGame()
                // Se a tecla for '*', exibe informações sobre jogos e moedas
                '*' -> {
                    Statistics.getStats()
                    statsScreen(Statistics.GAMESCOUNT, Statistics.NUMBERCOINS)
                }
                // Se a tecla for '#', inicia o processo de shutdown
                '#' -> {
                    shutdownScreen()
                    if(SHUTDOWN) break
                }
            }
        }
    }

    // Esta função é responsável por exibir as 20 melhores pontuações recebidas através da lista LEADERBOARD
    fun showScores() {
        var scoreIdx = 0 // Inicializa o índice da pontuação como 0
        if (LEADERBOARD.size != 0) { // Verifica se a lista LEADERBOARD não está vazia
            while (scoreIdx != 20 && scoreIdx != LEADERBOARD.size) {  // Enquanto não foram exibidas as 20 melhores pontuações ou enquanto não chegamos ao final da lista LEADERBOARD
                val score = LEADERBOARD[scoreIdx] // Obtém a pontuação na posição atual
                val scoreUser = score.second.toString()  // Converte a pontuação para uma string
                if (putCoin() || gameCanStart() || Maintenance.maintenanceOn()) break  // Verifica se deve interromper a exibição (se uma moeda foi inserida, se o jogo pode começar ou se a manutenção está ativada)
                TUI.cursor(1, 0) // Move o cursor para a linha 1 e coluna 0
                TUI.write("${scoreIdx + 1}-${score.first}")   // Escreve a posição e o nome do jogador
                TUI.cursor(1, 16 - scoreUser.length)  // Move o cursor para a posição onde a pontuação será exibida
                TUI.write(scoreUser)    // Escreve a pontuação do jogador
                Time.sleep(1000) // Aguarda por um segundo para exibir a pontuação
                TUI.clearLine(1, 0, TUI.NUMBERCOLUMS) // Limpa a linha 1 do LCD
                scoreIdx++  // Incrementa o índice da pontuação.
            }
        }
    }

    /* Função onde adicionamos uma moeda ao número de moedas introduzidas para o sistema
    e adiciona mais 2 créditos */
    private fun putCoin(): Boolean {
        if (CoinAcceptor.acceptCoin() && !Maintenance.maintenanceOn()) {  // Verifica se uma moeda foi aceita e se a manutenção não está ativada.
            Statistics.NUMBERCOINS++  // Incrementa o número de moedas e adiciona 2 créditos.
            COIN += 2 // adiciona os 2 créditos
            return true   // Retorna true para indicar que uma moeda foi adicionada com sucesso.
        }
        return false // Retorna false se uma moeda não foi adicionada.
    }


    // Escreve a nave e a zona onde escrevemos o invader
    // Esta função escreve a representação gráfica da nave e a área onde os invasores serão exibidos
    private fun writeBack() {
        TUI.cursor(CURRLINE, 0) // Define o cursor na linha atual e na coluna 0
        TUI.write("_>") // Escreve a representação gráfica da nave
    }

    // Função para mostrar os créditos no LCD
    // Esta função atualiza e exibe o valor das moedas no LCD
    fun coinValue() {
        if (!STARTGAME) { // Verifica se o jogo ainda não começou
            putCoin() // Adiciona uma moeda se uma moeda for inserida
            TUI.coinDisplay(COIN) // Exibe o valor das moedas no LCD
        }
    }


    // Função que verifica se podemos jogar
    fun gameCanStart(): Boolean {
        if (COIN > 0) { // Verifica se há créditos disponíveis, ou seja, se é >0
            val key = TUI.getKey() // Obtém a tecla pressionada
            if (key == '*') { // Se a tecla pressionada é '*'
                TUI.start(CURRLINE) // Inicia o jogo na linha atual do display
                TUI.clearLine(CURRLINE, 3, TUI.NUMBERCOLUMS) // Limpa a linha atual do display (coluna 3 até à 16)
                STARTGAME = true // Define a variável STARTGAME como verdadeira para indicar que o jogo está em andamento
                return true // Retorna verdadeiro para indicar que o jogo pode começar
            }
        }
        return false
    }

    // Esta função verifica se o jogador perdeu o jogo ou não
    fun inferLose(): Boolean {
        if (INVADERSLINES[0].length == TUI.NUMBERCOLUMS - 2 || INVADERSLINES[1].length == TUI.NUMBERCOLUMS - 2) {  // Verifica se os invasores alcançaram a nave
            TUI.loseLCD(SCORE) // Mostra a mensagem de derrota no display
            if (!MAINTENANCE) { // Se não estiver no modo de manutenção
                COIN-- // Reduz o número de moedas
                Statistics.GAMESCOUNT++ // Incrementa o número de jogos
            }
            STARTGAME = false // Define STARTGAME como falso para indicar que o jogo acabou
            TUI.clearLine(1, 7 + SCORE.toString().length, TUI.NUMBERCOLUMS) // Limpa a linha do LCD
            Time.sleep(2500) // Espera 2.5 segundos
            INVADERSLINES[0] = "" // Limpa a linha de invasores na posição 0
            INVADERSLINES[1] = "" // Limpa a linha de invasores na posição 1
            SPAWNRATE = 0 // Reseta o SPAWNRATE
            if (Maintenance.maintenanceOn()) SCORE = 0 // Se estiver no modo de manutenção, reseta a pontuação
            return true // Retorna verdadeiro para indicar que o jogo foi perdido
        }
        return false // Retorna falso para indicar que o jogo ainda está em andamento
    }


    // Função que aceita o nome e adiciona o nome e a pontuação à LEADERBOARD e volta ao menu principal,
    // reiniciando algumas variáveis
    fun enterName() {
        LEADERBOARD.add(PLAYERNAME.substringBefore(' ') to SCORE) // Adiciona o nome do jogador e sua pontuação à leaderboard
        Time.sleep(10) // Aguarda 10 milissegundos
        ACTUALCURSOR = 5 // Reinicia a posição do cursor
        ACTUALLETTER = 0 // Reinicia o índice da letra
        PLAYERNAME = "           " // Reinicia o nome do jogador
        SCORE = 0 // Reinicia a pontuação
        LEADERBOARD.sortByDescending { it.second } // Ordena a leaderboard por pontuação decrescente
        TUI.clear() // Limpa o LCD
    }

    fun nextSpace() {
        if (ACTUALCURSOR in 5..14) { // Verifica se o cursor está dentro dos limites do nome do jogador
            ACTUALCURSOR++ // Move o cursor para a direita
            val name = PLAYERNAME // Obtém o nome do jogador
            val letter = name[ACTUALCURSOR - 5] // Obtém a letra na posição atual do cursor
            if (letter == ' ') ACTUALLETTER = 0 // Se a letra for um espaço, define o índice da letra como 0
            else ACTUALLETTER = LETTER.indexOf(letter) // Senão, obtém o índice da letra na lista LETTER
            TUI.cursor(0, ACTUALCURSOR) // Move o cursor para a nova posição
            TUI.write(LETTER[ACTUALLETTER]) // Escreve a letra no LCD
            TUI.cursor(0, ACTUALCURSOR) // Move o cursor para a nova posição
            val string = StringBuilder(PLAYERNAME) // Cria um StringBuilder com o nome do jogador
            string.setCharAt(ACTUALCURSOR - 5, LETTER[ACTUALLETTER]) // Atualiza a letra na posição do cursor
            PLAYERNAME = string.toString() // Atualiza o nome do jogador
        }
    }

    fun prevSpace() {
        if (ACTUALCURSOR in 6..15) { // Verifica se o cursor está dentro dos limites do nome do jogador
            ACTUALCURSOR-- // Move o cursor para a esquerda
            TUI.cursor(0, ACTUALCURSOR) // Move o cursor para a nova posição
            val prevLetter = PLAYERNAME[ACTUALCURSOR - 5] // Obtém a letra na nova posição do cursor
            ACTUALLETTER = LETTER.indexOf(prevLetter) // Obtém o índice da letra na lista LETTER
            TUI.cursor(0, ACTUALCURSOR) // Move o cursor para a nova posição
            TUI.write(LETTER[ACTUALLETTER]) // Escreve a letra no LCD
            TUI.cursor(0, ACTUALCURSOR) // Move o cursor para a nova posição
        }
    }

    fun eraseLetter() {
        if (ACTUALCURSOR in 6..15) { // Verifica se o cursor está dentro dos limites do nome do jogador
            TUI.cursor(0, ACTUALCURSOR) // Move o cursor para a posição atual
            TUI.write(' ') // Escreve um espaço no LCD
            val string = StringBuilder(PLAYERNAME) // Cria um StringBuilder com o nome do jogador
            string.setCharAt(ACTUALCURSOR - 5, ' ') // Remove a letra na posição do cursor
            PLAYERNAME = string.toString() // Atualiza o nome do jogador
            ACTUALCURSOR-- // Move o cursor para a esquerda
            TUI.cursor(0, ACTUALCURSOR) // Move o cursor para a nova posição
            val prevLetter = PLAYERNAME[ACTUALCURSOR - 5] // Obtém a letra na nova posição do cursor
            ACTUALLETTER = LETTER.indexOf(prevLetter) // Obtém o índice da letra na lista LETTER
            TUI.cursor(0, ACTUALCURSOR) // Move o cursor para a nova posição
            TUI.write(LETTER[ACTUALLETTER]) // Escreve a letra no LCD
            TUI.cursor(0, ACTUALCURSOR) // Move o cursor para a nova posição
        }
    }

    fun writeLetter(key:Char){
        if(key == '2') {
            if (ACTUALLETTER == LETTER.lastIndex) ACTUALLETTER =
                0 // Verifica se o índice da letra atual é o último da lista
            else ACTUALLETTER++ // Se não for, incrementa o índice
        }
        else{
            if (ACTUALLETTER == 0) ACTUALLETTER = 0 // Verifica se o índice da letra é 0
            else ACTUALLETTER-- // Se não for, decrementa o índice
        }
        TUI.write(LETTER[ACTUALLETTER]) // Escreve a letra no LCD
        TUI.cursor(0, ACTUALCURSOR) // Move o cursor para a posição atual
        val string = StringBuilder(PLAYERNAME) // Cria um StringBuilder com o nome do jogador
        string.setCharAt(ACTUALCURSOR - 5, LETTER[ACTUALLETTER]) // Atualiza a letra na posição do cursor
        PLAYERNAME = string.toString() // Atualiza o nome do jogador
    }

    /* Função para escrevemos o nome que queremos associar à pontuação obtida. A tecla 2 escreve as letras de A a Z.
    * A tecla 5 é para fazermos enter ao nosso nome, adicionando ao ficheiro das pontuações, reinicializando
    * as variáveis iniciais. A tecla 6 avança um espaço. A tecla 4 volta um espaço atrás. A tecla * apaga a letra
    * A tecla 8 volta a letra atrás, escrevendo de Z a A */
    // Esta função lida com a entrada do jogador após o término do jogo
    fun postGame() {
        val key = TUI.waitKey(5000) // Aguarda uma tecla pressionada num intervalo de tempo até 5 segundos
        when (key) {
            // Se a tecla pressionada for '2'
            '2' -> writeLetter(key)
            // Se a tecla pressionada for '5'
            '5' -> {
                enterName()
                LOSE = false
            }
            // Se a tecla pressionada for '6'
            '6' -> nextSpace()
            // Se a tecla pressionada for '4'
            '4' -> prevSpace()
            // Se a tecla pressionada for '*'
            '*' -> eraseLetter()
            // Se a tecla pressionada for '8'
            '8' -> writeLetter(key)
        }
    }

    fun changeLine() {
        // Limpa o indicador de seleção e move o cursor para a próxima linha de invasores
        TUI.cursor(CURRLINE, 1)
        TUI.write(" ")
        CURRLINE = if (CURRLINE == 0) 1 else 0 // Move o cursor para a próxima linha de invasores
        TUI.cursor(CURRLINE, 1)
        TUI.write(">")
    }

    fun shootInvader() {
        writeBack()
        if (INVADERSLINES[CURRLINE].length > 0 && INVADERSLINES[CURRLINE].first() == CURRKEY) {
            TUI.cursor(CURRLINE, TUI.NUMBERCOLUMS - INVADERSLINES[CURRLINE].length) //Esta linha move o cursor para a posição onde o primeiro invasor está no LCD
            INVADERSLINES[CURRLINE] = INVADERSLINES[CURRLINE].drop(1) // remove o primeiro invasor da linha de invasores
            TUI.write(' ') //Esta linha escreve um espaço em branco no local onde o primeiro invasor estava no LCD
            SCORE += CURRKEY.toString().toInt() + 1 // Incrementa a pontuação do jogo com base no valor do invasor acertado
        }
    }

    fun showInvaders(){
        SPAWNRATE++ // Incrementa o SPAWNRATE para controlar o ritmo de spawn dos invasores
        if (SPAWNRATE == 4) { // Se SPAWNRATE atingir 4 (intervalo de tempo modificado por nós)
            val invaderPos = Random.nextInt(2) // Gera aleatoriamente a posição do invasor
            if (invaderPos == 0) { // Se a posição for 0, invasores são gerados na linha 0
                INVADERSLINES[0] = TUI.spawnInvaders(INVADERSLINES[0].toMutableList(), 0)
            } else { // Se não, invasores são gerados na linha 1
                INVADERSLINES[1] = TUI.spawnInvaders(INVADERSLINES[1].toMutableList(), 1)
            }
            SPAWNRATE = 0 // Reseta o SPAWNRATE
        }
    }

    // Função de jogo
    fun spaceInvaders() {
        ScoreDisplay.setScore(SCORE)  // Define a pontuação atual do jogo na interface
        val key = TUI.getKey()  // Obtém a tecla pressionada pelo jogador
        when (key) { // Executa ações com base na tecla pressionada
            // Se a tecla pressionada estiver no intervalo de '0' a '9'
            in '0'..'9' -> {
                CURRKEY = key // não é utilizado
                TUI.cursor(CURRLINE, 0) // Posiciona o cursor na linha atual
                TUI.write(key) // Escreve o número selecionado pelo jogador
            }
            // Se o jogador pressionar '*' (mudar de linha)
            '*' -> changeLine()
            // Se o jogador pressionar '#' (shoot)
            '#' -> shootInvader()
        }
        showInvaders()
    }


    fun game(){
        ScoreDisplay.zero() // Inicializa a exibição da pontuação do jogo (será 0)
        while (STARTGAME) {  // Loop enquanto o jogo estiver em andamento
            Time.sleep(175) // Intervalo de tempo de 175 ms
            spaceInvaders() // Executa o jogo Space Invaders
            if (inferLose() && SCORE > 0) { // Verifica se o jogador perdeu
                TUI.userScore(SCORE) // Exibe a pontuação do jogador
                PLAYERNAME = "A          " //Inicialixa o nome do jogador
                LOSE = true
            }
        }
    }

    // Função que faz tudo que a aplicação requer e quando requer
    fun app() {
        while(!SHUTDOWN){
            MAINTENANCE = false
            Time.sleep(800) // Intervalo de tempo de 800 ms
            TUI.begin() // Inicializa o TUI
            TUI.coinDisplay(COIN) // Exibe a quantidade de créditos no LCD
            showScores() // Exibe as pontuações na tela
            coinValue() // Verifica se uma moeda foi inserida
            gameCanStart() // Verifica se o jogo pode começar
            game()
            while (LOSE) {
                postGame()
            }
            if (Maintenance.maintenanceOn() && !STARTGAME) { // Loop para manutenção
                MAINTENANCE = true
                TUI.clear()
                TUI.maintWrite()
                maintenanceMenu() // Exibe o modo manutenção
            }
        }
    }

    // Menu para realizar o shutdown do sistema
    /* Menu para fazermos shutdown, caso queiramos fazer shutdown, o sistema desliga o programa, guardando o valor
    das estatísticas no ficheiro das estatisticas e a leaderboard no ficheiro das pontuações, caso não
    voltamos para o menu de manuntenção */
    fun shutdown() {
        ScoreDisplay.off(true) // Desliga a exibição do placar
        TUI.clear() // Limpa a tela
        deleteFileContent(Scores.SCORELIST) // Apaga o conteúdo do arquivo de pontuações
        deleteFileContent(Statistics.STATSLIST) // Apaga o conteúdo do arquivo de estatísticas
        writeLeaderboard() // Escreve a leaderboard no arquivo
        // Calcula o total de jogos e moedas
        val GAMES = Statistics.GAMESCOUNT
        val COINS = Statistics.NUMBERCOINS
        // Escreve os valores no arquivo de estatísticas
        Statistics.PUTSTATS.write("$GAMES")
        Statistics.PUTSTATS.newLine()
        Statistics.PUTSTATS.write("$COINS")
        Statistics.PUTSTATS.flush() // Escreve os dados restantes do buffer no arquivo de saída (Garante que todos os dados são escritos no arquivo)
        Time.sleep(500)
        System.exit(0)  // Encerra o programa(fecha a app)
    }

    fun main(){
        init()  // Inicialização do jogo
        app()
        shutdown()
    }
}

fun main(){
    APP.main()
    /*APP.init()
    APP.SCORE = 1253
    TUI.userScore(APP.SCORE)
    APP.PLAYERNAME = "A      "
    while (true) { if (APP.postGame()) break }*/
}