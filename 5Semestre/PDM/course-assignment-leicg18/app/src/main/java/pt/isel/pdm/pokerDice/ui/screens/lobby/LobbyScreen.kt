package pt.isel.pdm.pokerDice.ui.screens.lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.isel.pdm.pokerDice.R
import pt.isel.pdm.pokerDice.ui.TopBar
import pt.isel.pdm.pokerDice.ui.screens.lobbyViewModelPreview
import pt.isel.pdm.pokerDice.viewModels.lobby.LobbyViewModel

const val CREATE_LOBBY_BUTTON_TAG = "CreateLobbyButton"
const val LAZY_COLUMN_LOBBIES_TAG = "LazyColumnLobbies"
const val LOBBY_DETAILS_BUTTON_TAG = "LobbyDetailsButton"

@Composable
fun LobbyScreen(
    modifier: Modifier = Modifier,
    onNavigateToTitle: () -> Unit = {},
    onNavigateToLobbyCreation: () -> Unit = {},
    onNavigateToLobbyDetails: (lobbyId: Int) -> Unit = {},
    viewModel: LobbyViewModel = viewModel(),
) {
    viewModel.fetchAvailableLobbies()
    val lobbies = viewModel.availableLobbies.collectAsState().value

    Scaffold(
        topBar = {
            TopBar(
                onBackIntent = onNavigateToTitle
            )
        },
        bottomBar = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(
                    onClick = onNavigateToLobbyCreation,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(0.7f)
                        .navigationBarsPadding()
                        .testTag(CREATE_LOBBY_BUTTON_TAG),
                ) {
                    Text(text = stringResource(id = R.string.create_lobby))
                }
            }

        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag(LAZY_COLUMN_LOBBIES_TAG),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            state = rememberLazyListState()
        ) {
            items(lobbies) { lobby ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = lobby.name,
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.players) + ": ${lobby.players.size}/${lobby.maxPlayers}",
                            modifier = Modifier.testTag("PlayersText_${lobby.id}")
                        )
                        Text(
                            text = stringResource(R.string.ante) + ": ${lobby.ante}",
                            modifier = Modifier.testTag("AnteText_${lobby.id}")
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.joinLobby(lobby)
                            onNavigateToLobbyDetails(lobby.id)
                        },
                        modifier = Modifier.testTag(LOBBY_DETAILS_BUTTON_TAG)
                    ) {
                        Text(text = stringResource(R.string.join))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun LobbyScreenPreview() {
    LobbyScreen(
        onNavigateToLobbyDetails = {},
        onNavigateToLobbyCreation = {},
        onNavigateToTitle = {},
        viewModel = lobbyViewModelPreview
    )
}