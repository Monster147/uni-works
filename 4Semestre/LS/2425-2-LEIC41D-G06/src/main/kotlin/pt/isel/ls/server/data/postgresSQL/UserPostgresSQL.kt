package pt.isel.ls.server.data.postgresSQL

import org.eclipse.jetty.util.security.Password
import pt.isel.ls.server.common.AppError
import pt.isel.ls.server.common.Errors
import pt.isel.ls.server.data.UserData
import pt.isel.ls.server.data.postgresSQL.utils.DatabaseConnection
import pt.isel.ls.server.data.postgresSQL.utils.SQL_CREATE_USER
import pt.isel.ls.server.data.postgresSQL.utils.SQL_USERID_AND_TOKEN
import pt.isel.ls.server.data.postgresSQL.utils.SQL_USER_DETAILS_BY_EMAIL
import pt.isel.ls.server.data.postgresSQL.utils.SQL_USER_DETAILS_BY_ID
import pt.isel.ls.server.data.postgresSQL.utils.SQL_USER_DETAILS_BY_TOKEN
import pt.isel.ls.server.data.postgresSQL.utils.checkDuplicateEntity
import pt.isel.ls.server.domain.User
import java.sql.ResultSet
import java.util.UUID

/**
 * Classe que implementa a interface [UserData] para manipulação de dados de users numa base de dados.
 * Contém métodos para criar um user, obter os detalhes de um determinado user
 * e obter o token e o id de um user.
 */
class UserPostgresSQL : UserData {
    /**
     * Função que vai servir para converter um ResultSet em um objeto User.
     *
     * @return Um objeto [User] com os dados do ResultSet.
     */
    private fun ResultSet.toUser() =
        User(
            getInt("uid"),
            getString("name"),
            getString("email"),
            getString("token"),
            Password.deobfuscate(getString("password")),
        )

    /**
     * Função que cria um user.
     *
     * Verifica se já existe um user com o mesmo email, se sim, lança um erro de user duplicado.
     * Guarda no val token um UUID aleatório.
     * Guarda no val hashedPassword a password obfuscada (cifrada).
     * Guarda no val user um novo objeto User criado por meio de uma consulta SQL.
     * Caso ocorra algum erro durante a criação do user, a transação é revertida e o erro é lançado.
     * Efetua o commit da transação.
     * Por fim, retorna um Pair com o token e o ID do novo user criado.
     *
     * @param name O nome do novo user.
     * @param email O email do novo user.
     * @param password A password do novo user.
     * @return Um Pair com o token do novo [User] e o ID do novo user criado.
     */
    override fun createNewUser(
        name: String,
        email: String,
        password: String,
    ): Pair<UUID, Int> =
        DatabaseConnection.getConnection().use { con ->
            try {
                checkDuplicateEntity(con, "utilizador", mapOf("email" to email))
                val token = UUID.randomUUID()
                val hashedPassword = Password.obfuscate(password)
                val user =
                    con
                        .prepareStatement(SQL_CREATE_USER)
                        .use { stmt ->
                            stmt.setString(1, name)
                            stmt.setString(2, email)
                            stmt.setString(3, token.toString())
                            stmt.setString(4, hashedPassword)
                            stmt.executeQuery().use { rs ->
                                if (rs.next()) rs.toUser() else throw Errors.serverError()
                            }
                        }
                con.commit()
                token to user.uid
            } catch (e: AppError) {
                con.rollback()
                throw e
            }
        }

    /**
     * Função que obtém os detalhes de um user específico.
     *
     * Verifica se existe um user com o ID fornecido.
     * Se existir, retorna o user correspondente, caso contrário, retorna null.
     *
     * @param uid O ID do user cujos detalhes pretende-se obter.
     * @return Os detalhes do [User] ou null se o user não existir.
     */
    override fun getUserDetails(uid: Int): User? =
        DatabaseConnection.getConnection().use { con ->
            con.prepareStatement(SQL_USER_DETAILS_BY_ID).use { stmt ->
                stmt.setInt(1, uid)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) rs.toUser() else null
                }
            }
        }

    /**
     * Função que obtém o ID do user associado a um token.
     *
     * Verifica se existe um user com o token fornecido.
     * Se existir, retorna o ID do user correspondente, caso contrário, retorna null.
     *
     * @param token O token do user cujo ID se pretende obter.
     * @return O ID do [User] ou null se o token não existir.
     */
    override fun getUserIdByToken(token: String): Int? =
        DatabaseConnection.getConnection().use { con ->
            con.prepareStatement(SQL_USER_DETAILS_BY_TOKEN).use { stmt ->
                stmt.setString(1, token)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) rs.getInt("uid") else null
                }
            }
        }

    override fun getTokenByUserEmail(email: String): String? =
        DatabaseConnection.getConnection().use { con ->
            con.prepareStatement(SQL_USER_DETAILS_BY_EMAIL).use { stmt ->
                stmt.setString(1, email)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) rs.getString("token") else null
                }
            }
        }

    /**
     * Função que obtém o token e o ID de um user a partir do seu nome e password.
     *
     * Realiza a pesquisa na base de dados para encontrar um user com o nome e a password fornecidos.
     * Se encontrar, retorna um Pair com o token e o ID do user correspondente.
     * Caso contrário, retorna null.
     *
     * @param name O nome do user.
     * @param password A password do user.
     * @return Um Pair com o token e o ID do [User] ou null se não for encontrado.
     */
    override fun getUserTokenAndId(
        name: String,
        password: String,
    ): Pair<String, Int>? =
        DatabaseConnection.getConnection().use { con ->
            con.prepareStatement(SQL_USERID_AND_TOKEN).use { stmt ->
                stmt.setString(1, name)
                stmt.setString(2, Password.obfuscate(password))
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        rs.getString("token") to rs.getInt("uid")
                    } else {
                        null
                    }
                }
            }
        }
}
