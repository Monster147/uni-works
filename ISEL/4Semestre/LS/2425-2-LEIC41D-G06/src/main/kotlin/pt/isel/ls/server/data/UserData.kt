package pt.isel.ls.server.data

import pt.isel.ls.server.domain.User
import java.util.UUID

/**
 * Interface que define os métodos para manipulação de dados relacionados com [User].
 */
interface UserData {
    fun createNewUser(
        name: String,
        email: String,
        password: String,
    ): Pair<UUID, Int>

    fun getUserDetails(uid: Int): User?

    fun getUserIdByToken(token: String): Int?

    fun getTokenByUserEmail(email: String): String?

    fun getUserTokenAndId(
        name: String,
        password: String,
    ): Pair<String, Int>?
}
