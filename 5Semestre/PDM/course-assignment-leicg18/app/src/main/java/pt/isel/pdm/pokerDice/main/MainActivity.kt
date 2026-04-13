package pt.isel.pdm.pokerDice.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.navigation.compose.rememberNavController
import pt.isel.pdm.pokerDice.PokerDiceApplication
import pt.isel.pdm.pokerDice.ui.theme.MyApplicationTheme
import pt.isel.pdm.pokerDice.viewModels.MatchViewModel
import pt.isel.pdm.pokerDice.viewModels.UserViewModel
import pt.isel.pdm.pokerDice.viewModels.lobby.LobbyCreationViewModel
import pt.isel.pdm.pokerDice.viewModels.lobby.LobbyViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.LoginViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.RegisterViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val allServices: Map<String, Any> = getAllServices()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PokerDiceApp(
                        navController = rememberNavController(),
                        modifier = Modifier.padding(innerPadding),
                        onRulesClick = {
                            navigateToURL("https://en.wikipedia.org/wiki/Poker_dice#As_a_game")
                        },
                        onGmailClick = {
                            composeEmail(
                                arrayOf(
                                    "a51445@alunos.isel.pt",
                                    "a51447@alunos.isel.pt",
                                    "a51484@alunos.isel.pt"
                                )
                            )
                        },
                        allServices = allServices
                    )
                }
            }
        }
    }

    private fun getAllServices(): Map<String, Any> {
        val app = application as PokerDiceApplication
        return mapOf(
            "lobbyServices" to app.lobbyServices,
            "userServices" to app.userServices,
            "matchServices" to app.matchServices,
            "invitationServices" to app.invitationServices,
            "authInfoRepo" to app.authInfoRepo
        )
    }

    private fun navigateToURL(destination: String) {
        val webpage: Uri = destination.toUri()
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        startActivity(intent)
    }

    fun composeEmail(addresses: Array<String>) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, addresses)
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
}
