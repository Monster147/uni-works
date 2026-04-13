package pt.isel.ls.server.common

import kotlinx.datetime.LocalDate

/**
 * Enum class que define os códigos de erro utilizados na aplicação.
 * Cada código de erro tem um valor inteiro (o seu code) e uma descrição associada.
 *
 * @property code O código de erro associado ao erro.
 * @property description A descrição detalhada do erro.
 */
enum class ErrorCode(
    val code: Int,
    val description: String,
) {
    MISSING_PARAMETER(1, "Missing parameter"),
    INVALID_PARAMETER(2, "Invalid parameter"),
    INVALID_BODY(3, "Invalid request body"),
    CLUB_NOT_FOUND(4, "Club not found"),
    COURT_NOT_FOUND(5, "Court not found"),
    RENTAL_NOT_FOUND(6, "Rental not found"),
    DUPLICATE_CLUB(7, "Club already exists"),
    DUPLICATE_COURT(8, "Court already exists"),
    USER_NOT_FOUND(9, "User not found"),
    DUPLICATE_USER(10, "User already exists"),
    MISSING_TOKEN(11, "Missing authorization token"),
    CLUB_NOT_IN_COURT(12, "Court not found in this club"),
    DUPLICATE_RENTAL(13, "Rental already exists"),
    ID_NOT_FOUND(14, "ID not found"),
    COURT_ALREADY_RENTED(15, "Court already rented for the specified time"),
    SERVER_ERROR(16, "Internal server error"),
    CLUB_NAME_NOT_FOUND(17, "Club name not found"),
    DUPLICATE_ENTITY(18, "Entity already exists"),
    NAME_NOT_FOUND(19, "Name not found"),
    PASSWORD_NOT_FOUND(20, "Password not found"),
}

/**
 * Classe que representa um erro na aplicação.
 * Contém um código de erro e uma descrição detalhada do erro.
 *
 * @property code O código de erro associado ao erro.
 * @property details A descrição detalhada do erro.
 */
data class AppError(
    val code: ErrorCode,
    val details: String,
) : Throwable()

/**
 * Objeto que contém métodos para criar erros específicos da aplicação.
 *
 * Cada método retorna uma instancia de AppError com um código de erro específico e uma mensagem detalhada.
 */
object Errors {
    fun missingParameter(param: String) = AppError(ErrorCode.MISSING_PARAMETER, "Missing parameter: $param")

    fun invalidParameter(param: String) = AppError(ErrorCode.INVALID_PARAMETER, "Invalid parameter: $param")

    fun invalidBody() = AppError(ErrorCode.INVALID_BODY, "Invalid request body")

    fun clubNotFound(clubId: Int) = AppError(ErrorCode.CLUB_NOT_FOUND, "Club with ID $clubId not found")

    fun clubNameNotFound(clubName: String) = AppError(ErrorCode.CLUB_NAME_NOT_FOUND, "Club with name $clubName not found")

    fun courtNotFound(courtId: Int) = AppError(ErrorCode.COURT_NOT_FOUND, "Court with ID $courtId not found")

    fun rentalNotFound(rentalId: Int) = AppError(ErrorCode.RENTAL_NOT_FOUND, "Rental with ID $rentalId not found")

    fun duplicateClub(
        name: String,
        owner: Int,
    ) = AppError(ErrorCode.DUPLICATE_CLUB, "Club with name $name and owner $owner already exists")

    fun duplicateCourt(name: String) = AppError(ErrorCode.DUPLICATE_COURT, "Court with name $name already exists")

    fun userNotFound(user: String) = AppError(ErrorCode.USER_NOT_FOUND, "User $user not found")

    fun duplicateUser(email: String) = AppError(ErrorCode.DUPLICATE_USER, "User with email $email already exists")

    fun missingToken() = AppError(ErrorCode.MISSING_TOKEN, "Missing authorization token")

    fun clubNotInCourt(
        clubId: Int,
        courtId: Int,
    ) = AppError(
        ErrorCode.CLUB_NOT_IN_COURT,
        "Court with ID $courtId not found in club with ID $clubId",
    )

    fun duplicateRental(
        clubId: Int,
        courtId: Int,
        date: LocalDate,
    ) = AppError(
        ErrorCode.DUPLICATE_RENTAL,
        "Rental already exists for club $clubId in court $courtId on date $date",
    )

    fun duplicateEntity(entity: String) = AppError(ErrorCode.DUPLICATE_ENTITY, "$entity already exists.")

    fun idNotFound(
        entity: String,
        id: Int,
    ) = AppError(ErrorCode.ID_NOT_FOUND, "$entity with id $id not found")

    fun courtAlreadyRented(
        clubId: Int,
        courtId: Int,
        date: LocalDate,
    ) = AppError(
        ErrorCode.COURT_ALREADY_RENTED,
        "Court with ID $courtId in club $clubId already rented on date $date",
    )

    fun serverError() = AppError(ErrorCode.SERVER_ERROR, "Internal server error")

    fun alreadyRented(
        clubId: Int,
        courtId: Int,
        date: LocalDate,
        startDuration: Int,
        endDuration: Int,
        availableHours: List<Int>,
    ) = AppError(
        ErrorCode.COURT_ALREADY_RENTED,
        "Court with ID $courtId in club $clubId already rented on date $date " +
            "for the time range $startDuration to $endDuration. " +
            "Available hours are $availableHours",
    )

    fun nameNotFound(name: String) = AppError(ErrorCode.NAME_NOT_FOUND, "Name $name not found, please try again")

    fun passwordNotFound() = AppError(ErrorCode.PASSWORD_NOT_FOUND, "Password not found, please try again")
}
