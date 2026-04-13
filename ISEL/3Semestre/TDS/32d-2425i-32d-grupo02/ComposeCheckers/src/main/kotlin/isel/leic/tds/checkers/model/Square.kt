package isel.leic.tds.checkers.model

class Square private constructor(val index: Int) {

    val row get () = Row((index - column.index)/BOARD_DIM)
    val column get() = Column(index % BOARD_DIM)

    val black = (row.index + column.index) % 2 == 1

    // Função para transformar um Square em uma string
    override fun toString(): String {
        return "${row.digit}${column.symbol}"
    }

    // Função para verificar se um Square é igual a outro
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Square) return false
        return row == other.row && column == other.column
    }

    override fun hashCode(): Int {
        return  31 * row.hashCode() + column.hashCode()
    }

    companion object{
        // Lista com os Squares
        val values:List<Square> = List(BOARD_DIM * BOARD_DIM) { Square(it) }

    }
}

fun String.toSquareOrNull(): Square?{
    if(length != 2) return null
    val row = this[0].toRowOrNull() ?: return null
    val column = this[1].toColumnOrNull() ?: return null
    return Square(row, column)
}

fun String.toSquare(): Square {
    return this.toSquareOrNull() ?: throw IllegalArgumentException()
}

fun Square(row: Row, column: Column): Square =
    Square.values[row.index * BOARD_DIM + column.index % BOARD_DIM]