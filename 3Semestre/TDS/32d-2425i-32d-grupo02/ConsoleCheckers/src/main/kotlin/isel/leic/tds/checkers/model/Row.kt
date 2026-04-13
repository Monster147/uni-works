package isel.leic.tds.checkers.model

@JvmInline
value class Row(val index:Int) {
    init{
        require(index in 0 ..< BOARD_DIM) {throw IllegalArgumentException("Invalid row index: $index")}
    }

    // Dígito da Row
    val digit: Char
        get() = ('0' + BOARD_DIM - index).toChar()

    companion object {
        // Lista com as Rows
        val values:List<Row> = List(BOARD_DIM) { Row(it) }
    }

}

// Transforma um caracter em uma Row
fun Char.toRowOrNull(): Row?{
    val idx = BOARD_DIM - (this.lowercaseChar() - '0')
    return if (idx in 0 ..< BOARD_DIM) Row(idx) else null
}