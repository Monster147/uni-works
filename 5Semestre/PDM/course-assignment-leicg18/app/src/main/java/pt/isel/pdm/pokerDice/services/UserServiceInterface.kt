package pt.isel.pdm.pokerDice.services

import pt.isel.pdm.pokerDice.domain.PasswordValidationInfo
import pt.isel.pdm.pokerDice.domain.Token
import pt.isel.pdm.pokerDice.domain.TokenExternalInfo
import pt.isel.pdm.pokerDice.domain.User
import pt.isel.pdm.pokerDice.domain.UserStats
import java.time.Clock
import java.time.Instant

interface UserServiceInterface {

    fun validatePassword(
        password: String,
        validationInfo: PasswordValidationInfo,
    ): Boolean


    fun createPasswordValidationInformation(password: String): PasswordValidationInfo

    fun isSafePassword(password: String): Boolean

    suspend fun createUser(
        name: String,
        email: String,
        password: String,
        inviteCode: String,
    ): Either<UserError, User>

    suspend fun createToken(
        email: String,
        password: String,
    ): Either<TokenCreationError, TokenExternalInfo>

    suspend fun revokeToken(token: String): Boolean

    suspend fun getUserByToken(token: String): User?

    fun canBeToken(token: String): Boolean

    fun isTokenTimeValid(
        clock: Clock,
        token: Token,
    ): Boolean

    fun generateTokenValue(): String

    fun getTokenExpiration(token: Token): Instant

    suspend fun getUserStats(userId: Int): UserStats?
}