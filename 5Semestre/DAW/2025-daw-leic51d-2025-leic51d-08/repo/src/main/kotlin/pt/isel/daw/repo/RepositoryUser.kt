package pt.isel.daw.repo

import pt.isel.daw.PasswordValidationInfo
import pt.isel.daw.Token
import pt.isel.daw.TokenValidationInfo
import pt.isel.daw.User
import pt.isel.daw.UserStats
import java.time.Instant

interface RepositoryUser : Repository<User> {
    fun createUser(
        name: String,
        email: String,
        passwordValidation: PasswordValidationInfo,
    ): User

    fun createStatsForNewUser(userId: Int)

    fun findByEmail(email: String): User?

    fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>?

    fun createToken(
        token: Token,
        maxTokens: Int,
    )

    fun updateTokenLastUsed(
        token: Token,
        now: Instant,
    )

    fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int

    fun getUserStatsById(id: Int): UserStats?

    fun saveUserStats(entity: UserStats)

    fun userStatsReset(id: Int): Boolean

    fun calculateWinrate(stats: UserStats): UserStats
}
