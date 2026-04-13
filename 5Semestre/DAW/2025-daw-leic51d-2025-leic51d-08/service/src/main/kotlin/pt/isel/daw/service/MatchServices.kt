package pt.isel.daw.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import pt.isel.daw.Hand
import pt.isel.daw.HandCategory
import pt.isel.daw.HandEvaluation
import pt.isel.daw.LobbyState
import pt.isel.daw.Match
import pt.isel.daw.MatchState
import pt.isel.daw.Round
import pt.isel.daw.Turn
import pt.isel.daw.TurnState
import pt.isel.daw.User
import pt.isel.daw.UserStats
import pt.isel.daw.generateHand
import pt.isel.daw.repo.RepositoryUser
import pt.isel.daw.repo.TransactionManager
import pt.isel.daw.rerollHand

sealed class MatchError {
    data object MatchLobbyNotFound : MatchError()

    data object NotHostOfLobby : MatchError()

    data object PlayerAlreadyPlayedThisRound : MatchError()

    data object NotYourTurn : MatchError()

    data object InvalidKeptDice : MatchError()

    data object MatchNotInProgress : MatchError()

    data object NoRoundsDefined : MatchError()
}

@Component
class MatchServices(
    private val trxManager: TransactionManager,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(MatchServices::class.java)
    }

    fun startMatch(
        lobbyId: Int,
        host: User,
    ): Either<MatchError, Match> {
        return trxManager.run {
            repoLobbies.getById(lobbyId)?.let {
                if (it.host.id != host.id) {
                    return@run failure(MatchError.NotHostOfLobby)
                }
                val match = repoMatches.createMatch(it)
                val lobby = it.copy(state = LobbyState.MATCH_IN_PROGRESS, matchId = match.id)
                repoLobbies.save(lobby)
                it.players.forEach {
                    val stats = repoUsers.getUserStatsById(it.id)
                    val newStats =
                        stats?.copy(total_matches = stats.total_matches + 1) ?: UserStats(
                            id = it.id,
                            total_matches = 1,
                        )
                    repoUsers.saveUserStats(newStats)
                }
                return@run success(match)
            }
            failure(MatchError.MatchLobbyNotFound)
        }
    }

    fun startNextRound(matchId: Int): Either<MatchError, Match> {
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

    fun playTurn(
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
                            println("Kept Dice: $keptDice")
                            println("keptDice size: ${keptDice.size}")
                            if (keptDice.size != 5) return@run failure(MatchError.InvalidKeptDice)
                            rerollTurn(existingTurn, keptDice)
                        }

                        existingTurn.rollCount == 0 -> {
                            newTurn(player, existingTurn.id, currentRound.id)
                        }

                        else ->
                            existingTurn.copy(
                                state = TurnState.COMPLETED,
                                score =
                                    existingTurn.score
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

    fun passTurn(
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

    fun finishRound(
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
                val updatedRounds =
                    it.rounds.toMutableList().also { roundsList ->
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

                it.players.filter { it !in updatedWinners }.forEach { p ->
                    val stats = repoUsers.getUserStatsById(p.id)
                    val newStats =
                        stats?.copy(rounds_lost = stats.rounds_lost + 1) ?: UserStats(
                            id = p.id,
                            total_matches = 1,
                            rounds_lost = 1,
                        )
                    repoUsers.saveUserStats(newStats)
                }

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
                    val matchWinners = determineMatchWinners(updatedMatch, repoUsers)
                    val newlyUpdatedMatch =
                        updatedMatch.copy(
                            winners = matchWinners,
                        )
                    repoMatches.save(newlyUpdatedMatch)

                    return@run success(newlyUpdatedMatch)
                }
                return@run success(updatedMatch)
            }
            failure(MatchError.MatchLobbyNotFound)
        }
    }

    fun getMatchById(matchId: Int): Match? =
        trxManager.run {
            repoMatches.getById(matchId)
        }

    fun getRoundById(
        matchId: Int,
        roundNumber: Int,
    ): Round? =
        trxManager.run {
            repoMatches.getById(matchId)?.rounds?.getOrNull(roundNumber - 1)
        }

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
                    currentPlayer =
                        determineExpectedPlayer(match, updatedRounds.last())
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
        val updatedRound =
            round.copy(
                turns = updatedTurns.toMutableList(),
            )
        val newCurrentPlayerRound =
            updatedRound.copy(
                currentPlayer = determineExpectedPlayer(match, updatedRound) ?: updatedTurn.player,
            )
        val currentRoundIdx = match.currentRound - 1
        val updatedRounds = match.rounds.toMutableList().also { it[currentRoundIdx] = newCurrentPlayerRound }
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
            countFreq == listOf(5) ->
                HandEvaluation(
                    HandCategory.FIVE_OF_A_KIND,
                    countValues,
                ) // Five of a Kind
            countFreq == listOf(4, 1) ->
                HandEvaluation(
                    HandCategory.FOUR_OF_A_KIND,
                    countValues,
                ) // Four of a Kind
            countFreq == listOf(3, 2) ->
                HandEvaluation(
                    HandCategory.FULL_HOUSE,
                    countValues,
                ) // Full House
            isStraight(diceValues) ->
                HandEvaluation(
                    HandCategory.STRAIGHT,
                    listOf(countValues.maxOrNull() ?: 0),
                ) // Straight
            countFreq == listOf(3, 1, 1) ->
                HandEvaluation(
                    HandCategory.THREE_OF_A_KIND,
                    countValues,
                ) // Three of a Kind
            countFreq == listOf(2, 2, 1) ->
                HandEvaluation(
                    HandCategory.TWO_PAIR,
                    countValues,
                ) // Two Pair
            countFreq == listOf(2, 1, 1, 1) ->
                HandEvaluation(
                    HandCategory.ONE_PAIR,
                    countValues,
                ) // One Pair
            else ->
                HandEvaluation(
                    HandCategory.HIGH_CARD,
                    diceValues.sortedDescending(),
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
    ): Boolean = round.turns.all { it.state == TurnState.COMPLETED } && round.turns.size == expectedCount

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
                if (winners.size == 1) {
                    val stats = repoUsers.getUserStatsById(w.id)
                    val newStats =
                        stats?.copy(rounds_won = stats.rounds_won + 1) ?: UserStats(
                            id = w.id,
                            total_matches = 1,
                            rounds_won = 1,
                        )
                    repoUsers.saveUserStats(newStats)
                } else {
                    val stats = repoUsers.getUserStatsById(w.id)
                    val newStats =
                        stats?.copy(rounds_drawn = stats.rounds_drawn + 1) ?: UserStats(
                            id = w.id,
                            total_matches = 1,
                            rounds_drawn = 1,
                        )
                    repoUsers.saveUserStats(newStats)
                }
            }
        }
    }

    private fun updatePlayerStatsAfterRoundCompletion(
        matchPlayers: List<User>,
        roundWinners: List<User>,
        repoUsers: RepositoryUser,
    ) {
        if (roundWinners.size == 1) {
            val winner = roundWinners[0]
            val stats = repoUsers.getUserStatsById(winner.id)
            val newStats =
                stats?.copy(rounds_won = stats.rounds_won + 1) ?: UserStats(
                    id = winner.id,
                    total_matches = 1,
                    rounds_won = 1,
                )
            repoUsers.saveUserStats(newStats)
        } else {
            matchPlayers.filter { it in roundWinners }.forEach { p ->
                val stats = repoUsers.getUserStatsById(p.id)
                val newStats =
                    stats?.copy(rounds_drawn = stats.rounds_drawn + 1) ?: UserStats(
                        id = p.id,
                        total_matches = 1,
                        rounds_drawn = 1,
                    )
                repoUsers.saveUserStats(newStats)
            }
        }
        matchPlayers.filter { it !in roundWinners }.forEach { p ->
            val stats = repoUsers.getUserStatsById(p.id)
            val newStats =
                stats?.copy(rounds_lost = stats.rounds_lost + 1) ?: UserStats(
                    id = p.id,
                    total_matches = 1,
                    rounds_lost = 1,
                )
            repoUsers.saveUserStats(newStats)
        }
    }

    private fun determineMatchWinners(
        match: Match,
        repoUsers: RepositoryUser,
    ): List<User> {
        val winsByPlayerId: Map<Int, Int> =
            match.rounds
                .flatMap { it.winners }
                .groupingBy { it.id }
                .eachCount()
        if (winsByPlayerId.isEmpty()) return emptyList()
        val maxWins = winsByPlayerId.values.maxOrNull() ?: 0
        val winners = match.players.filter { p -> winsByPlayerId[p.id] == maxWins }
        updatePlayerStatsAfterMatchCompletion(match.players, winners, repoUsers)
        return winners
    }

    private fun updatePlayerStatsAfterMatchCompletion(
        matchPlayers: List<User>,
        matchWinners: List<User>,
        repoUsers: RepositoryUser,
    ) {
        if (matchWinners.size == 1) {
            val winner = matchWinners[0]
            val stats = repoUsers.getUserStatsById(winner.id)
            val matchWonStats =
                stats?.copy(matches_won = stats.matches_won + 1) ?: UserStats(
                    id = winner.id,
                    total_matches = 1,
                    matches_won = 1,
                )
            val newStats = repoUsers.calculateWinrate(matchWonStats)
            repoUsers.saveUserStats(newStats)
        } else {
            matchPlayers.filter { it in matchWinners }.forEach { p ->
                val stats = repoUsers.getUserStatsById(p.id)
                val matchDrawnStats =
                    stats?.copy(matches_drawn = stats.matches_drawn + 1) ?: UserStats(
                        id = p.id,
                        total_matches = 1,
                        matches_drawn = 1,
                    )
                val newStats = repoUsers.calculateWinrate(matchDrawnStats)
                repoUsers.saveUserStats(newStats)
            }
        }
        matchPlayers.filter { it !in matchWinners }.forEach { p ->
            val stats = repoUsers.getUserStatsById(p.id)
            val matchLossStats =
                stats?.copy(matches_lost = stats.matches_lost + 1) ?: UserStats(
                    id = p.id,
                    total_matches = 1,
                    matches_lost = 1,
                )
            val newStats = repoUsers.calculateWinrate(matchLossStats)
            repoUsers.saveUserStats(newStats)
        }
    }

    private fun Match.getCurrentRoundOrNull(): Round? = rounds.getOrNull(currentRound - 1)
}
