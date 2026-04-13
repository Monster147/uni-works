package pt.isel.ls.server.data.postgresSQL.utils

import pt.isel.ls.server.common.Errors
import java.sql.Connection

/**
 * Função que verifica se uma entidade existe na base de dados.
 *
 * Caso a entidade não exista, lança um erro indicando que o ID não foi encontrado.
 *
 * @param con A conexão com a base de dados.
 * @param table O nome da tabela onde a entidade deve ser verificada.
 * @param idColumn O nome da coluna que contém o ID da entidade.
 * @param id O ID da entidade a ser verificada.
 */
fun checkEntityExists(
    con: Connection,
    table: String,
    idColumn: String,
    id: Int,
) {
    con.prepareStatement("SELECT 1 FROM $table WHERE $idColumn = ?").use { stmt ->
        stmt.setInt(1, id)
        stmt.executeQuery().use { rs ->
            if (!rs.next()) throw Errors.idNotFound(table, id)
        }
    }
}

/**
 * Função que verifica se uma entidade com os dados fornecidos já existe na base de dados.
 *
 * Caso a entidade já exista, lança um erro indicando que a entidade é duplicada.
 *
 * @param con A conexão com a base de dados.
 * @param table O nome da tabela onde a entidade deve ser verificada.
 * @param columns Um mapa contendo os nomes das colunas e os valores correspondentes para verificar duplicação.
 */
fun checkDuplicateEntity(
    con: Connection,
    table: String,
    columns: Map<String, Any>,
) {
    val conditions = columns.keys.joinToString(" AND ") { "$it = ?" }
    con.prepareStatement("SELECT 1 FROM $table WHERE $conditions").use { stmt ->
        columns.values.forEachIndexed { index, value ->
            stmt.setObject(index + 1, value)
        }
        stmt.executeQuery().use { rs ->
            if (rs.next()) {
                throw Errors.duplicateEntity(table.replaceFirstChar(Char::titlecase))
            }
        }
    }
}
