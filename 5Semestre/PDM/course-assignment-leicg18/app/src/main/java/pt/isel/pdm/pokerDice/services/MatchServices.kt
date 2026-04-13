package pt.isel.pdm.pokerDice.services

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import pt.isel.pdm.pokerDice.domain.Hand
import pt.isel.pdm.pokerDice.domain.HandCategory
import pt.isel.pdm.pokerDice.domain.HandEvaluation
import pt.isel.pdm.pokerDice.domain.LobbyState
import pt.isel.pdm.pokerDice.domain.Match
import pt.isel.pdm.pokerDice.domain.MatchState
import pt.isel.pdm.pokerDice.domain.Round
import pt.isel.pdm.pokerDice.domain.Turn
import pt.isel.pdm.pokerDice.domain.TurnState
import pt.isel.pdm.pokerDice.domain.User
import pt.isel.pdm.pokerDice.domain.generateHand
import pt.isel.pdm.pokerDice.domain.rerollHand
import pt.isel.pdm.pokerDice.repo.RepositoryUser
import pt.isel.pdm.pokerDice.repo.TransactionManager
import pt.isel.pdm.pokerDice.services.dto.MatchEvent

sealed class MatchError {
    data object MatchLobbyNotFound : MatchError()

    data object NotHostOfLobby : MatchError()

    data object PlayerAlreadyPlayedThisRound : MatchError()

    data object NotYourTurn : MatchError()

    data object InvalidKeptDice : MatchError()

    data object MatchNotInProgress : MatchError()

    data object NoRoundsDefined : MatchError()

    data object UnknownError : MatchError()
}

class MatchServices(
    private val trxManager: TransactionManager,
) : MatchServiceInterface {

    override suspend fun startMatch(
        lobbyId: Int,
        host: User,
    ): Either<MatchError, Match> {
        return trxManager.run {
            repoLobbies.getById(lobbyId)?.let {
                if (it.host.id != host.id) {
                    return@run failure(MatchError.NotHostOfLobby)
                }
                val match = repoMatches.createMatch(it)
                repoLobbies.save(it.copy(state = LobbyState.MATCH_IN_PROGRESS))
                return@run success(match)
            }
            failure(MatchError.MatchLobbyNotFound)
        }
    }

    override suspend fun startNextRound(matchId: Int): Either<MatchError, Match> {
        return trxManager.run {
            repoMatches.getById(matchId)?.let {
                val lobby =
                    repoLobbies.getById(it.lobbyId)
                        ?: return@run failure(MatchError.MatchLobbyNotFound)
                if (it.state != MatchState.IN_PROGRESS) return@run failure(MatchError.MatchNotInProgress)
                if (it.rounds.isEmpty()) return@run failure(MatchError.NoRoundsDefined)

                val eligiblePlayers = it.players.filter { it.balance >= lobby.ante }.toMutableList()

                if (eligiblePlayers.size < 2) { // Not enough players to continue the match
                    val updatedMatch =
                        it.copy(state = MatchState.COMPLETED, players = eligiblePlayers)
                    repoMatches.save(updatedMatch)
                    return@run success(updatedMatch)
                }
                val updatedMatch = prepareNextRound(it, eligiblePlayers)
                repoMatches.save(updatedMatch)
                return@run success(updatedMatch)
            }
            failure(MatchError.MatchLobbyNotFound)
        }
    }

    override suspend fun playTurn(
        matchId: Int,
        player: User,
        keptDice: List<Boolean>?,
    ): Either<MatchError, Match> {
        return trxManager.run {
            repoMatches.getById(matchId)?.let {
                if (it.state != MatchState.IN_PROGRESS) return@run failure(MatchError.MatchNotInProgress)
                if (it.currentRound <= 0) return@run failure(MatchError.NoRoundsDefined)
                val currentRound =
                    it.getCurrentRoundOrNull() ?: return@run failure(MatchError.MatchLobbyNotFound)

                val expectedPlayer = determineExpectedPlayer(it, currentRound) ?: player

                if (expectedPlayer.id != player.id) return@run failure(MatchError.NotYourTurn)

                val existingTurn = currentRound.turns.find { t -> t.player.id == player.id }

                if (existingTurn == null) {
                    return@run failure(MatchError.NotYourTurn)
                }

                val newTurn =
                    when {
                        keptDice != null -> {
                            if (keptDice.size != 5) return@run failure(MatchError.InvalidKeptDice)
                            rerollTurn(existingTurn, keptDice)
                        }

                        existingTurn.rollCount == 0 -> {
                            newTurn(player, existingTurn.id, currentRound.id)
                        }

                        else ->
                            existingTurn.copy(
                                state = TurnState.COMPLETED,
                                score = existingTurn.score
                                    ?: evaluateHand(existingTurn.hand).category,
                            )
                    }

                val updatedMatch = updateTurnInMatch(it, currentRound, newTurn)
                repoMatches.save(updatedMatch)
                return@run success(updatedMatch)
            }
            failure(MatchError.MatchLobbyNotFound)
        }
    }

    override suspend fun passTurn(
        matchId: Int,
        player: User,
    ): Either<MatchError, Match> {
        return trxManager.run {
            repoMatches.getById(matchId)?.let {
                if (it.state != MatchState.IN_PROGRESS) return@run failure(MatchError.MatchNotInProgress)
                if (it.currentRound <= 0) return@run failure(MatchError.NoRoundsDefined)
                val currentRound =
                    it.getCurrentRoundOrNull() ?: return@run failure(MatchError.MatchLobbyNotFound)

                val turn =
                    currentRound.turns.find { t -> t.player.id == player.id }
                        ?: return@run failure(MatchError.PlayerAlreadyPlayedThisRound)
                if (turn.state == TurnState.COMPLETED) return@run failure(MatchError.NotYourTurn)

                val completedTurn =
                    turn.copy(
                        state = TurnState.COMPLETED,
                        score = turn.score ?: evaluateHand(turn.hand).category,
                    )

                val updatedMatch = updateTurnInMatch(it, currentRound, completedTurn)
                repoMatches.save(updatedMatch)
                return@run success(updatedMatch)
            }
            failure(MatchError.MatchLobbyNotFound)
        }
    }

    override suspend fun finishRound(
        matchId: Int,
        host: User,
    ): Either<MatchError, Match> {
        return trxManager.run {
            repoMatches.getById(matchId)?.let {
                val lobby =
                    repoLobbies.getById(it.lobbyId)
                        ?: return@run failure(MatchError.MatchLobbyNotFound)

                if (lobby.host.id != host.id) {
                    return@run failure(MatchError.NotHostOfLobby)
                }
                if (it.currentRound <= 0) return@run failure(MatchError.NoRoundsDefined)
                val currentRound =
                    it.getCurrentRoundOrNull() ?: return@run failure(MatchError.MatchLobbyNotFound)

                if (!allTurnsCompleted(currentRound, it.players.size)) {
                    return@run failure(MatchError.PlayerAlreadyPlayedThisRound)
                }

                val evaluations = currentRound.turns.map { it.player to evaluateHand(it.hand) }

                val winners = determineWinner(evaluations)

                val currentRoundIdx = it.currentRound - 1
                val updatedRounds = it.rounds.toMutableList().also { roundsList ->
                    val updatedRound = currentRound.copy(winners = winners)
                    roundsList[currentRoundIdx] = updatedRound
                }

                var pot = 0
                val eligPlayers = it.players.filter { it.balance >= lobby.ante }
                eligPlayers.forEach { p ->
                    p.balance -= lobby.ante
                    repoUsers.save(p)
                    pot += lobby.ante
                }

                val updatedWinners = winners.map { w -> eligPlayers.find { it.id == w.id } ?: w }

                distributePot(repoUsers, pot, updatedWinners)

                val isLastRound = it.currentRound >= it.rounds.size
                val updatedMatch =
                    it.copy(
                        rounds = updatedRounds,
                        state = if (isLastRound) MatchState.COMPLETED else it.state,
                    )

                repoMatches.save(updatedMatch)

                if (isLastRound) {
                    val updatedLobby = lobby.copy(state = LobbyState.FINISHED)
                    repoLobbies.save(updatedLobby)
                    val newlyUpdatedMatch = updatedMatch.copy(
                        winners = determineMatchWinners(updatedMatch)
                    )
                    repoMatches.save(newlyUpdatedMatch)
                    return@run success(newlyUpdatedMatch)
                }
                return@run success(updatedMatch)
            }
            failure(MatchError.MatchLobbyNotFound)
        }
    }

    override suspend fun getMatchById(matchId: Int): Match? =
        trxManager.run {
            repoMatches.getById(matchId)
        }

    override suspend fun getRoundById(
        matchId: Int,
        roundNumber: Int,
    ): Round? =
        trxManager.run {
            repoMatches.getById(matchId)?.rounds?.getOrNull(roundNumber - 1)
        }

    private val matchEvents = MutableSharedFlow<MatchEvent>()

    override fun subscribeToMatchEvents(matchId: Int): Flow<MatchEvent> = matchEvents

    private fun prepareNextRound(
        match: Match,
        players: List<User>,
    ): Match {
        val nextRoundNumber = match.currentRound + 1
        val updatedRounds = match.rounds.toMutableList()

        if (updatedRounds.size < nextRoundNumber) {
            updatedRounds.add(
                Round(
                    id = updatedRounds.size + 1,
                    matchId = match.id,
                    roundNumber = nextRoundNumber,
                    turns = mutableListOf(),
                    currentPlayer = determineExpectedPlayer(match, updatedRounds.last())
                        ?: players[0],
                ),
            )
        } else {
            updatedRounds[nextRoundNumber - 1] = updatedRounds[nextRoundNumber - 1]
        }

        return match.copy(
            players = players,
            rounds = updatedRounds,
            currentRound = nextRoundNumber,
        )
    }

    private fun determineExpectedPlayer(
        match: Match,
        round: Round,
    ): User? {
        return match.players.firstOrNull { player ->
            round.turns.find { it.player.id == player.id }?.state != TurnState.COMPLETED
        }
    }

    private fun updateTurnInMatch(
        match: Match,
        round: Round,
        updatedTurn: Turn,
    ): Match {
        val updatedTurns =
            round.turns.filter { it.player.id != updatedTurn.player.id } + updatedTurn
        val updatedRound = round.copy(
            turns = updatedTurns.toMutableList(),
            currentPlayer = determineExpectedPlayer(match, round) ?: updatedTurn.player,
        )
        val currentRoundIdx = match.currentRound - 1
        val updatedRounds = match.rounds.toMutableList().also { it[currentRoundIdx] = updatedRound }
        return match.copy(rounds = updatedRounds)
    }

    private fun evaluateHand(hand: Hand): HandEvaluation {
        val diceValues = hand.dice.map { it.face.ordinal }
        val counts = diceValues.groupingBy { it }.eachCount()

        val sortedCounts =
            counts.entries
                .sortedWith(
                    compareByDescending<Map.Entry<Int, Int>> { it.value }
                        .thenByDescending { it.key },
                )

        val countValues = sortedCounts.map { it.key }
        val countFreq = sortedCounts.map { it.value }

        return when {
            countFreq == listOf(5) -> HandEvaluation(
                HandCategory.FIVE_OF_A_KIND,
                countValues
            ) // Five of a Kind
            countFreq == listOf(4, 1) -> HandEvaluation(
                HandCategory.FOUR_OF_A_KIND,
                countValues
            ) // Four of a Kind
            countFreq == listOf(3, 2) -> HandEvaluation(
                HandCategory.FULL_HOUSE,
                countValues
            ) // Full House
            isStraight(diceValues) -> HandEvaluation(
                HandCategory.STRAIGHT,
                listOf(countValues.maxOrNull() ?: 0)
            ) // Straight
            countFreq == listOf(3, 1, 1) -> HandEvaluation(
                HandCategory.THREE_OF_A_KIND,
                countValues
            ) // Three of a Kind
            countFreq == listOf(2, 2, 1) -> HandEvaluation(
                HandCategory.TWO_PAIR,
                countValues
            ) // Two Pair
            countFreq == listOf(2, 1, 1, 1) -> HandEvaluation(
                HandCategory.ONE_PAIR,
                countValues
            ) // One Pair
            else -> HandEvaluation(
                HandCategory.HIGH_CARD,
                diceValues.sortedDescending()
            ) // High Card
        }
    }

    private fun isStraight(values: List<Int>): Boolean {
        val sorted = values.sorted()
        return sorted.distinct().size == 5 &&
                (sorted.last() - sorted.first() == 4)
    }

    private fun newTurn(
        player: User,
        turnId: Int,
        roundId: Int,
    ): Turn {
        val hand = Hand().generateHand()
        val score = evaluateHand(hand)
        return Turn(
            id = turnId,
            roundId = roundId,
            player = player,
            hand = hand,
            score = score.category,
            rollCount = 1,
        )
    }

    private fun rerollTurn(
        turn: Turn,
        keptDice: List<Boolean>,
    ): Turn {
        val newHand = Hand().rerollHand(turn.hand, keptDice)
        val newRollCount = turn.rollCount + 1
        val score = evaluateHand(newHand)
        return turn.copy(
            hand = newHand,
            rollCount = newRollCount,
            state = if (newRollCount >= 3) TurnState.COMPLETED else TurnState.IN_PROGRESS,
            score = score.category,
        )
    }

    private fun allTurnsCompleted(
        round: Round,
        expectedCount: Int,
    ): Boolean =
        round.turns.all { it.state == TurnState.COMPLETED } && round.turns.size == expectedCount

    private fun determineWinner(evaluations: List<Pair<User, HandEvaluation>>): List<User> {
        val bestEvalEntry = evaluations.maxByOrNull { it.second }?.second ?: return emptyList()
        return evaluations
            .filter { it.second.compareTo(bestEvalEntry) == 0 }
            .map { it.first }
            .distinctBy { it.id }
    }

    private fun distributePot(
        repoUsers: RepositoryUser,
        pot: Int,
        winners: List<User>,
    ) {
        if (winners.isNotEmpty()) {
            val share = pot / winners.size
            var remainder = pot % winners.size
            winners.forEach { w ->
                w.balance += share + if (remainder > 0) 1 else 0
                if (remainder > 0) remainder -= 1
                repoUsers.save(w)
            }
        }
    }

    private fun determineMatchWinners(match: Match): List<User> {
        val winsByPlayerId: Map<Int, Int> =
            match.rounds
                .flatMap { it.winners }
                .groupingBy { it.id }
                .eachCount()
        if (winsByPlayerId.isEmpty()) return emptyList()
        val maxWins = winsByPlayerId.values.maxOrNull() ?: 0
        val winners = match.players.filter { p -> winsByPlayerId[p.id] == maxWins }
        return winners
    }

    private fun Match.getCurrentRoundOrNull(): Round? = rounds.getOrNull(currentRound - 1)
}
