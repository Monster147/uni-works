package pt.isel.pdm.pokerDice.ui.screens.login_register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Airplay
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pt.isel.pdm.pokerDice.R
import pt.isel.pdm.pokerDice.domain.UserRegisterCredentials

@Composable
fun RegisterForm(
    loading: Boolean,
    error: String?,
    onRegister: (credentials: UserRegisterCredentials) -> Unit,
    validateCredentials: (name: String, email: String, password: String, code: String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    RegisterFormStateless(
        loading = loading,
        error = error,
        name = name,
        email = email,
        password = password,
        code = code,
        isDataValid = validateCredentials(name, email, password, code),
        onNameChange = { name = it },
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        onCodeChange = { code = it },
        onRegister = { name, email, password, code ->
            onRegister(UserRegisterCredentials(name, email, password, code))
        },
        modifier = modifier
    )
}

const val REGISTER_TITLE_TAG = "RegisterTitle"
const val REGISTER_BUTTON_TEXT_TAG = "RegisterButtonText"
const val PASSWORD_REQUIREMENTS_TAG = "PasswordRequirements"

@Composable
fun RegisterFormStateless(
    loading: Boolean,
    error: String?,
    name: String,
    email: String,
    password: String,
    code: String,
    isDataValid: Boolean,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onRegister: (name: String, email: String, password: String, code: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.register),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag(REGISTER_TITLE_TAG)
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(text = stringResource(id = R.string.name)) },
            placeholder = { Text(stringResource(R.string.type_name)) },
            modifier = Modifier.testTag(CREATE_NAME_INPUT_TAG),
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
        )


        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text(text = stringResource(id = R.string.email)) },
            placeholder = { Text(stringResource(R.string.type_email)) },
            modifier = Modifier.testTag(CREATE_EMAIL_INPUT_TAG),
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") }
        )

        var passwordVisible by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text(text = stringResource(id = R.string.password)) },
            placeholder = { Text(stringResource(R.string.type_password)) },
            modifier = Modifier.testTag(CREATE_PASSWORD_INPUT_TAG),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
        )

        OutlinedTextField(
            value = code,
            onValueChange = onCodeChange,
            label = { Text(text = "Invitation Code") },
            placeholder = { Text("Type a invitation code") },
            modifier = Modifier.testTag(INVITATION_CODE_INPUT_TAG),
            leadingIcon = { Icon(Icons.Default.Airplay, contentDescription = "Invitation Code") },
        )

        if (!error.isNullOrBlank()) {
            Box(
                modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        }

        Text(
            text = stringResource(id = R.string.password_requirements),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag(PASSWORD_REQUIREMENTS_TAG)
        )

        Button(
            onClick = {
                onRegister(name, email, password, code)
            },
            modifier = Modifier.testTag(REGISTER_BUTTON_TAG),
            enabled = isDataValid && !loading
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = stringResource(id = R.string.register),
                modifier = Modifier.testTag(REGISTER_BUTTON_TEXT_TAG)
            )
        }
    }
}