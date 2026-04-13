package pt.isel.daw.repo

/**
 * The lifecycle of a Transaction is managed outside the scope of the IoC/DI container.
 * Transactions are instantiated by a TransactionManager,
 * which is managed by the IoC/DI container (e.g., Spring).
 * The implementation of Transaction is responsible for creating the
 * necessary repository instances in its constructor.
 */
interface Transaction {
    val repoUsers: RepositoryUser
    val repoLobbies: RepositoryLobby
    val repoMatches: RepositoryMatches
    val repoTurns: RepositoryTurns
    val repoRounds: RepositoryRounds
    val repoInvitations: RepositoryInvitation

    fun rollback()
}
