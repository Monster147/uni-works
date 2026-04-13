package pt.isel.ls.server.data.postgresSQL

import pt.isel.ls.server.common.AppError
import pt.isel.ls.server.common.Errors
import pt.isel.ls.server.data.ClubData
import pt.isel.ls.server.data.postgresSQL.utils.DatabaseConnection
import pt.isel.ls.server.data.postgresSQL.utils.SQL_CLUBS
import pt.isel.ls.server.data.postgresSQL.utils.SQL_CLUB_DETAILS
import pt.isel.ls.server.data.postgresSQL.utils.SQL_CREATE_CLUB
import pt.isel.ls.server.data.postgresSQL.utils.checkDuplicateEntity
import pt.isel.ls.server.domain.Club
import pt.isel.ls.server.web.models.ClubsName
import java.sql.ResultSet

/**
 * Classe que implementa a interface [ClubData] para manipulação de clubs numa base de dados.
 * Contém métodos para criar um club, obter os detalhes de um determinado club, obter todos os clubs existentes
 * e para pesquisar clubs pelo nome.
 */
class ClubPostgresSQL : ClubData {
    /**
     * Função que vai servir para converter um ResultSet num objeto Club.
     *
     * @return Um objeto [Club] com os dados do ResultSet.
     */
    private fun ResultSet.toClub() =
        Club(
            getInt("cid"),
            getString("name"),
            getInt("owner"),
        )

    /**
     * Função que vai servir para converter um ResultSet num objeto ClubsName.
     *
     * @return Um objeto [ClubsName] com os dados do ResultSet.
     */
    private fun ResultSet.toClubsName() =
        ClubsName(
            getString("name"),
            getInt("cid"),
        )

    /**
     * Função que vai servir para converter um ResultSet numa lista de objetos ClubsName.
     * Itera sobre o ResultSet e adiciona cada ClubsName à lista.
     *
     * @return Uma lista de objetos [ClubsName] com os dados do ResultSet.
     */
    private fun ResultSet.toClubsNameList(): List<ClubsName> =
        mutableListOf<ClubsName>().apply {
            while (next()) add(toClubsName())
        }

    /**
     * Função que vai servir para criar um club.
     *
     * Caso o nome do club já exista e o owner dele seja o mesmo, lança um erro de duplicação.
     * Guarda no val club o novo club criado por meio de uma consulta SQL.
     * Caso a consulta falhe, lança um erro de servidor.
     * Caso a consulta seja bem-sucedida, adiciona o novo club à base de dados efetuando o commit da transação.
     * Por fim, retorna o ID do novo club criado.
     * Caso seja lançada uma exceção do tipo AppError, efetua o rollback da transação e relança o erro.
     *
     * @param name O nome do novo club.
     * @param owner O ID do utilizador que está a criar o club.
     * @return O ID do [Club] criado.
     */
    override fun createClubByName(
        name: String,
        owner: Int,
    ): Int =
        DatabaseConnection.getConnection().use { con ->
            try {
                checkDuplicateEntity(con, "club", mapOf("name" to name, "owner" to owner))
                val club =
                    con.prepareStatement(SQL_CREATE_CLUB).use { stmt ->
                        stmt.setString(1, name)
                        stmt.setInt(2, owner)
                        stmt.executeQuery().use { rs ->
                            if (rs.next()) rs.toClub() else throw Errors.serverError()
                        }
                    }
                con.commit()
                club.cid
            } catch (e: AppError) {
                con.rollback()
                throw e
            }
        }

    /**
     * Função que vai servir para obter os detalhes de um determinado club.
     *
     * Caso o ID do club esteja presente na lista de clubs, retorna o club correspondente.
     *
     * @param cid O ID do club cujos detalhes se pretende obter.
     * @return Os detalhes do [Club] ou null se o club não existir.
     */
    override fun getClubDetails(cid: Int): Club? =
        DatabaseConnection.getConnection().use { con ->
            con.prepareStatement(SQL_CLUB_DETAILS).use { stmt ->
                stmt.setInt(1, cid)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) rs.toClub() else null
                }
            }
        }

    /**
     * Função que vai servir para obter todos os clubs existentes.
     *
     * Itera sobre o ResultSet e adiciona cada club à lista de ClubsName.
     *
     * @return A lista de todos os clubs existentes.
     */
    override fun getClubs(): List<ClubsName> =
        DatabaseConnection.getConnection().use { con ->
            con.prepareStatement(SQL_CLUBS).use { stmt ->
                stmt.executeQuery().use { rs ->
                    rs.toClubsNameList()
                }
            }
        }

    /**
     * Função que vai servir para pesquisar clubs pelo nome.
     *
     * Guarda no val query o nome do club em letras minúsculas e sem espaços.
     * Filtra a lista de Clubs, verificando se o nome do club em letras minúsculas e sem espaços contém o query.
     * Mapeia os resultados filtrados para uma lista de ClubsName, contendo o nome e o ID do club.
     * Por fim, retorna a lista de ClubsName.
     *
     * @param name O nome do club a ser pesquisado.
     * @return A lista de clubs cujo nome contém a string pesquisada.
     */
    override fun searchClubsByName(name: String): List<ClubsName> {
        val query = name.lowercase().replace(" ", "")
        return getClubs().filter { club ->
            club.name
                .lowercase()
                .replace(" ", "")
                .contains(query)
        }
    }
}
