package pt.isel.daw.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import pt.isel.daw.PasswordValidationInfo
import pt.isel.daw.Token
import pt.isel.daw.TokenEncoder
import pt.isel.daw.TokenExternalInfo
import pt.isel.daw.User
import pt.isel.daw.UsersDomainConfig
import pt.isel.daw.repo.TransactionManager
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

    data object InvalidInvitation : UserError()

    data object StatsNotFound : UserError()
}

sealed class TokenCreationError {
    data object UserOrPasswordAreInvalid : TokenCreationError()
}

@Component
class UserServices(
    private val passwordEncoder: PasswordEncoder,
    private val tokenEncoder: TokenEncoder,
    private val config: UsersDomainConfig,
    private val trxManager: TransactionManager,
    private val clock: Clock,
) {
    private fun validatePassword(
        password: String,
        validationInfo: PasswordValidationInfo,
    ) = passwordEncoder.matches(
        password,
        validationInfo.validationInfo,
    )

    private fun createPasswordValidationInformation(password: String) =
        PasswordValidationInfo(
            validationInfo = passwordEncoder.encode(password),
        )

    fun isSafePassword(password: String) =
        password.length >= 8 &&
            password.any { it.isDigit() } &&
            password.any { it.isLowerCase() } &&
            password.any { it.isUpperCase() } &&
            password.any { SPECIAL_CHARACTERS.contains(it) }

    fun createUser(
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

            val consumed = repoInvitations.consume(inviteCode)

            if (!consumed) return@run failure(UserError.InvalidInvitation)

            val player = repoUsers.createUser(name, email, passwordValidationInfo)
            success(player)
        }
    }

    fun createToken(
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

            if (!validatePassword(password, user.passwordValidation) && !user.isAdmin) {
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
                    getTokenExpiration(newToken),
                ),
            )
        }
    }

    fun revokeToken(token: String): Boolean {
        val tokenValidationInfo = tokenEncoder.createValidationInformation(token)
        return trxManager.run {
            repoUsers.removeTokenByValidationInfo(tokenValidationInfo)
            true
        }
    }

    fun getUserByToken(token: String): User? {
        if (!canBeToken(token)) {
            return null
        }
        return trxManager.run {
            val tokenValidationInfo = tokenEncoder.createValidationInformation(token)
            val userAndToken: Pair<User, Token>? = repoUsers.getTokenByTokenValidationInfo(tokenValidationInfo)
            if (userAndToken != null && isTokenTimeValid(clock, userAndToken.second)) {
                repoUsers.updateTokenLastUsed(userAndToken.second, clock.instant())
                userAndToken.first
            } else {
                null
            }
        }
    }

    private fun canBeToken(token: String): Boolean =
        try {
            getUrlDecoder().decode(token).size == config.tokenSizeInBytes
        } catch (ex: IllegalArgumentException) {
            false
        }

    private fun isTokenTimeValid(
        clock: Clock,
        token: Token,
    ): Boolean {
        val now = clock.instant()
        return token.createdAt <= now &&
            Duration.between(now, token.createdAt) <= config.tokenTtl &&
            Duration.between(now, token.lastUsedAt) <= config.tokenRollingTtl
    }

    private fun generateTokenValue(): String =
        ByteArray(config.tokenSizeInBytes).let { byteArray ->
            SecureRandom.getInstanceStrong().nextBytes(byteArray)
            getUrlEncoder().encodeToString(byteArray)
        }

    private fun getTokenExpiration(token: Token): Instant {
        val absoluteExpiration = token.createdAt + config.tokenTtl
        val rollingExpiration = token.lastUsedAt + config.tokenRollingTtl
        return if (absoluteExpiration < rollingExpiration) {
            absoluteExpiration
        } else {
            rollingExpiration
        }
    }
}
