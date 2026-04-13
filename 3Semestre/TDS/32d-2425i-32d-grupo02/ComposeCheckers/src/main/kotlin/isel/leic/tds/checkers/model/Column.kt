package isel.leic.tds.checkers.model

@JvmInline
value class Column(val index: Int) {
    init {
        require(index in 0 until BOARD_DIM) { "Invalid column index: $index" }
    }

    // Símbolo da Column
    val symbol: Char
        get() = 'a' + index

    companion object {
        // Lista com as Column's
        val values: List<Column> = List(BOARD_DIM) { Column(it) }
    }
}

// Transforma um caracter em uma Column
fun Char.toColumnOrNull(): Column? {
    val idx = this.uppercaseChar() - 'A'
    return if (idx in 0 until BOARD_DIM) Column(idx) else null
}