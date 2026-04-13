package pt.isel.ls.server.services

import pt.isel.ls.server.common.Errors
import pt.isel.ls.server.data.UserData
import pt.isel.ls.server.domain.User
import java.util.UUID

/**
 * Classe que define os Services para os Users.
 *
 * Contém métodos para criar um user, obter os detalhes de um determinado user
 * e obter o token e o id de um user.
 *
 * @property userData Interface utilizada para manipulação dos users.
 */
class UserServices(
    private val userData: UserData,
) {
    /**
     * Função que vai servir para criar um novo user.
     *
     * Caso o email esteja vazio, lança um erro a dizer que o parâmetro "email" está em falta.
     * Caso o nome esteja vazio, lança um erro a dizer que o parâmetro "name" está em falta.
     * Caso a password esteja vazia, lança um erro a dizer que o parâmetro "password" está em falta.
     * Caso o nome não corresponda ao regex definido, lança um erro a dizer que o parâmetro "name" é inválido.
     * Caso o email não corresponda ao regex definido, lança um erro a dizer que o parâmetro "email" é inválido.
     * Caso a password tenha menos de 8 caracteres, lança um erro a dizer que a password deve ter pelo menos 8 caracteres.
     * Por fim, chama a função createNewUser do userData para criar o user que retorna um par com o ID do novo user e o token gerado.
     *
     * @param name O nome do novo user.
     * @param email O email do novo user.
     * @param password A password do novo user.
     * @return Um Pair com o ID do novo user e o token gerado.
     */
    fun createNewUser(
        name: String,
        email: String,
        password: String,
    ): Pair<UUID, Int> {
        if (email.isBlank()) throw Errors.missingParameter("email")
        if (name.isBlank()) throw Errors.missingParameter("name")
        if (password.isBlank()) throw Errors.missingParameter("password")
        if (!Regex("^[A-Za-zÀ-ÖØ-öø-ÿ' -]+$").matches(name)) throw Errors.invalidParameter("name")
        if (!Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$").matches(email)) throw Errors.invalidParameter("email")
        if (password.length < 8) throw Errors.invalidParameter("password must have at least 8 characters")
        return userData.createNewUser(name, email, password)
    }

    /**
     * Função que vai servir para obter os detalhes de um determinado user.
     *
     * Caso o ID do user seja menor ou igual a 0, lança um erro a dizer que o parâmetro "uid" é inválido.
     * Por fim, chama a função getUserDetails do userData para obter os detalhes do user.
     *
     * @param uid O ID do user cujos detalhes se pretende obter.
     * @return Os detalhes do user ou null se o user não existir.
     */
    fun getUserDetails(uid: Int): User? {
        if (uid <= 0) throw Errors.invalidParameter("uid")
        return userData.getUserDetails(uid)
    }

    /**
     * Função que vai servir para obter o ID de um user a partir do token.
     *
     * Caso o token esteja vazio, lança um erro a dizer que o parâmetro "token" está em falta.
     * Por fim, chama a função getUserIdByToken do userData para obter o ID do user associado ao token.
     *
     * @param token O token do user.
     * @return O ID do user ou null se o token não for válido.
     */
    fun getUserIdByToken(token: String): Int? {
        if (token.isEmpty()) throw Errors.missingParameter("token")
        return userData.getUserIdByToken(token)
    }

    /**
     * Função que vai servir para obter o token de um user a partir do email.
     *
     * Caso o token esteja vazio, lança um erro a dizer que o parâmetro "token" está em falta.
     * Por fim, chama a função getTokenByUserEmail do userData para obter o ID do user associado ao token.
     *
     * @param email O email do user.
     * @return O ID do user ou null se o token não for válido.
     */
    fun getTokenByUserEmail(email: String): String? {
        if (email.isEmpty()) throw Errors.missingParameter("email")
        return userData.getTokenByUserEmail(email)
    }

    /**
     * Função que vai servir para obter o token e o id de um user.
     *
     * Caso o nome esteja vazio, lança um erro a dizer que o parâmetro "name" está em falta.
     * Caso a password esteja vazia, lança um erro a dizer que o parâmetro "password" está em falta.
     * Por fim, chama a função getUserTokenAndId do userData para obter o token e o id do user.
     *
     * @param name O nome do user.
     * @param password A password do user.
     * @return Um Pair com o token do user e o ID do user ou null se as credenciais não forem válidas.
     */
    fun getUserTokenAndId(
        name: String,
        password: String,
    ): Pair<String, Int>? {
        if (name.isBlank()) throw Errors.missingParameter("name")
        if (password.isBlank()) throw Errors.missingParameter("password")
        return userData.getUserTokenAndId(name, password)
    }
}
