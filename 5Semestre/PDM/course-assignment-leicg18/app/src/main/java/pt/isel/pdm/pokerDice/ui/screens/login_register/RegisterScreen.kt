package pt.isel.pdm.pokerDice.ui.screens.login_register

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.isel.pdm.pokerDice.domain.isValidRegisterCredentialsData
import pt.isel.pdm.pokerDice.ui.TopBar
import pt.isel.pdm.pokerDice.ui.screens.loginViewModelPreview
import pt.isel.pdm.pokerDice.ui.screens.registerViewModelPreview
import pt.isel.pdm.pokerDice.viewModels.login_register.LoginState
import pt.isel.pdm.pokerDice.viewModels.login_register.LoginViewModel
import pt.isel.pdm.pokerDice.viewModels.login_register.RegisterState
import pt.isel.pdm.pokerDice.viewModels.login_register.RegisterViewModel

const val CREATE_NAME_INPUT_TAG = "CreateNameInput"
const val CREATE_EMAIL_INPUT_TAG = "CreateEmailInput"
const val CREATE_PASSWORD_INPUT_TAG = "CreatePasswordInput"

const val REGISTER_BUTTON_TAG = "RegisterButton"

const val INVITATION_CODE_INPUT_TAG = "InvitationCodeInput"

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    registerViewModel: RegisterViewModel = viewModel(),
    loginViewModel: LoginViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToTitle: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopBar(
                onBackIntent = onNavigateBack
            )
        }
    ) { padding ->
        val registerState = registerViewModel.registerState
        val loginState = loginViewModel.loginState

        LaunchedEffect(registerState) {
            if (registerState is RegisterState.Success) {
                loginViewModel.login(registerState.credentials)
            }
        }

        LaunchedEffect(loginState) {
            if (loginState is LoginState.Success) {
                onNavigateToTitle()
            }
        }

        RegisterForm(
            modifier = modifier
                .padding(padding)
                .fillMaxSize(),
            loading = registerState is RegisterState.Loading,
            error = if (registerState is RegisterState.Error) registerState.message else null,
            onRegister = { credentials ->
                registerViewModel.addUser(credentials)
            },
            validateCredentials = ::isValidRegisterCredentialsData
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(
        registerViewModel = registerViewModelPreview,
        loginViewModel = loginViewModelPreview
    )
}