package isel.leic.tds.checkers.model

enum class Player(val symbol: Char, val qSymbol: Char){
    // Jogador WHITE, com os símbolos associados
    WHITE('w', 'W'),

    // Jogador BLACK, com os símbolos associados
    BLACK ('b', 'B');

    // val para obter o jogador a seguir
    val other get() = if(this == WHITE) BLACK else WHITE
}
