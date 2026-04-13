package pt.isel.daw.repo.mem

import pt.isel.daw.PasswordValidationInfo
import pt.isel.daw.Token
import pt.isel.daw.TokenValidationInfo
import pt.isel.daw.User
import pt.isel.daw.UserStats
import pt.isel.daw.repo.RepositoryUser
import java.time.Instant

class RepositoryUserInMem : RepositoryUser {
    private val users =
        mutableListOf<User>(
            User(
                id = 1,
                name = "PaulAtreides",
                email = "paul@atreides.com",
                passwordValidation =
                    PasswordValidationInfo(
                        validationInfo = "\$2a\$10\$voE2l/diDCqtba7fO6jYa.NMMjHjTDfck.O80G7WwlDEospa/E59i",
                    ),
                balance = 500,
                isAdmin = true,
            ),
            User(
                id = 2,
                name = "JohnDoe",
                email = "johndoe@gmail.com",
                passwordValidation =
                    PasswordValidationInfo(
                        validationInfo = "$2a$10$93vShUl45qpuDAlxWRW7HOggBVkK36WmP8Yw1JE5tfafklgP8pZ/a",
                    ),
                balance = 500,
                isAdmin = false,
            ),
        )
    private val tokens = mutableListOf<Token>()
    private val userStats =
        mutableListOf<UserStats>(
            UserStats(id = 1),
            UserStats(id = 2, winrate = 0.24),
        )

    override fun createUser(
        name: String,
        email: String,
        passwordValidation: PasswordValidationInfo,
    ): User =
        User(users.size + 1, name, email, passwordValidation).also {
            users.add(it)
            createStatsForNewUser(it.id)
            println(users)
        }

    override fun createStatsForNewUser(userId: Int) {
        userStats.add(
            UserStats(
                id = userId,
            ),
        )
    }

    override fun findByEmail(email: String): User? = users.find { it.email == email }

    override fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>? =
        tokens.firstOrNull { it.tokenValidationInfo == tokenValidationInfo }?.let {
            val user = getById(it.userId)
            requireNotNull(user)
            user to it
        }

    override fun createToken(
        token: Token,
        maxTokens: Int,
    ) {
        val nrOfTokens = tokens.count { it.userId == token.userId }

        // Remove the oldest token if we have achieved the maximum number of tokens
        if (nrOfTokens >= maxTokens) {
            tokens
                .filter { it.userId == token.userId }
                .minByOrNull { it.lastUsedAt }!!
                .also { tk -> tokens.removeIf { it.tokenValidationInfo == tk.tokenValidationInfo } }
        }
        tokens.add(token)
    }

    override fun updateTokenLastUsed(
        token: Token,
        now: Instant,
    ) {
        tokens.removeIf { it.tokenValidationInfo == token.tokenValidationInfo }
        tokens.add(token)
    }

    override fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int {
        val count = tokens.count { it.tokenValidationInfo == tokenValidationInfo }
        tokens.removeAll { it.tokenValidationInfo == tokenValidationInfo }
        return count
    }

    override fun getUserStatsById(id: Int): UserStats? = userStats.find { it.id == id }

    override fun saveUserStats(entity: UserStats) {
        userStats.removeIf { it.id == entity.id }
        userStats.add(entity)
    }

    override fun userStatsReset(id: Int) = userStats.removeIf { it.id == id }

    override fun calculateWinrate(stats: UserStats): UserStats {
        val winrate =
            if (stats.total_matches == 0) {
                0.0
            } else {
                stats.matches_won / stats.total_matches.toDouble()
            }

        return stats.copy(winrate = winrate)
    }

    override fun getById(id: Int): User? = users.find { it.id == id }

    override fun getAll(): List<User> = users.toList()

    override fun save(entity: User) {
        users.removeIf { it.id == entity.id }
        users.add(entity)
    }

    override fun deleteById(id: Int) = users.removeIf { it.id == id }

    override fun clear() {
        users.clear()
        tokens.clear()
    }
}
