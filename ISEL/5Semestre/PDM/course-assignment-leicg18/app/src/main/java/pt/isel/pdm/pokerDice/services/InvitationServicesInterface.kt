package pt.isel.pdm.pokerDice.services

interface InvitationServicesInterface {

    suspend fun createInvitation(createdBy: Int?): Either<InviteError, String>

    suspend fun isValid(code: String): Either<InviteError, Boolean>

}