package pt.isel.pdm.pokerDice.services

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import pt.isel.pdm.pokerDice.services.dto.CodeDTO
import pt.isel.pdm.pokerDice.services.dto.InvitationValidationDTO
import pt.isel.pdm.pokerDice.services.utils.APIConfig

class InvitationAPIServices(
    private val client: HttpClient,
    private val getToken: suspend () -> String?
): InvitationServicesInterface {

    private val baseUrl = APIConfig.BASE_URL

    override suspend fun createInvitation(createdBy: Int?): Either<InviteError, String> {
        try {
            val result = client.post("$baseUrl/api/invitations") {
                header(HttpHeaders.Authorization, "Bearer ${getToken()}")
            }

            when(result.status.value) {
                201 -> {
                    val code = result.body<CodeDTO>().toInvite()
                    return success(code.code)
                }
                else -> {
                    return failure(InviteError.Unknown)
                }
            }
        } catch (e: Exception) {
            return failure(InviteError.Unknown)
        }
        TODO("Not yet implemented")
    }

    override suspend fun isValid(code: String): Either<InviteError, Boolean> {
        try{
            val result = client.get("$baseUrl/api/invitations/$code")
            when(result.status.value) {
                200 -> {
                    val isValid = result.body<InvitationValidationDTO>()
                    return success(isValid.valid)
                }
                404 -> {
                    return failure(InviteError.NotFound)
                }
                400 -> {
                    return failure(InviteError.AlreadyUsed)
                }
                else -> {
                    return failure(InviteError.Unknown)
                }
            }
        } catch (e: Exception) {
            return failure(InviteError.Unknown)
        }
        TODO("Not yet implemented")
    }

}