package pt.isel.daw.repo.jdbc

import pt.isel.daw.repo.Transaction
import java.sql.Connection

class TransactionJdbc(
    private val con: Connection,
) : Transaction {
    override val repoUsers = RepositoryUserJdbc(con)
    override val repoLobbies = RepositoryLobbiesJdbc(con)
    override val repoMatches = RepositoryMatchesJdbc(con)
    override val repoRounds = RepositoryRoundsJdbc(con)
    override val repoTurns = RepositoryTurnsJdbc(con)
    override val repoInvitations = RepositoryInvitationJdbc(con)

    override fun rollback() {
        con.rollback()
    }
}
