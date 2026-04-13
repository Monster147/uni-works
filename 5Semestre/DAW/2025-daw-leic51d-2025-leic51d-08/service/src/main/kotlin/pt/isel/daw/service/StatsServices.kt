package pt.isel.daw.service

import org.springframework.stereotype.Component
import pt.isel.daw.UserStats
import pt.isel.daw.repo.TransactionManager

@Component
class StatsServices(
    private val trxManager: TransactionManager,
) {
    fun getUserStatsById(id: Int): Either<UserError, UserStats> {
        return trxManager.run {
            val result = repoUsers.getUserStatsById(id)
            if (result == null) return@run failure(UserError.StatsNotFound)
            success(result)
        }
    }
}
