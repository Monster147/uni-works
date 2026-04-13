package pt.isel.pdm.pokerDice.viewModels.login_register.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class AuthInfo(val userEmail: String, val authToken: String, val password: String)

interface AuthInfoRepo {

    val authInfo: Flow<AuthInfo?>

    suspend fun saveAuthInfo(authInfo: AuthInfo)

    suspend fun getAuthInfo(): AuthInfo?

    suspend fun clearAuthInfo()
}

class FakeAuthInfoRepo : AuthInfoRepo {
    private var storedAuthInfo: AuthInfo? = null

    override val authInfo: Flow<AuthInfo?>
        get() = flow { emit(storedAuthInfo) }

    override suspend fun saveAuthInfo(authInfo: AuthInfo) {
        storedAuthInfo = authInfo
    }

    override suspend fun getAuthInfo(): AuthInfo? {
        return storedAuthInfo
    }

    override suspend fun clearAuthInfo() {
        storedAuthInfo = null
    }
}