package pt.isel.pdm.pokerDice.services

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import pt.isel.pdm.pokerDice.domain.PasswordValidationInfo
import pt.isel.pdm.pokerDice.domain.Token
import pt.isel.pdm.pokerDice.domain.TokenExternalInfo
import pt.isel.pdm.pokerDice.domain.User
import pt.isel.pdm.pokerDice.domain.UserStats
import pt.isel.pdm.pokerDice.services.dto.TokenExternalInfoDTO
import pt.isel.pdm.pokerDice.services.dto.UserDTO
import pt.isel.pdm.pokerDice.services.dto.UserStatsDTO
import pt.isel.pdm.pokerDice.services.utils.APIConfig
import java.net.URL
import java.time.Clock
import java.time.Instant

class UsersAPIServices(
    private val client: HttpClient,
    private val getToken: suspend () -> String?
) : UserServiceInterface {

    private val baseUrl = APIConfig.BASE_URL

    override suspend fun createUser(
        name: String,
        email: String,
        password: String,
        inviteCode: String,
    ): Either<UserError, User> {
        try{
            val response = client.post(url = URL("$baseUrl/api/users")) {
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "name" to name,
                        "email" to email,
                        "password" to password,
                        "invite_code" to inviteCode
                    )
                )
            }

            when(response.status.value){
                201 -> {
                    val user = response.body<UserDTO>().toUser()
                    return success(user)
                }
                400 -> {
                    return failure(UserError.AlreadyUsedEmailAddress)
                }
                else -> {
                    return failure(UserError.AlreadyUsedEmailAddress)
                }
            }

        } catch (e: Exception) {
            return failure(UserError.AlreadyUsedEmailAddress)
        }
        // TODO("Not yet implemented")
    }

    override suspend fun createToken(
        email: String,
        password: String
    ): Either<TokenCreationError, TokenExternalInfo> {
        try{
            val response = client.post(url = URL("$baseUrl/api/users/token")) {
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "email" to email,
                        "password" to password
                    )
                )
            }

            when(response.status.value){
                200 -> {
                    val tokenInfo = response.body<TokenExternalInfoDTO>().toTokenExternalInfo()
                    return success(tokenInfo)
                }
                401 -> {
                    println("Unauthorized: Invalid email or password  1")
                    return failure(TokenCreationError.UserOrPasswordAreInvalid)
                }
                else -> {
                    println("Unauthorized: Invalid email or password  2")
                    return failure(TokenCreationError.UserOrPasswordAreInvalid)
                }
            }

        } catch (e: Exception) {
            println(e)
            println("Unauthorized: Invalid email or password  3")
            return failure(TokenCreationError.UserOrPasswordAreInvalid)
        }
    }

    override suspend fun revokeToken(token: String): Boolean {
        try{
            val response = client.post("$baseUrl/api/logout") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            return when(response.status){
                HttpStatusCode.OK -> true
                else -> false
            }
        } catch (e: Exception) {
            return false
        }
        //TODO("Not yet implemented")
    }

    override suspend fun getUserByToken(token: String): User? {
        try{
            val token = getToken() ?: return null
            val response = client.get("$baseUrl/api/me") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
            }

            when(response.status){
                HttpStatusCode.OK -> {
                    val user = response.body<UserDTO>().toUser()
                    return user
                }
                HttpStatusCode.Unauthorized -> {
                    return null
                }
                else -> {
                    return null
                }
            }

        } catch (e: Exception) {
            return null
        }
    }

    override suspend fun getUserStats(userId: Int): UserStats? {
        try{
            val token = getToken() ?: return null
            val response = client.get("$baseUrl/api/me/stats") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
            }
            when(response.status){
                HttpStatusCode.OK -> {
                    val stats = response.body<UserStatsDTO>().toUserStats()
                    println("User stats: $stats")
                    return stats
                }
                HttpStatusCode.Unauthorized -> {
                    return null
                }
                else -> {
                    return null
                }
            }
        } catch (e: Exception) {
            return null
        }
    }

    // useless functions for API service
    override fun canBeToken(token: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun isTokenTimeValid(
        clock: Clock,
        token: Token
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun generateTokenValue(): String {
        TODO("Not yet implemented")
    }

    override fun getTokenExpiration(token: Token): Instant {
        TODO("Not yet implemented")
    }

    override fun validatePassword(
        password: String,
        validationInfo: PasswordValidationInfo
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun createPasswordValidationInformation(password: String): PasswordValidationInfo {
        TODO("Not yet implemented")
    }

    override fun isSafePassword(password: String): Boolean {
        TODO("Not yet implemented")
    }
}