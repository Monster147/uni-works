package pt.isel.pdm.pokerDice.viewModels.login_register.infrastructure

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.AuthInfo
import pt.isel.pdm.pokerDice.viewModels.login_register.domain.AuthInfoRepo

class AuthInfoPreferencesRepo(private val store: DataStore<Preferences>) : AuthInfoRepo {

    private val USER_EMAIL_KEY: Preferences.Key<String> = stringPreferencesKey(name = "user_email")
    private val TOKEN_KEY: Preferences.Key<String> = stringPreferencesKey(name = "auth_token")
    private val PASSWORD_KEY: Preferences.Key<String> = stringPreferencesKey("user_password")

    override val authInfo: Flow<AuthInfo?> =
        store.data.map { preferences ->
            preferences.toAuthInfo()
        }

    override suspend fun saveAuthInfo(authInfo: AuthInfo) {
        store.edit { preferences ->
            preferences[USER_EMAIL_KEY] = authInfo.userEmail
            preferences[TOKEN_KEY] = authInfo.authToken
            preferences[PASSWORD_KEY] = obfuscate(authInfo.password)
        }
    }

    override suspend fun getAuthInfo(): AuthInfo? {
        val preferences: Preferences = store.data.first()
        return preferences.toAuthInfo()
    }

    override suspend fun clearAuthInfo() {
        store.edit { it.clear() }
    }

    fun Preferences.toAuthInfo(): AuthInfo? =
        this[USER_EMAIL_KEY]?.let {
            val token = this[TOKEN_KEY] ?: return null
            AuthInfo(
                userEmail = it,
                authToken = token,
                password = this[PASSWORD_KEY]?.let { obfPwd ->
                    deobfuscate(obfPwd)
                } ?: ""
            )
        }

    private fun obfuscate(pass: String): String =
        Base64.encodeToString(pass.toByteArray(), Base64.DEFAULT)

    private fun deobfuscate(passEncoded: String): String =
        String(Base64.decode(passEncoded, Base64.DEFAULT))
}