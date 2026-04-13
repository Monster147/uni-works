package pt.isel.pdm.pokerDice.ui.screens.lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pt.isel.pdm.pokerDice.R
import pt.isel.pdm.pokerDice.domain.LobbyCreation

private const val MIN_PLAYERS: Int = 2
private const val MAX_PAYERS: Int = 6
private const val MIN_ROUNDS: Int = 1
private const val MAX_ROUNDS: Int = 60

private const val MIN_ANTE: Int = 10

private const val MAX_ANTE: Int = 750

@Composable
fun LobbyCreationForm(
    loading: Boolean,
    error: String?,
    validateData: (lobbyName: String, description: String, maxPlayers: Int?, nOfRounds: Int?, ante: Int?) -> Boolean,
    onCreateLobby: (lobby: LobbyCreation) -> Unit,
    modifier: Modifier = Modifier
) {
    var lobbyName by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var selectedPlayers by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedRounds by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedAnte by rememberSaveable { mutableStateOf<Int?>(null) }

    val roundsOptions = remember(selectedPlayers) {
        selectedPlayers?.takeIf { it > 0 }?.let { players ->
            val first = (MIN_ROUNDS..MAX_ROUNDS).firstOrNull { it % players == 0 } ?: MIN_ROUNDS
            first..MAX_ROUNDS step players
        } ?: (0..0 step 1)
    }
    val playerOptions = MIN_PLAYERS..MAX_PAYERS
    val anteOptions = MIN_ANTE..MAX_ANTE step 10

    LobbyCreationFormStateless(
        lobbyName = lobbyName,
        description = description,
        selectedPlayers = selectedPlayers,
        selectedRounds = selectedRounds,
        selectedAnte = selectedAnte,
        onLobbyNameChange = { lobbyName = it },
        onDescriptionChange = { description = it },
        onPlayersSelected = {
            if (selectedPlayers != it) {
                selectedPlayers = it
                selectedRounds = null
            }
        },
        onRoundsSelected = { selectedRounds = it },
        onAnteSelected = { selectedAnte = it },
        onCreateLobby = { lobbyName, description, selectedPlayers, selectedRounds, selectedAnte ->
            onCreateLobby(
                LobbyCreation(
                    name = lobbyName,
                    description = description,
                    maxPlayers = selectedPlayers
                        ?: throw IllegalStateException("Selected players must not be null"),
                    nOfRounds = selectedRounds
                        ?: throw IllegalStateException("Selected rounds must not be null"),
                    ante = selectedAnte
                        ?: throw IllegalStateException("Selected ante must not be null")
                )
            )
        },
        isDataValid = validateData(
            lobbyName,
            description,
            selectedPlayers,
            selectedRounds,
            selectedAnte
        ),
        modifier = modifier,
        loading = loading,
        error = error,
        playerOptions = playerOptions,
        roundsOptions = roundsOptions,
        anteOptions = anteOptions,
    )
}

const val LOBBY_NAME_INPUT_TAG = "LobbyNameInput"
const val LOBBY_DESCRIPTION_INPUT_TAG = "LobbyDescriptionInput"
const val PLAYERS_NUMBER_SELECT_TAG = "PlayersNumberSelect"
const val ROUNDS_NUMBER_SELECT_TAG = "RoundsNumberSelect"
const val ANTE_SELECT_TAG = "AnteSelect"
const val LOBBIES_BUTTON_TAG = "LobbiesButton"
const val SELECT_NUMBER_PLAYERS_FIRST_TAG = "SelectNumberPlayersFirst"
const val SELECT_NUMBER_ROUNDS_FIRST_TAG = "SelectNumberRoundsFirst"
const val ERROR_TEXT_TAG = "ErrorText"



@Composable
fun LobbyCreationFormStateless(
    lobbyName: String,
    description: String,
    selectedPlayers: Int?,
    selectedRounds: Int?,
    selectedAnte: Int?,
    onLobbyNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPlayersSelected: (Int) -> Unit,
    onRoundsSelected: (Int) -> Unit,
    onAnteSelected: (Int) -> Unit,
    onCreateLobby: (lobbyName: String, description: String, maxPlayers: Int?, nOfRounds: Int?, ante: Int?) -> Unit,
    modifier: Modifier = Modifier,
    isDataValid: Boolean,
    loading: Boolean,
    error: String?,
    playerOptions: IntProgression,
    roundsOptions: IntProgression,
    anteOptions: IntProgression
) {
    val fieldModifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = lobbyName,
            onValueChange = onLobbyNameChange,
            label = { Text(stringResource(R.string.lobby_name)) },
            placeholder = { Text(stringResource(R.string.type_name)) },
            modifier = fieldModifier.testTag(LOBBY_NAME_INPUT_TAG)
        )
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text(stringResource(R.string.description)) },
            placeholder = { Text(stringResource(R.string.type_short_description)) },
            modifier = fieldModifier.testTag(LOBBY_DESCRIPTION_INPUT_TAG)
        )

        NumberSelect(
            modifier = fieldModifier.testTag(PLAYERS_NUMBER_SELECT_TAG),
            options = playerOptions,
            label = stringResource(id = R.string.select_number_players),
            onValueSelected = onPlayersSelected,
            selectedValue = selectedPlayers
        )
        if (selectedPlayers != null) {
            NumberSelect(
                modifier = fieldModifier.testTag(ROUNDS_NUMBER_SELECT_TAG),
                options = roundsOptions,
                label = stringResource(id = R.string.select_number_rounds),
                onValueSelected = onRoundsSelected,
                selectedValue = selectedRounds
            )
        } else {
            Text(
                text = stringResource(R.string.select_number_players_first),
                modifier = Modifier.testTag(SELECT_NUMBER_PLAYERS_FIRST_TAG)
            )
        }

        if (selectedPlayers != null && selectedRounds == null) {
            Text(
                text = stringResource(R.string.select_number_rounds_first),
                modifier = Modifier.testTag(SELECT_NUMBER_ROUNDS_FIRST_TAG)
            )
        }

        if (selectedPlayers != null && selectedRounds != null) {
            NumberSelect(
                modifier = fieldModifier.testTag(ANTE_SELECT_TAG),
                options = anteOptions,
                label = stringResource(id = R.string.select_ante),
                onValueSelected = onAnteSelected,
                selectedValue = selectedAnte
            )
        }

        if (!error.isNullOrBlank()) {
            Box(
                fieldModifier,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag(ERROR_TEXT_TAG)
                )
            }
        }

        Button(
            modifier = Modifier.testTag(LOBBIES_BUTTON_TAG),
            onClick = {
                onCreateLobby(
                    lobbyName,
                    description,
                    selectedPlayers,
                    selectedRounds,
                    selectedAnte
                )
            },
            enabled = isDataValid && !loading
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(text = stringResource(id = R.string.create_lobby))
        }
    }
}