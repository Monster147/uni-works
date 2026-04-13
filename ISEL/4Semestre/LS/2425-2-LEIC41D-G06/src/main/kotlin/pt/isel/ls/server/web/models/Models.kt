package pt.isel.ls.server.web.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Data classes que representam os vários modelos utilizados na aplicação.
 */
@Serializable
data class HttpErrorResponse(
    val code: Int,
    val error: String,
)

@Serializable
data class ClubsName(
    val name: String,
    val id: Int,
)

@Serializable
data class UserInput(
    val name: String,
    val email: String,
    val password: String,
)

@Serializable
data class UserTokenIdInput(
    val name: String,
    val password: String,
)

@Serializable
data class UserOutput(
    val id: Int,
    val name: String,
    val email: String,
    val token: String = "",
    val password: String = "",
)

@Serializable
data class UserTokenIdOutput(
    val token: String,
    val id: Int,
)

@Serializable
data class ClubInput(
    val name: String,
)

@Serializable
data class ClubOutput(
    val id: Int,
    val name: String,
    val owner: Int,
)

@Serializable
data class CourtInput(
    val name: String,
    val club: Int,
)

@Serializable
data class CourtOutput(
    val id: Int,
    val name: String,
    val club: Int,
)

@Serializable
data class RentalInput(
    val rid: Int = 0,
    val date: LocalDate,
    val startDuration: Int,
    val endDuration: Int,
    val user: Int = 0,
    val court: Int,
    val club: Int,
)

@Serializable
data class RentalOutput(
    val id: Int,
    val date: LocalDate,
    val startDuration: Int,
    val endDuration: Int,
    val user: Int,
    val court: Int,
    val club: Int,
)
