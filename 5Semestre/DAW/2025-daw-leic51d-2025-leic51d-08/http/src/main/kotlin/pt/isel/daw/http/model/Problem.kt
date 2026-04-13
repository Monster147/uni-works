package pt.isel.daw.http.model

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.URI

private const val MEDIA_TYPE = "application/problem+json"
private const val PROBLEM_URI_PATH =
    "file:///docs/problems"

sealed class Problem(
    typeUri: URI,
) {
    @Suppress("unused")
    val type = typeUri.toString()
    val title = typeUri.toString().split("/").last()

    fun response(status: HttpStatus): ResponseEntity<Any> =
        ResponseEntity
            .status(status)
            .header("Content-Type", MEDIA_TYPE)
            .body(this)

    data object EmailAlreadyInUse : Problem(URI("$PROBLEM_URI_PATH/email-already-in-use"))

    data object InsecurePassword : Problem(URI("$PROBLEM_URI_PATH/insecure-password"))

    data object UserOrPasswordAreInvalid : Problem(URI("$PROBLEM_URI_PATH/user-or-password-are-invalid"))

    data object LobbyNameAlreadyInUse : Problem(URI("$PROBLEM_URI_PATH/lobby-name-already-in-use"))

    data object LobbyNotFound : Problem(URI("$PROBLEM_URI_PATH/lobby-not-found"))

    data object NotHost : Problem(URI("$PROBLEM_URI_PATH/not-host"))

    data object PlayerAlreadyPlayedThisRound : Problem(URI("$PROBLEM_URI_PATH/player-already-played-this-round"))

    data object InvalidRequestContent : Problem(URI("$PROBLEM_URI_PATH/invalid-request-content"))

    data object NotYourTurn : Problem(URI("$PROBLEM_URI_PATH/not-your-turn"))

    data object MatchNotInProgress : Problem(URI("$PROBLEM_URI_PATH/match-not-in-progress"))

    data object NoRoundsDefined : Problem(URI("$PROBLEM_URI_PATH/no-rounds-defined"))

    data object InvalidKeptDice : Problem(URI("$PROBLEM_URI_PATH/invalid-kept-dice"))

    data object InvalidInvitation : Problem(URI("$PROBLEM_URI_PATH/invalid-invitation"))

    data object InvitationAlreadyUsed : Problem(URI("$PROBLEM_URI_PATH/invitation-already-used"))

    data object StatsNotFound : Problem(URI("$PROBLEM_URI_PATH/stats-not-found"))

    data object UnknownError : Problem(URI("$PROBLEM_URI_PATH/unknown-error"))
}
