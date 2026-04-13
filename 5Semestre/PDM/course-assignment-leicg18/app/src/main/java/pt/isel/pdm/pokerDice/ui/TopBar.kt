package pt.isel.pdm.pokerDice.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

const val BACK_BUTTON_TAG = "back_button"
const val INFO_BUTTON_TAG = "info_button"
const val TITLE_TEXT_TAG = "title_text"
const val PROFILE_BUTTON_TAG = "profile_button"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String = "",
    onBackIntent: (() -> Unit)? = null,
    onInfoIntent: (() -> Unit)? = null,
    onProfileIntent: (() -> Unit)? = null,
) {
    TopAppBar(
        title = { Text(text = title, modifier = Modifier.testTag(tag = TITLE_TEXT_TAG)) },
        navigationIcon = {
            onBackIntent?.let {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .clickable(onClick = it)
                        .testTag(tag = BACK_BUTTON_TAG)
                        .padding(8.dp)
                )
            }
        },
        actions = {

            onProfileIntent?.let {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .clickable(onClick = it)
                        .testTag(tag = PROFILE_BUTTON_TAG)
                        .padding(8.dp)
                )
            }

            onInfoIntent?.let {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "About",
                    modifier = Modifier
                        .clickable(onClick = it)
                        .testTag(tag = INFO_BUTTON_TAG)
                        .padding(8.dp)
                )
            }


        }
    )
}

@Preview
@Composable
private fun TopBarWithBackNavigationPreview() {
    TopBar(title = "Some App Title", onBackIntent = { }, onInfoIntent = { }, onProfileIntent = { })
}

@Preview
@Composable
private fun TopBarWithoutBackNavigationPreview() {
    TopBar(title = "", onInfoIntent = { })
}

@Preview
@Composable
private fun TopBarWithoutInfoNavigationPreview() {
    TopBar(title = "", onBackIntent = { })
}

@Preview
@Composable
private fun TopBarWithProfilePreview() {
    TopBar(title = "", onProfileIntent = { })
}