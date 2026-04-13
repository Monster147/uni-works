package pt.isel.pdm.pokerDice.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pt.isel.pdm.pokerDice.R
import pt.isel.pdm.pokerDice.viewModels.LoadingLoginState
import pt.isel.pdm.pokerDice.viewModels.LoadingSplashViewModel

@Composable
fun LoadingSplash(
    modifier: Modifier = Modifier,
    loadingSplashViewModel: LoadingSplashViewModel,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToTitle: () -> Unit = {}
) {
    var progress by remember { mutableStateOf(0f) }
    var loginFinished by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 2000,
            easing = LinearEasing
        ),
        label = "SplashProgress"
    )

    val state = loadingSplashViewModel.loadingLoginState

    LaunchedEffect(Unit) {
        progress = 1f
        loadingSplashViewModel.loginStoredCredentials()
    }

    LaunchedEffect(state) {
        when (state) {
            is LoadingLoginState.Success,
            is LoadingLoginState.Error,
            is LoadingLoginState.NoLogin -> {
                loginFinished = true
            }
        }
    }

    LaunchedEffect(animatedProgress, loginFinished) {
        if (animatedProgress >= 1f && loginFinished) {
            when (state) {
                is LoadingLoginState.Success -> onNavigateToTitle()
                is LoadingLoginState.Error,
                is LoadingLoginState.NoLogin -> onNavigateToLogin()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val text = when(state) {
            is LoadingLoginState.Idle -> stringResource(R.string.loading) + "..."
            is LoadingLoginState.Loading -> stringResource(R.string.logging_in) + "..."
            is LoadingLoginState.Success -> stringResource(R.string.logged_in)
            is LoadingLoginState.Error -> stringResource(R.string.login_error)
            is LoadingLoginState.NoLogin -> stringResource(R.string.no_login)
            else -> ""
        }
        //CircularProgressIndicator()
        Text(
            text = text,
            modifier =
                modifier
                .padding(horizontal = 32.dp, vertical = 6.dp),
            textAlign = TextAlign.Center
        )

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = modifier
                .padding(top = 16.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(6.dp))
            ,
            color = ProgressIndicatorDefaults.linearColor,
            trackColor = ProgressIndicatorDefaults.linearTrackColor,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )
    }
}