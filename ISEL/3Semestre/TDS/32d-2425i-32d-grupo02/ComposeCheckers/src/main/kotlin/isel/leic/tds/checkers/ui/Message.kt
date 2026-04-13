package isel.leic.tds.checkers.ui

import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun Message(
    text: String,
    onOk: () -> Unit,
) = AlertDialog(
    onDismissRequest = onOk,
    confirmButton = {
        TextButton(onClick = onOk,) { Text("Ok") }
    },
    text = { Text(text) }
)