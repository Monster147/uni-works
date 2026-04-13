package pt.isel.daw.repo.mem

import pt.isel.daw.repo.RepositoryInvitation
import pt.isel.daw.repo.RepositoryLobby
import pt.isel.daw.repo.RepositoryMatches
import pt.isel.daw.repo.RepositoryRounds
import pt.isel.daw.repo.RepositoryTurns
import pt.isel.daw.repo.RepositoryUser
import pt.isel.daw.repo.Transaction

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
