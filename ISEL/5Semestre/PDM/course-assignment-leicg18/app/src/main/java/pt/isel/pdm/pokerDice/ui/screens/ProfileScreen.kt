package pt.isel.pdm.pokerDice.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pt.isel.pdm.pokerDice.R
import pt.isel.pdm.pokerDice.domain.UserStats
import pt.isel.pdm.pokerDice.ui.TopBar
import pt.isel.pdm.pokerDice.viewModels.ProfileState
import pt.isel.pdm.pokerDice.viewModels.UserViewModel

const val PROFILE_NAME_TAG = "ProfileName"
const val PROFILE_EMAIL_TAG = "ProfileEmail"
const val PROFILE_BALANCE_TAG = "ProfileBalance"
const val GET_CODE_BUTTON_TAG = "GetCodeButton"
const val LOGOUT_BUTTON_TAG = "LogoutButton"
const val STATS_TAG = "Stats"
const val ROUNDS_WON_TAG = "RoundsWon"
const val ROUNDS_LOST_TAG = "RoundsLost"
const val ROUNDS_DRAW_TAG = "RoundsDraw"
const val TOTAL_MATCHES_TAG = "TotalMatches"
const val MATCHES_WON_TAG = "MatchesWon"
const val MATCHES_LOST_TAG = "MatchesLost"
const val MATCHES_DRAW_TAG = "MatchesDraw"
const val WIN_RATE_TAG = "WinRate"
const val WIN_RATE_CIRCLE_TAG = "WinRateCircle"
const val CODE_TEXT_TAG = "CodeText"
const val VALID_CODE_TAG = "ValidCode"
const val VALIDATE_CODE_BUTTON_TAG = "ValidateCodeButton"


@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onNavigateToTitle: () -> Unit = {},
    onLogout: () -> Unit = {},
    userViewModel: UserViewModel
) {
    val observedUserState = userViewModel.state

    val displayedUser = userViewModel.user
    val displayedUserStats = userViewModel.userStats


    val snackbarHostState = remember { SnackbarHostState() }
    var copyRequest by remember { mutableStateOf(false) }
    val copiedString = stringResource(R.string.copied) + "!"

    LaunchedEffect(copyRequest) {
        if(copyRequest) {
            snackbarHostState.showSnackbar(copiedString)
            copyRequest = false
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                onBackIntent = onNavigateToTitle
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        when (observedUserState) {
            is ProfileState.Loading, ProfileState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(R.string.loading) + "...")
                }
            }

            is ProfileState.Unauthenticated -> {
                onLogout()
            }

            else -> {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        if (displayedUser != null) {
                            Text(
                                text = stringResource(R.string.username) + ": ${displayedUser.name}",
                                modifier = Modifier.testTag(PROFILE_NAME_TAG)
                            )

                            Text(
                                text = "Email: ${displayedUser.email}",
                                modifier = Modifier.testTag(PROFILE_EMAIL_TAG)
                            )

                            Text(
                                text = stringResource(R.string.balance) + ": ${displayedUser.balance}",
                                modifier = Modifier.testTag(PROFILE_BALANCE_TAG)
                            )
                        }
                    }

                    if (displayedUserStats != null) {
                        item {
                            RenderStats(Modifier, displayedUserStats)
                        }
                    }

                    if (userViewModel.code.isNotEmpty()) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                val clipboard = LocalClipboardManager.current

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(stringResource(R.string.curr_code) + ":")
                                    Text(
                                        text = userViewModel.code,
                                        modifier = Modifier.testTag(CODE_TEXT_TAG)
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy code",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            copyRequest = true
                                            clipboard.setText(AnnotatedString(userViewModel.code))
                                        }
                                )
                            }
                        }

                        item {
                            val validCode = if (userViewModel.validCode)
                                stringResource(R.string.valid)
                            else
                                stringResource(R.string.invalid)

                            Text(
                                text = stringResource(R.string.code_status) + ": $validCode",
                                modifier = Modifier.testTag(VALID_CODE_TAG)
                            )
                        }
                    }

                    item {
                        Button(
                            onClick = { userViewModel.getCode() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag(GET_CODE_BUTTON_TAG)
                        ) {
                            Text(
                                if (userViewModel.code.isEmpty())
                                    stringResource(R.string.get_code)
                                else
                                    stringResource(R.string.refresh_code)
                            )
                        }
                    }

                    if (userViewModel.code.isNotEmpty()) {
                        item {
                            Button(
                                onClick = { userViewModel.validateCode() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag(VALIDATE_CODE_BUTTON_TAG)
                            ) {
                                Text(stringResource(R.string.validate_code))
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                userViewModel.logout()
                                onLogout()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag(LOGOUT_BUTTON_TAG)
                        ) {
                            Text(stringResource(R.string.logout))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RenderStats(modifier:Modifier, userStats: UserStats) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.statistics),
            modifier = Modifier
                .padding(bottom = 4.dp)
                .testTag(STATS_TAG)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.rounds_won) + ": ${userStats.roundsWon}",
                    modifier = Modifier.testTag(ROUNDS_WON_TAG)
                )
                Text(
                    text = stringResource(R.string.rounds_lost) + ": ${userStats.roundsLost}",
                    modifier = Modifier.testTag(ROUNDS_LOST_TAG)
                )
                Text(
                    text = stringResource(R.string.rounds_drawn) + ": ${userStats.roundsDrawn}",
                    modifier = Modifier.testTag(ROUNDS_DRAW_TAG)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.total_matches) + ": ${userStats.totalMatches}",
                    modifier = Modifier.testTag(TOTAL_MATCHES_TAG)
                )
                Text(
                    text = stringResource(R.string.matches_won) + ": ${userStats.matchesWon}",
                    modifier = Modifier.testTag(MATCHES_WON_TAG)
                )
                Text(
                    text = stringResource(R.string.matches_lost) + ": ${userStats.matchesLost}",
                    modifier = Modifier.testTag(MATCHES_LOST_TAG)
                )
                Text(
                    text = stringResource(R.string.matches_drawn) +  ": ${userStats.matchesDrawn}",
                    modifier = Modifier.testTag(MATCHES_DRAW_TAG)
                )
            }

        }
        Text(
            text = stringResource(R.string.win_rate),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .testTag(WIN_RATE_TAG)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag(WIN_RATE_CIRCLE_TAG),
            contentAlignment = Alignment.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    progress = { userStats.winRate.coerceIn(0.0, 1.0).toFloat() },
                    modifier = Modifier.size(100.dp),
                    strokeWidth = 8.dp,
                )
                Text(
                    text = "${(userStats.winRate * 100).toInt()}%"
                )
            }
        }
    }
}


@Preview
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(userViewModel = userViewModelPreview)
}