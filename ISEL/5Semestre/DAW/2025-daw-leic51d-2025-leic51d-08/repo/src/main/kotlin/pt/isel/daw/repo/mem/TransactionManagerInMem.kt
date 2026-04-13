package pt.isel.daw.repo.mem

import pt.isel.daw.repo.Transaction
import pt.isel.daw.repo.TransactionManager

class TransactionManagerInMem : TransactionManager {
    private val repoUsers = RepositoryUserInMem()
    private val repoLobbies = RepositoryLobbiesInMem()
    private val repoMatches = RepositoryMatchesInMem()
    private val repoRounds = RepositoryRoundsInMem()
    private val repoTurns = RepositoryTurnsInMem()
    private val repoInvitations = RepositoryInvitationInMem()

    override fun <R> run(block: Transaction.() -> R): R =
        block(TransactionInMem(repoUsers, repoLobbies, repoMatches, repoRounds, repoTurns, repoInvitations))
}
