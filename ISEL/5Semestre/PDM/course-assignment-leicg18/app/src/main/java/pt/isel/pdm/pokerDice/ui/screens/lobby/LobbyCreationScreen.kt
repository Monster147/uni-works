package pt.isel.pdm.pokerDice.ui.screens.lobby

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.isel.pdm.pokerDice.domain.isValidLobbyCreationData
import pt.isel.pdm.pokerDice.ui.TopBar
import pt.isel.pdm.pokerDice.ui.screens.lobbyCreationViewModelPreview
import pt.isel.pdm.pokerDice.viewModels.lobby.LobbyCreationState
import pt.isel.pdm.pokerDice.viewModels.lobby.LobbyCreationViewModel

@Composable
fun LobbyCreationScreen(
    modifier: Modifier = Modifier,
    viewModel: LobbyCreationViewModel = viewModel(),
    onNavigateToLobbies: () -> Unit = {},
    onNavigateToLobbyDetails: (lobbyId: Int) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopBar(
                onBackIntent = onNavigateToLobbies
            )
        }
    ) { innerPadding ->

        val lobbyState = viewModel.lobbyCreationState

        LaunchedEffect(lobbyState) {
            if (lobbyState is LobbyCreationState.Success) {
                onNavigateToLobbyDetails(lobbyState.lobby.id)
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding)
                .padding(horizontal = 48.dp)
                .imePadding()
        ) {

            LobbyCreationForm(
                loading = lobbyState is LobbyCreationState.Loading,
                error = (lobbyState as? LobbyCreationState.Error)?.message,
                validateData = ::isValidLobbyCreationData,
                onCreateLobby = { lobbyCreation ->
                    viewModel.createLobby(lobbyCreation)
                },
                modifier = modifier
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LobbyCreationScreenPreview() {
    LobbyCreationScreen(viewModel = lobbyCreationViewModelPreview)
}