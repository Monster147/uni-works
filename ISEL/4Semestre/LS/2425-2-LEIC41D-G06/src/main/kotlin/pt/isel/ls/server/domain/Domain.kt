package pt.isel.ls.server.domain

import kotlinx.datetime.LocalDate

/**
 * Data classes definem a estrutura das entidades [User], [Club], [Court] e [Rental].
 *
 * @property uid ID do user.
 * @property name Nome do user ou do club.
 * @property email Endereço de email do user.
 * @property token Token de autenticação do user.
 * @property password Palavra-passe do user.
 * @property cid ID do club.
 * @property crid ID do court.
 * @property rid ID do rental.
 * @property date Data do rental.
 * @property startDuration Hora de início do rental em horas.
 * @property endDuration Hora de fim do rental em horas.
 */
data class User(
    val uid: Int = 0,
    val name: String,
    val email: String,
    val token: String = "",
    val password: String = "",
)

data class Club(
    val cid: Int = 0,
    val name: String,
    val owner: Int = 0,
)

data class Court(
    val crid: Int = 0,
    val name: String,
    val club: Int,
)

data class Rental(
    val rid: Int = 0,
    val date: LocalDate,
    val startDuration: Int,
    val endDuration: Int,
    val user: Int = 0,
    val court: Int,
    val club: Int,
)
