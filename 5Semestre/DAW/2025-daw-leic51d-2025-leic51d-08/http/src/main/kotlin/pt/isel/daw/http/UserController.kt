package pt.isel.daw

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.http.model.Problem
import pt.isel.daw.http.model.UserCreateTokenInputModel
import pt.isel.daw.http.model.UserCreateTokenOutputModel
import pt.isel.daw.http.model.UserCreated
import pt.isel.daw.http.model.UserHomeOutputModel
import pt.isel.daw.http.model.UserInput
import pt.isel.daw.service.Either
import pt.isel.daw.service.Failure
import pt.isel.daw.service.StatsServices
import pt.isel.daw.service.Success
import pt.isel.daw.service.TokenCreationError
import pt.isel.daw.service.UserError
import pt.isel.daw.service.UserServices

@RestController
class UserController(
    private val userServices: UserServices,
    private val statsServices: StatsServices,
) {
    val testUserServices get() = userServices

    @PostMapping("/api/users")
    fun createUser(
        @RequestBody userInput: UserInput,
    ): ResponseEntity<*> {
        val result: Either<UserError, User> =
            userServices
                .createUser(userInput.name, userInput.email, userInput.password, userInput.invite_code)

        return when (result) {
            is Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .header(
                        "Location",
                        "/api/users/${result.value.id}",
                    )
                    .body(
                        UserCreated(
                            id = result.value.id,
                            name = result.value.name,
                            email = result.value.email,
                            password = result.value.passwordValidation,
                            balance = result.value.balance,
                        ),
                    )

            is Failure ->
                when (result.value) {
                    is UserError.AlreadyUsedEmailAddress ->
                        Problem.EmailAlreadyInUse.response(
                            HttpStatus.BAD_REQUEST,
                        )

                    UserError.InsecurePassword ->
                        Problem.InsecurePassword.response(
                            HttpStatus.BAD_REQUEST,
                        )

                    UserError.InvalidInvitation ->
                        Problem.InvalidInvitation.response(
                            HttpStatus.BAD_REQUEST,
                        )

                    else ->
                        Problem.UnknownError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }
    }

    @PostMapping("/api/users/token")
    fun token(
        @RequestBody input: UserCreateTokenInputModel,
    ): ResponseEntity<*> {
        val res = userServices.createToken(input.email, input.password)
        return when (res) {
            is Success ->
                ResponseEntity
                    .status(HttpStatus.OK)
                    .body(UserCreateTokenOutputModel(res.value.tokenValue))

            is Failure ->
                when (res.value) {
                    TokenCreationError.UserOrPasswordAreInvalid ->
                        Problem.UserOrPasswordAreInvalid.response(HttpStatus.BAD_REQUEST)
                }
        }
    }

    @PostMapping("api/logout")
    fun logout(user: AuthenticatedUser) {
        userServices.revokeToken(user.token)
    }

    @GetMapping("/api/me")
    fun userHome(userAuthenticatedUser: AuthenticatedUser): ResponseEntity<UserHomeOutputModel> {
        val res = userServices.getUserByToken(userAuthenticatedUser.token)
        if (res != null) {
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                    UserHomeOutputModel(
                        id = userAuthenticatedUser.user.id,
                        name = userAuthenticatedUser.user.name,
                        email = userAuthenticatedUser.user.email,
                        balance = userAuthenticatedUser.user.balance,
                    ),
                )
        } else {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build()
        }
    }

    @GetMapping("/api/me/stats")
    fun getUserStats(userAuthenticatedUser: AuthenticatedUser): ResponseEntity<Any> {
        val statsResult = statsServices.getUserStatsById(userAuthenticatedUser.user.id)
        return when (statsResult) {
            is Success ->
                ResponseEntity
                    .status(HttpStatus.OK)
                    .body(statsResult.value)

            is Failure ->
                when (statsResult.value) {
                    UserError.StatsNotFound -> Problem.StatsNotFound.response(HttpStatus.NOT_FOUND)
                    else -> Problem.UnknownError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }
    }
}
