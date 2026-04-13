package pt.isel.pdm.pokerDice.repo.mem

import pt.isel.pdm.pokerDice.domain.PasswordValidationInfo
import pt.isel.pdm.pokerDice.domain.Token
import pt.isel.pdm.pokerDice.domain.TokenValidationInfo
import pt.isel.pdm.pokerDice.domain.User
import pt.isel.pdm.pokerDice.repo.RepositoryUser
import pt.isel.pdm.pokerDice.services.utils.SimpleSha256PasswordEnconder
import java.time.Instant

class RepositoryUserInMem : RepositoryUser {
    private val passwordEncoder = SimpleSha256PasswordEnconder()
    private val users = mutableListOf<User>(
        User(
            id = 1,
            name = "JohnDoe",
            email = "johndoe@gmail.com",
            passwordValidation = PasswordValidationInfo("uGzp3y+WRN9/+c6B/dLUed+/JKVXDPIAD8sgJQODhL4=")
        ),
        User(1000, "Alice", "alice@gmail.com", PasswordValidationInfo(passwordEncoder.encode("hash"))),
        User(1001, "Bob", "bob@gmail.com", PasswordValidationInfo(passwordEncoder.encode("hash"))),
        User(1002, "Charlie", "charlie@gmail.com", PasswordValidationInfo(passwordEncoder.encode("hash"))),
        User(1003, "Dave", "dave@gmail.com", PasswordValidationInfo(passwordEncoder.encode("hash")))
    )
    private val tokens = mutableListOf<Token>()

    override fun createUser(
        name: String,
        email: String,
        passwordValidation: PasswordValidationInfo,
    ): User =
        User(users.size + 1, name, email, passwordValidation).also {
            users.add(it)
            println(users)
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
