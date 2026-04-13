package pt.isel.pdm.pokerDice.ui.screens.login_register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.isel.pdm.pokerDice.R
import pt.isel.pdm.pokerDice.domain.isValidCredentialsData
import pt.isel.pdm.pokerDice.ui.screens.loginViewModelPreview
import pt.isel.pdm.pokerDice.viewModels.login_register.LoginState
import pt.isel.pdm.pokerDice.viewModels.login_register.LoginViewModel

const val OR_DIVIDER_TAG = "OrDividerTag"

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(),
    onNavigateToTitle: () -> Unit,
    onNavigateToRegister: () -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        val observedState = viewModel.loginState


        LaunchedEffect(observedState) {
            when (observedState) {
                is LoginState.Success -> {
                    onNavigateToTitle()
                }

                else -> {}
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

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                LoginForm(
                    loading = observedState is LoginState.Loading,
                    error = if (observedState is LoginState.Error) observedState.message else "",
                    onLogin = { tentativeCredentials -> viewModel.login(tentativeCredentials) },
                    validateCredentials = ::isValidCredentialsData,
                    modifier = modifier
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = DividerDefaults.Thickness,
                        color = Color.Gray
                    )
                    Text(
                        text = " " + stringResource(R.string.or) + " ",
                        modifier = Modifier.padding(horizontal = 8.dp).testTag(OR_DIVIDER_TAG),
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = DividerDefaults.Thickness,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onNavigateToRegister() },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp)
                        .testTag(REGISTER_BUTTON_TAG)
                ) {
                    Text(
                        text = stringResource(id = R.string.no_account_create_one),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen(viewModel = loginViewModelPreview, onNavigateToTitle = {})
}
