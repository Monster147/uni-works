package isel.leic.tds.checkers.model

import isel.leic.tds.checkers.model.Player.BLACK
import isel.leic.tds.checkers.model.Player.WHITE
import kotlin.math.*


data class Piece(val player: Player, val pos: Square, val isQueen: Boolean = false){
    // Símbolo associado à peça
    val symbol = if (isQueen) player.qSymbol else player.symbol

    // Função para transformar a peça em uma peça-raínha
    fun canBeQueen(row: Row) =
        when(player){
            WHITE -> row.index == 0
            BLACK -> row.index == BOARD_DIM-1
        }

    // Função para verificar se uma peça pode capturar outra
    fun canCapture(otherPiece: Piece?, places: Map<Square, Piece>): Boolean{
        require(otherPiece != null)
        when{
            this == otherPiece -> return false
            player == otherPiece.player -> return false
            otherPiece.pos.row.index == 0 || otherPiece.pos.row.index == BOARD_DIM-1 -> return false
            otherPiece.pos.column.index == 0 || otherPiece.pos.column.index == BOARD_DIM-1 -> return false
        }

        val colDis = otherPiece.pos.column.index - this.pos.column.index
        val rowDis = otherPiece.pos.row.index - this.pos.row.index
        if (abs(colDis)!=abs(rowDis)) return false

        return if(this.isQueen){
            !otherPiece.checkIfGuarded(
                places.filter{it.value.player == otherPiece.player},
                colDis > 0, rowDis > 0)
        }
        else{
            if(abs(rowDis)!= 1 || abs(colDis)!= 1) return false
            !otherPiece.checkIfGuarded(
                places.filter{it.value.player == otherPiece.player},
                colDis > 0, rowDis > 0)
        }
    }

    // Função para verificar se a peça tem uma peça na diagonal anterior
    private fun checkIfGuarded(otherPlaces: Map<Square, Piece>, checkColDis:Boolean, checkRowDis:Boolean): Boolean {
        val colDis = if(checkColDis) 1 else -1
        val rowDis = if(checkRowDis) 1 else -1
        if(otherPlaces.any{this.pos.column.index + colDis == it.value.pos.column.index
                    && this.pos.row.index + rowDis == it.value.pos.row.index}) return true
        if(otherPlaces.any{this.pos.column.index - colDis == it.value.pos.column.index
                    && this.pos.row.index - rowDis == it.value.pos.row.index}) return true
        return false
    }
}