package pt.isel.daw.http

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.AuthenticatedUser
import pt.isel.daw.http.model.CodeOutput
import pt.isel.daw.http.model.Problem
import pt.isel.daw.http.model.ValidCodeOutput
import pt.isel.daw.service.Failure
import pt.isel.daw.service.InvitationServices
import pt.isel.daw.service.InviteError
import pt.isel.daw.service.Success

@RestController
class InvitationController(
    private val invitationServices: InvitationServices,
) {
    @PostMapping("/api/invitations")
    fun createInvitation(authUser: AuthenticatedUser): ResponseEntity<*> {
        val codeResult = invitationServices.createInvitation(authUser.user.id)
        return when (codeResult) {
            is Success -> {
                val codeOutput = CodeOutput(codeResult.value)
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(codeOutput)
            }

            is Failure -> handleInviteError(codeResult)
        }
    }

    @GetMapping("/api/invitations/{code}")
    fun isValid(
        @PathVariable code: String,
    ): ResponseEntity<*> {
        val validResult = invitationServices.isValid(code)
        return when (validResult) {
            is Success -> {
                val codeOutput = ValidCodeOutput(validResult.value)
                ResponseEntity
                    .status(HttpStatus.OK)
                    .body(codeOutput)
            }

            is Failure -> handleInviteError(validResult.value)
        }
    }

    private fun handleInviteError(error: Any): ResponseEntity<*> {
        return when (error) {
            is InviteError.NotFound ->
                Problem.InvalidInvitation.response(
                    HttpStatus.NOT_FOUND,
                )

            is InviteError.AlreadyUsed ->
                Problem.InvitationAlreadyUsed.response(
                    HttpStatus.BAD_REQUEST,
                )

            is InviteError.Unknown ->
                Problem.UnknownError.response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                )

            else ->
                Problem.UnknownError.response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                )
        }
    }
}
