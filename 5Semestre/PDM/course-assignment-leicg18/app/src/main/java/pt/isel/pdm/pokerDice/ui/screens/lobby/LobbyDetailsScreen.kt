package pt.isel.pdm.pokerDice.ui.screens.lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pt.isel.pdm.pokerDice.R
import pt.isel.pdm.pokerDice.domain.Lobby
import pt.isel.pdm.pokerDice.domain.PasswordValidationInfo
import pt.isel.pdm.pokerDice.domain.User
import pt.isel.pdm.pokerDice.ui.TopBar
import pt.isel.pdm.pokerDice.ui.screens.lobbyViewModelPreview
import pt.isel.pdm.pokerDice.viewModels.lobby.LobbyViewModel
import pt.isel.pdm.pokerDice.viewModels.lobby.MatchState
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.delay
import pt.isel.pdm.pokerDice.domain.LobbyState
import pt.isel.pdm.pokerDice.ui.screens.lobbyDetailsViewModelPreview
import pt.isel.pdm.pokerDice.viewModels.lobby.LobbyDetailsViewModel

const val START_MATCH_BUTTON_TAG = "StartMatchButton"
const val LOBBY_DESCRIPTION_TAG = "LobbyDescription"
const val LOBBY_PLAYERS_INFO_TAG = "LobbyPlayersInfo"
const val LOBBY_ROUNDS_TAG = "LobbyRounds"
const val LOBBY_ANTE_TAG = "LobbyAnte"
const val LOBBY_PLAYERS_TITLE_TAG = "LobbyPlayersTitle"
const val LOBBY_PLAYER_ITEM_PREFIX = "LobbyPlayer_"

@Composable
fun LobbyDetailsScreen(
    onNavigateToLobbies: () -> Unit,
    modifier: Modifier = Modifier,
    lobbyId: Int,
    lobbyDetailsViewModel: LobbyDetailsViewModel,
    onNavigateToMatch: (matchId: Int) -> Unit = {},
    previewLobby: Lobby? = null
) {
    LaunchedEffect(lobbyId){
        lobbyDetailsViewModel.fetchLobbyById(lobbyId)
        lobbyDetailsViewModel.startListeningToLobbyEvents(lobbyId)
    }

    LaunchedEffect(Unit) {
        lobbyDetailsViewModel.navigateBack.collect {
            onNavigateToLobbies()
        }
    }

    val lobby = lobbyDetailsViewModel.currentLobby.collectAsState().value

    if(lobby != null) {

        LaunchedEffect(lobby) {
            lobbyDetailsViewModel.checkHost()
        }

        val isHost = lobbyDetailsViewModel.isHost
        val startedMatchState = lobbyDetailsViewModel.matchState

        LaunchedEffect(startedMatchState) {
            when (startedMatchState) {
                is MatchState.Success -> {
                    onNavigateToMatch(startedMatchState.matchId)
                }

                else -> { /* No action needed */
                }
            }
        }

        Scaffold(
            topBar = {
                TopBar(
                    title = lobby.name,
                    onBackIntent = onNavigateToLobbies
                )
            },
            bottomBar = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Button(
                        modifier = Modifier
                            .testTag(START_MATCH_BUTTON_TAG)
                            .padding(16.dp)
                            .fillMaxWidth(0.7f)
                            .navigationBarsPadding(),
                        enabled = isHost && lobby.players.size >= 2,
                        onClick = {
                            lobbyDetailsViewModel.startMatch(lobby)
                        },
                    ) {
                        Text(text = stringResource(id = R.string.start_match))
                    }
                }
            }
        ) { innerPadding ->

            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.description) + ": ${lobby.description}",
                                modifier = Modifier.testTag(LOBBY_DESCRIPTION_TAG)
                            )

                            Text(
                                text = stringResource(R.string.players) + ": ${lobby.players.size}/${lobby.maxPlayers}",
                                modifier = Modifier.testTag(LOBBY_PLAYERS_INFO_TAG)
                            )

                            Text(
                                text = stringResource(R.string.rounds) + ": ${lobby.rounds}",
                                modifier = Modifier.testTag(LOBBY_ROUNDS_TAG)
                            )

                            Text(
                                text = stringResource(R.string.ante) + ": ${lobby.ante}",
                                modifier = Modifier.testTag(LOBBY_ANTE_TAG)
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.players_lobby) + ":",
                        modifier = Modifier.padding(bottom = 8.dp).testTag(LOBBY_PLAYERS_TITLE_TAG)
                    )
                }

                items(lobby.players) { player ->
                    val isHost = player.id == lobby.host.id
                    Text(
                        text = if (isHost) "${player.name} (Host)" else player.name,
                        modifier = Modifier.testTag("$LOBBY_PLAYER_ITEM_PREFIX${player.id}")
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun LobbyDetailsScreenPreview() {
    val users = listOf(
        User(1, "Alice", "alice@gmail.com", PasswordValidationInfo("hash")),
        User(2, "Bob", "bob@gmail.com", PasswordValidationInfo("hash")),
        User(3, "Charlie", "charlie@gmail.com", PasswordValidationInfo("hash"))
    )

    val lobby = Lobby(
        name = "Preview Lobby",
        description = "A place to have fun playing poker dice!",
        maxPlayers = 5,
        rounds = 10,
        players = users,
        host = users[0],
        id = 1
    )
    LobbyDetailsScreen(
        onNavigateToLobbies = {},
        lobbyId = lobby.id,
        lobbyDetailsViewModel = lobbyDetailsViewModelPreview,
        previewLobby = lobby
    )
}