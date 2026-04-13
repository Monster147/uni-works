package pt.isel.daw.service

import org.springframework.stereotype.Component
import pt.isel.daw.repo.TransactionManager

sealed class InviteError {
    object NotFound : InviteError()

    object AlreadyUsed : InviteError()

    object Unknown : InviteError()
}

@Component
class InvitationServices(
    private val trxManager: TransactionManager,
) {
    fun createInvitation(createdBy: Int?): Either<InviteError, String> {
        return trxManager.run {
            try {
                val inv = repoInvitations.create(createdBy)
                success(inv.code)
            } catch (e: Exception) {
                failure(InviteError.Unknown)
            }
        }
    }

    fun isValid(code: String): Either<InviteError, Boolean> {
        return trxManager.run {
            val inv = repoInvitations.findByCode(code)
            when {
                inv == null -> failure(InviteError.NotFound)
                inv.used -> failure(InviteError.AlreadyUsed)
                else -> success(true)
            }
        }
    }
}
