package pt.isel.pdm.pokerDice.repo.mem

import pt.isel.pdm.pokerDice.repo.RepositoryInvitation
import pt.isel.pdm.pokerDice.repo.RepositoryLobby
import pt.isel.pdm.pokerDice.repo.RepositoryMatches
import pt.isel.pdm.pokerDice.repo.RepositoryRounds
import pt.isel.pdm.pokerDice.repo.RepositoryTurns
import pt.isel.pdm.pokerDice.repo.RepositoryUser
import pt.isel.pdm.pokerDice.repo.Transaction

class TransactionInMem(
    override val repoUsers: RepositoryUser,
    override val repoLobbies: RepositoryLobby,
    override val repoMatches: RepositoryMatches,
    override val repoRounds: RepositoryRounds,
    override val repoTurns: RepositoryTurns,
    override val repoInvitations: RepositoryInvitation,
) : Transaction {
    override fun rollback(): Unit = throw UnsupportedOperationException()
}
