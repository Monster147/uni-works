package pt.isel.daw.http

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import pt.isel.daw.AuthenticatedUser
import pt.isel.daw.Match
import pt.isel.daw.MatchState
import pt.isel.daw.Round
import pt.isel.daw.Turn
import pt.isel.daw.http.model.MatchOutput
import pt.isel.daw.http.model.PlayTurnInputModel
import pt.isel.daw.http.model.Problem
import pt.isel.daw.http.model.RoundOutput
import pt.isel.daw.http.model.TurnOutput
import pt.isel.daw.http.model.UserMatchOutput
import pt.isel.daw.service.ActionKind
import pt.isel.daw.service.DataPublisher
import pt.isel.daw.service.Failure
import pt.isel.daw.service.MatchError
import pt.isel.daw.service.MatchServices
import pt.isel.daw.service.Success

@RestController
class MatchController(
    private val matchServices: MatchServices,
    private val publisher: DataPublisher,
) {
    @PostMapping("/api/lobbies/{id}/start")
    fun startMatch(
        user: AuthenticatedUser,
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val matchResult = matchServices.startMatch(id, user.user)
        return when (matchResult) {
            is Success -> {
                publisher.sendMessageToAll(
                    "Lobby-$id",
                    mapOf(
                        "message" to "A match has started in your lobby.",
                        "matchId" to matchResult.value.id,
                    ),
                    ActionKind.MatchStarted,
                )
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .header(
                        "Location",
                        "/api/matches/${matchResult.value.id}",
                    ).body(matchResult.value.id)
            }

            is Failure -> handleMatchError(matchResult.value)
        }
    }

    @PostMapping("/api/matches/{id}/rounds/start")
    fun startNextRound(
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val matchResult = matchServices.startNextRound(id)
        return when (matchResult) {
            is Success -> {
                val match = matchResult.value
                publisher.sendMessageToAll(
                    "Match-${match.id}",
                    mapOf("match" to getMatchSummary(match)),
                    ActionKind.NewRound,
                )
                ResponseEntity
                    .status(HttpStatus.OK)
                    .header(
                        "Location",
                        "/api/matches/${match.id}/rounds/${match.currentRound}",
                    ).build<Unit>()
            }

            is Failure -> handleMatchError(matchResult.value)
        }
    }

    @PostMapping("/api/matches/{id}/turns/play")
    fun playTurn(
        @PathVariable id: Int,
        user: AuthenticatedUser,
        @RequestBody keptDice: PlayTurnInputModel,
    ): ResponseEntity<*> {
        val matchResult = matchServices.playTurn(id, user.user, keptDice.keptDice)
        return when (matchResult) {
            is Success -> {
                val match = matchResult.value
                publisher.sendMessageToAll(
                    "Match-${match.id}",
                    mapOf("match" to getMatchSummary(match)),
                    ActionKind.PlayedTurn,
                )
                ResponseEntity
                    .status(HttpStatus.OK)
                    .header(
                        "Location",
                        "/api/matches/${match.id}/rounds/${match.currentRound}/turns/${user.user.id}",
                    ).build<Unit>()
            }

            is Failure -> handleMatchError(matchResult.value)
        }
    }

    @PostMapping("/api/matches/{id}/finish")
    fun finishRound(
        @PathVariable id: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val matchResult = matchServices.finishRound(id, user.user)
        return when (matchResult) {
            is Success -> {
                val match = matchResult.value
                if (match.state == MatchState.IN_PROGRESS) {
                    publisher.sendMessageToAll(
                        "Match-${match.id}",
                        mapOf("match" to getMatchSummary(match)),
                        ActionKind.RoundEnded,
                    )
                } else if (match.state == MatchState.COMPLETED) {
                    publisher.sendMessageToAll(
                        "Match-${match.id}",
                        mapOf("match" to getMatchSummary(match)),
                        ActionKind.MatchEnded,
                    )
                }
                ResponseEntity
                    .status(HttpStatus.OK)
                    .header(
                        "Location",
                        "/api/lobbies/",
                    ).build<Unit>()
            }

            is Failure -> handleMatchError(matchResult.value)
        }
    }

    @PostMapping("/api/matches/{id}/turns/pass")
    fun passTurn(
        @PathVariable id: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val matchResult = matchServices.passTurn(id, user.user)
        return when (matchResult) {
            is Success -> {
                val match = matchResult.value
                publisher.sendMessageToAll(
                    "Match-${match.id}",
                    mapOf("match" to getMatchSummary(match)),
                    ActionKind.PassedTurn,
                )
                ResponseEntity.status(HttpStatus.OK).build<Unit>()
            }

            is Failure -> handleMatchError(matchResult.value)
        }
    }

    @GetMapping("api/matches/{id}")
    fun getMatch(
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val match = matchServices.getMatchById(id)
        return match?.let { ResponseEntity.ok(getMatchSummary(it)) }
            ?: Problem.LobbyNotFound.response(HttpStatus.NOT_FOUND)
    }

    @GetMapping("api/matches/{id}/rounds/{roundNumber}")
    fun getRound(
        @PathVariable id: Int,
        @PathVariable roundNumber: Int,
    ): ResponseEntity<*> {
        val round = matchServices.getRoundById(id, roundNumber)
        return round?.let { ResponseEntity.ok(getRoundSummary(it)) }
            ?: Problem.LobbyNotFound.response(HttpStatus.NOT_FOUND)
    }

    @GetMapping("api/matches/{id}/events")
    fun subscribeToMatchEvents(
        @PathVariable id: Int,
    ): SseEmitter {
        val emitter = SseEmitter(Long.MAX_VALUE)
        publisher.addEmitter(
            "Match-$id",
            SSEUpdateDataEmitterAdapter(emitter),
        )
        return emitter
    }

    private fun getMatchSummary(match: Match): MatchOutput {
        val userObjs =
            match.players.map {
                UserMatchOutput(
                    id = it.id,
                    name = it.name,
                    email = it.email,
                    balance = it.balance,
                )
            }
        val matchWinnersObj =
            if (match.winners.isEmpty()) {
                emptyList()
            } else {
                match.winners.map {
                    UserMatchOutput(
                        id = it.id,
                        name = it.name,
                        email = it.email,
                        balance = it.balance,
                    )
                }
            }
        val roundObjs =
            match.rounds.map {
                getRoundSummary(it)
            }
        return MatchOutput(
            matchId = match.id,
            lobbyId = match.lobbyId,
            players = userObjs,
            rounds = roundObjs,
            state = match.state.toString(),
            winners = matchWinnersObj,
            currentRound = match.currentRound,
        )
    }

    private fun getRoundSummary(round: Round): RoundOutput {
        val roundWinnersObj =
            if (round.winners.isEmpty()) {
                emptyList()
            } else {
                round.winners.map { w ->
                    UserMatchOutput(
                        id = w.id,
                        name = w.name,
                        email = w.email,
                        balance = w.balance,
                    )
                }
            }
        val currentPlayer =
            UserMatchOutput(
                id = round.currentPlayer.id,
                name = round.currentPlayer.name,
                email = round.currentPlayer.email,
                balance = round.currentPlayer.balance,
            )
        return RoundOutput(
            roundNumber = round.roundNumber,
            turns =
                round.turns.map {
                    getTurnSummary(it)
                },
            currentPlayer = currentPlayer,
            winners = roundWinnersObj,
        )
    }

    private fun getTurnSummary(turn: Turn): TurnOutput {
        val player =
            UserMatchOutput(
                id = turn.player.id,
                name = turn.player.name,
                email = turn.player.email,
                balance = turn.player.balance,
            )
        return TurnOutput(
            player = player,
            hand = turn.hand.toString(),
            rollCount = turn.rollCount,
            state = turn.state.toString(),
            score = turn.score?.toString(),
        )
    }

    private fun handleMatchError(error: MatchError): ResponseEntity<Any> =
        when (error) {
            MatchError.MatchLobbyNotFound -> Problem.LobbyNotFound.response(HttpStatus.NOT_FOUND)
            MatchError.NotHostOfLobby -> Problem.NotHost.response(HttpStatus.FORBIDDEN)
            MatchError.PlayerAlreadyPlayedThisRound -> Problem.PlayerAlreadyPlayedThisRound.response(HttpStatus.BAD_REQUEST)
            MatchError.NotYourTurn -> Problem.NotYourTurn.response(HttpStatus.BAD_REQUEST)
            MatchError.InvalidKeptDice -> Problem.InvalidKeptDice.response(HttpStatus.BAD_REQUEST)
            MatchError.MatchNotInProgress -> Problem.MatchNotInProgress.response(HttpStatus.BAD_REQUEST)
            MatchError.NoRoundsDefined -> Problem.NoRoundsDefined.response(HttpStatus.BAD_REQUEST)
        }
}
