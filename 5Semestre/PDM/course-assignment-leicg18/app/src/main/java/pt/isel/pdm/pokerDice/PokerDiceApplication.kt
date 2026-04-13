package pt.isel.pdm.pokerDice

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import pt.isel.pdm.pokerDice.domain.UsersDomainConfig
import pt.isel.pdm.pokerDice.repo.mem.TransactionManagerInMem
import pt.isel.pdm.pokerDice.services.utils.SimpleSha256PasswordEnconder
import pt.isel.pdm.pokerDice.services.utils.SimpleTokenEncoder
import pt.isel.pdm.pokerDice.viewModels.login_register.infrastructure.AuthInfoPreferencesRepo
import java.time.Clock
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.sse.SSE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pt.isel.pdm.pokerDice.services.InvitationAPIServices
import pt.isel.pdm.pokerDice.services.LobbyAPIServices
import pt.isel.pdm.pokerDice.services.MatchAPIServices
import pt.isel.pdm.pokerDice.services.UsersAPIServices

class PokerDiceApplication : Application() {
    val trxManager = TransactionManagerInMem()
    val passwordEncoder = SimpleSha256PasswordEnconder()
    val tokenEncoder = SimpleTokenEncoder()
    val userConfig = UsersDomainConfig()
    val clock = Clock.systemUTC()
    private val dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_info")

    private val httpClient by lazy {
        // HTTP engine is chosen by Ktor based on the current dependencies and platform.
        HttpClient (OkHttp) {
            install(plugin = ContentNegotiation) {
                json(
                    json = Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    }
                )
            }

            install(HttpTimeout) {
                requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                socketTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
            }

            install(SSE)
        }
    }

    val authInfoRepo by lazy {
        AuthInfoPreferencesRepo(store = dataStore)
    }

    val lobbyServices by lazy {
        LobbyAPIServices(
            client = httpClient,
            getToken = { authInfoRepo.getAuthInfo()?.authToken }
        )
        /*LobbyServices(trxManager)*/
    }
    val userServices by lazy {
        //UserServices(passwordEncoder, tokenEncoder, userConfig, trxManager, clock)
        UsersAPIServices(
            client = httpClient,
            getToken = { authInfoRepo.getAuthInfo()?.authToken }
        )
    }

    val matchServices by lazy {
        //MatchServices(trxManager)
        MatchAPIServices(
            client = httpClient,
            getToken = { authInfoRepo.getAuthInfo()?.authToken }
        )
    }

    val invitationServices by lazy {
        //InvitationServices(trxManager)
        InvitationAPIServices(
            client = httpClient,
            getToken = { authInfoRepo.getAuthInfo()?.authToken }
        )
    }
}