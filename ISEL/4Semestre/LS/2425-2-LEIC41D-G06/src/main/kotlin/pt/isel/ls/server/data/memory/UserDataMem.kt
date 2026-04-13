package pt.isel.ls.server.data.memory

import org.eclipse.jetty.util.security.Password
import pt.isel.ls.server.common.Errors
import pt.isel.ls.server.data.UserData
import pt.isel.ls.server.domain.User
import java.util.UUID

/**
 * Classe que implementa a interface [UserData] para manipulação de dados de users em memória.
 * Contém métodos para criar um user, obter os detalhes de um determinado user
 * e obter o token e o id de um user.
 */
class UserDataMem : UserData {
    /**
     * Função que cria um user.
     *
     * Verifica se já existe um user com o mesmo email, se sim, lança um erro de user duplicado.
     * Guarda no val uid o ID do novo user, que é o tamanho atual da lista de users mais 1.
     * Guarda no val token um UUID aleatório.
     * Guarda no val hashedPassword a password obfuscada(cifrada).
     * Guarda no val user um novo objeto User.
     * Adiciona o novo user à lista de users.
     * Por fim, retorna um Pair com o token e o ID do novo user criado.
     *
     * @param name O nome do novo user.
     * @param email O email do novo user.
     * @param password A password do novo user.
     * @return Um Pair com o token do novo user e o ID do novo user criado.
     */
    override fun createNewUser(
        name: String,
        email: String,
        password: String,
    ): Pair<UUID, Int> {
        if (Users.any { it.email == email }) throw Errors.duplicateUser(email)
        val uid = Users.size + 1
        val token = UUID.randomUUID()
        val hashedPassword = Password.obfuscate(password)
        val user = User(uid, name, email, token.toString(), hashedPassword)
        Users.add(user)
        println(Users)
        return token to uid
        // Verifica se o utilizador a ser criado já contem um email presente no array, se não existir, cria um novo utilizador e associa-lhe um id
    }

    /**
     * Função que obtém os detalhes de um user específico.
     *
     * Verifica se existe um user com o ID fornecido.
     * Se existir, retorna o user correspondente, caso contrário, retorna null.
     *
     * @param uid O ID do user cujos detalhes pretende-se obter.
     * @return Os detalhes do user ou null se o user não existir.
     */
    override fun getUserDetails(uid: Int): User? = Users.find { it.uid == uid }

    /**
     * Função que obtém o ID do user associado a um token.
     *
     * Verifica se existe um user com o token fornecido.
     * Se existir, retorna o ID do user correspondente, caso contrário, retorna null.
     *
     * @param token O token do user cujo ID se pretende obter.
     * @return O ID do user ou null se o token não existir.
     */
    override fun getUserIdByToken(token: String): Int? = Users.find { it.token == token }?.uid

    /**
     * Função que obtém o token de um user associado ao seu email.
     *
     * Verifica se existe um user com o email fornecido.
     * Se existir, retorna o token do user correspondente, caso contrário, retorna null.
     *
     * @param email O email do user cujo token se pretende obter.
     * @return O token do user ou null se o email não existir.
     */
    override fun getTokenByUserEmail(email: String): String? = Users.find { it.email == email }?.token

    /**
     * Função que obtém o token e o ID de um user a partir do seu nome e password.
     *
     * Guarda no val user a lista de users filtrada pelo nome e password fornecidos.
     * Se existir, retorna um Pair com o token e o ID do user correspondente.
     * Caso contrário, lança erros específicos se o nome ou a password não forem encontrados.
     *
     * @param name O nome do user.
     * @param password A password do user.
     * @return Um Pair com o token e o ID do user ou null se não for encontrado.
     */
    override fun getUserTokenAndId(
        name: String,
        password: String,
    ): Pair<String, Int>? {
        println(Users)
        val user = Users.find { it.name == name && (it.password) == Password.obfuscate(password) }
        println(Users)
        return if (user != null) {
            user.token to user.uid
        } else {
            if (!Users.any { it.name == name }) {
                throw Errors.nameNotFound(name)
            }
            if (!Users.any { (it.password) == Password.obfuscate(password) }) {
                throw Errors.passwordNotFound()
            } else {
                null
            }
        }
    }
}
