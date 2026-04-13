package pt.isel.pdm.pokerDice.services

import pt.isel.pdm.pokerDice.domain.PasswordValidationInfo
import pt.isel.pdm.pokerDice.domain.Token
import pt.isel.pdm.pokerDice.domain.TokenExternalInfo
import pt.isel.pdm.pokerDice.domain.User
import pt.isel.pdm.pokerDice.domain.UserStats
import pt.isel.pdm.pokerDice.domain.UsersDomainConfig
import pt.isel.pdm.pokerDice.repo.TransactionManager
import pt.isel.pdm.pokerDice.services.utils.PasswordEnconder
import pt.isel.pdm.pokerDice.services.utils.TokenEncoder
import java.security.SecureRandom
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.Base64.getUrlDecoder
import java.util.Base64.getUrlEncoder

const val SPECIAL_CHARACTERS = "!@#\$%^&*()-_=+[]{}|\\:;\"'<>,.?/"

sealed class UserError {
    data object AlreadyUsedEmailAddress : UserError()

    data object InsecurePassword : UserError()
}

sealed class TokenCreationError {
    data object UserOrPasswordAreInvalid : TokenCreationError()
}

class UserServices(
    private val passwordEncoder: PasswordEnconder,
    private val tokenEncoder: TokenEncoder,
    private val config: UsersDomainConfig,
    private val trxManager: TransactionManager,
    private val clock: Clock,
) : UserServiceInterface {

    private val userStats = mutableListOf<UserStats>(
        UserStats(id = 1000),
    )
    override fun validatePassword(
        password: String,
        validationInfo: PasswordValidationInfo,
    ) = passwordEncoder.matches(
        password,
        validationInfo.validationInfo,
    )

    override fun createPasswordValidationInformation(password: String) =
        PasswordValidationInfo(
            validationInfo = passwordEncoder.encode(password),
        )

    override fun isSafePassword(password: String) =
        password.length >= 8 &&
                password.any { it.isDigit() } &&
                password.any { it.isLowerCase() } &&
                password.any { it.isUpperCase() } &&
                password.any { SPECIAL_CHARACTERS.contains(it) }

    override suspend fun createUser(
        name: String,
        email: String,
        password: String,
        inviteCode: String,
    ): Either<UserError, User> {
        if (!isSafePassword(password)) {
            return failure(UserError.InsecurePassword)
        }

        val passwordValidationInfo = createPasswordValidationInformation(password)

        return trxManager.run {
            if (repoUsers.findByEmail(email) != null) {
                return@run failure(UserError.AlreadyUsedEmailAddress)
            }
            val player = repoUsers.createUser(name, email, passwordValidationInfo)
            success(player)
        }
    }

    override suspend fun createToken(
        email: String,
        password: String,
    ): Either<TokenCreationError, TokenExternalInfo> {
        if ((email.isBlank()) || (password.isBlank())) {
            return failure(TokenCreationError.UserOrPasswordAreInvalid)
        }

        return trxManager.run {
            val user =
                repoUsers.findByEmail(email)
                    ?: return@run failure(TokenCreationError.UserOrPasswordAreInvalid)

            if (/*password != user.passwordValidation.validationInfo*/
                !validatePassword(password, user.passwordValidation)
            ) {
                return@run failure(TokenCreationError.UserOrPasswordAreInvalid)
            }

            val tokenValue = generateTokenValue()
            val now = clock.instant()
            val newToken =
                Token(
                    tokenEncoder.createValidationInformation(tokenValue),
                    user.id,
                    createdAt = now,
                    lastUsedAt = now,
                )
            repoUsers.createToken(newToken, config.maxTokensPerUser)
            Either.Right(
                TokenExternalInfo(
                    tokenValue,
                    //getTokenExpiration(newToken),
                ),
            )
        }
    }

    override suspend fun revokeToken(token: String): Boolean {
        val tokenValidationInfo = tokenEncoder.createValidationInformation(token)
        return trxManager.run {
            repoUsers.removeTokenByValidationInfo(tokenValidationInfo)
            true
        }
    }

    override suspend fun getUserByToken(token: String): User? {
        if (!canBeToken(token)) {
            return null
        }
        return trxManager.run {
            val tokenValidationInfo = tokenEncoder.createValidationInformation(token)
            val userAndToken: Pair<User, Token>? =
                repoUsers.getTokenByTokenValidationInfo(tokenValidationInfo)
            if (userAndToken != null && isTokenTimeValid(clock, userAndToken.second)) {
                repoUsers.updateTokenLastUsed(userAndToken.second, clock.instant())
                userAndToken.first
            } else {
                null
            }
        }
    }

    override fun canBeToken(token: String): Boolean =
        try {
            getUrlDecoder().decode(token).size == config.tokenSizeInBytes
        } catch (ex: IllegalArgumentException) {
            false
        }

    override fun isTokenTimeValid(
        clock: Clock,
        token: Token,
    ): Boolean {
        val now = clock.instant()
        return token.createdAt <= now &&
                Duration.between(now, token.createdAt) <= config.tokenTtl &&
                Duration.between(now, token.lastUsedAt) <= config.tokenRollingTtl
    }

    override fun generateTokenValue(): String =
        ByteArray(config.tokenSizeInBytes).let { byteArray ->
            SecureRandom.getInstanceStrong().nextBytes(byteArray)
            getUrlEncoder().encodeToString(byteArray)
        }

    override fun getTokenExpiration(token: Token): Instant {
        val absoluteExpiration = token.createdAt + config.tokenTtl
        val rollingExpiration = token.lastUsedAt + config.tokenRollingTtl
        return if (absoluteExpiration < rollingExpiration) {
            absoluteExpiration
        } else {
            rollingExpiration
        }
    }

    override suspend fun getUserStats(userId: Int): UserStats? = userStats.find { it.id == userId }
}
