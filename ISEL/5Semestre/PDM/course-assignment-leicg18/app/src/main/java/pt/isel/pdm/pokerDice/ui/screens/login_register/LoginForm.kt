package pt.isel.pdm.pokerDice.ui.screens.login_register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pt.isel.pdm.pokerDice.R
import pt.isel.pdm.pokerDice.domain.UserCredentials

@Composable
fun LoginForm(
    loading: Boolean,
    error: String?,
    onLogin: (credentials: UserCredentials) -> Unit,
    validateCredentials: (email: String, password: String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    LoginFormStateless(
        loading = loading,
        error = error,
        email = email,
        password = password,
        isDataValid = validateCredentials(email, password),
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        onLogin = { email, password -> onLogin(UserCredentials(email, password)) },
        modifier
    )
}

const val LOGIN_BUTTON_TAG = "LoginButton"
const val LOGIN_EMAIL_INPUT_TAG = "LoginEmailInput"
const val LOGIN_PASSWORD_INPUT_TAG = "LoginPasswordInput"

@Composable
fun LoginFormStateless(
    loading: Boolean,
    error: String?,
    email: String,
    password: String,
    isDataValid: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: (email: String, password: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text(stringResource(id = R.string.email)) },
            placeholder = { Text(stringResource(R.string.type_email)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(LOGIN_EMAIL_INPUT_TAG),
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Email") }
        )

        var passwordVisible by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text(stringResource(id = R.string.password)) },
            placeholder = { Text(stringResource(R.string.type_password)) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                if (isDataValid && !loading) onLogin(
                    email,
                    password
                )
            }),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(LOGIN_PASSWORD_INPUT_TAG)
        )

        if (!error.isNullOrBlank()) {
            Box(
                modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        }

        Button(
            onClick = { onLogin(email, password) },
            enabled = isDataValid && !loading,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(LOGIN_BUTTON_TAG)
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(text = stringResource(id = R.string.login))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginFormInvalidDataPreview() {
    LoginFormStateless(
        loading = false,
        error = null,
        email = "iusporting@isel.pt",
        password = "",
        isDataValid = false,
        onEmailChange = {},
        onPasswordChange = {},
        onLogin = { _, _ -> }
    )
}

@Preview(showBackground = true)
@Composable
fun LoginFormValidDataPreview() {
    LoginFormStateless(
        loading = false,
        error = null,
        email = "naogostodoporto@isel.pt",
        password = "the8-2",
        isDataValid = true,
        onEmailChange = { },
        onPasswordChange = { },
        onLogin = { _, _ -> }
    )
}

@Preview(showBackground = true)
@Composable
fun LoginFormSubmittingPreview() {
    LoginFormStateless(
        loading = true,
        error = null,
        email = "vivaoBenfica@isel.pt",
        password = "thePassword",
        isDataValid = true,
        onEmailChange = { },
        onPasswordChange = { },
        onLogin = { _, _ -> }
    )
}

@Preview(showBackground = true)
@Composable
fun LoginFormPreviewError() {
    LoginFormStateless(
        loading = false,
        error = "Failed to authenticate",
        email = "",
        password = "",
        isDataValid = false,
        onEmailChange = {},
        onPasswordChange = {},
        onLogin = { _, _ -> }
    )
}