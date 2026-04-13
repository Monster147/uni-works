package pt.isel.ls.server.data.postgresSQL

import pt.isel.ls.server.common.Errors
import pt.isel.ls.server.data.CourtsData
import pt.isel.ls.server.data.postgresSQL.utils.DatabaseConnection
import pt.isel.ls.server.data.postgresSQL.utils.SQL_COURTS_CLUB
import pt.isel.ls.server.data.postgresSQL.utils.SQL_COURT_DETAILS
import pt.isel.ls.server.data.postgresSQL.utils.SQL_CREATE_COURT
import pt.isel.ls.server.data.postgresSQL.utils.checkDuplicateEntity
import pt.isel.ls.server.domain.Court
import java.sql.ResultSet

/**
 * Classe que implementa a interface [CourtsData] para manipulação de courts numa base de dados.
 * Esta classe permite criar courts, obter detalhes de um court específico
 * e listar todos os courts associados a um club específico.
 */

class CourtsPostgresSQL : CourtsData {
    /**
     * Função que vai servir para converter um ResultSet num objeto Court.
     *
     * @return Um objeto [Court] com os dados do ResultSet.
     */
    private fun ResultSet.toCourt() =
        Court(
            getInt("crid"),
            getString("name"),
            getInt("club"),
        )

    /**
     * Função que vai servir para converter um ResultSet numa lista de objetos Court.
     *
     * Itera sobre o ResultSet e adiciona cada Court à lista.
     *
     * @return Uma lista de objetos [Court] com os dados do ResultSet.
     */
    private fun ResultSet.toCourtsList(): List<Court> =
        mutableListOf<Court>().apply {
            while (next()) add(toCourt())
        }

    /**
     * Função que cria um court associado a um clube.
     *
     * Caso já exista um court com o mesmo nome associado a um club com o mesmo nome, lança um erro de court duplicado.
     * Guarda no val court o novo court criado por meio de uma consulta SQL.
     * Caso ocorra algum erro durante a criação do court, a transação é revertida e o erro é lançado.
     * Efetua o commit da transação
     * Por fim, retorna o ID do novo court criado.
     *
     * @param name O nome do court a ser criado.
     * @param club O ID do club ao qual o court será associado.
     * @return O ID do [Court] criado.
     */
    override fun createNewCourt(
        name: String,
        club: Int,
    ): Int =
        DatabaseConnection.getConnection().use { con ->
            try {
                checkDuplicateEntity(con, "court", mapOf("name" to name, "club" to club))
                val court =
                    con.prepareStatement(SQL_CREATE_COURT).use { stmt ->
                        stmt.setString(1, name)
                        stmt.setInt(2, club)
                        stmt.executeQuery().use { rs ->
                            if (rs.next()) rs.toCourt() else throw Errors.serverError()
                        }
                    }
                con.commit()
                court.crid
            } catch (e: Exception) {
                con.rollback()
                throw e
            }
        }

    /**
     * Função que obtém os detalhes de um court específico.
     *
     * Verifica se existe um court com o ID fornecido.
     * Se existir, retorna o court correspondente, caso contrário, retorna null.
     *
     * @param courtId O ID do court cujos detalhes pretende-se obter.
     * @return Os detalhes do [Court] ou null se o court não existir.
     */
    override fun getCourtDetails(courtId: Int): Court? =
        DatabaseConnection.getConnection().use { con ->
            con.prepareStatement(SQL_COURT_DETAILS).use { stmt ->
                stmt.setInt(1, courtId)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) rs.toCourt() else null
                }
            }
        }

    /**
     * Função que obtém todos os courts associados a um club específico.
     *
     * Verifica se o club existe.
     * Por fim retorna uma lista de courts associados ao club especificado, ou uma lista vazia caso o club
     * não exista ou caso não tenha nenhum court associado.
     *
     * @param club O ID do club cujos courts pretende-se obter.
     * @return Uma lista de courts associados ao club especificado.
     */
    override fun getAllCourtsFromAClub(club: Int): List<Court> =
        DatabaseConnection.getConnection().use { con ->
            con.prepareStatement(SQL_COURTS_CLUB).use { stmt ->
                stmt.setInt(1, club)
                stmt.executeQuery().use { rs ->
                    rs.toCourtsList()
                }
            }
        }
}
