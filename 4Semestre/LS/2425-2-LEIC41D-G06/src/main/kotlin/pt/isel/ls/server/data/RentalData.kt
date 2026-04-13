package pt.isel.ls.server.data

import kotlinx.datetime.LocalDate
import pt.isel.ls.server.domain.Rental

/**
 * Interface que define os métodos para manipulação de dados relacionados com [Rental].
 */
interface RentalData {
    fun createNewRental(
        date: LocalDate,
        startDuration: Int,
        endDuration: Int,
        club: Int,
        court: Int,
        user: Int,
    ): Int

    fun getRentalDetails(rid: Int): Rental?

    fun getRentals(
        club: Int,
        court: Int,
        date: LocalDate?,
    ): List<Rental>

    fun getRentalsOfUser(user: Int): List<Rental>

    fun getRentalsAvailableHours(
        club: Int,
        court: Int,
        date: LocalDate,
    ): List<Int>

    fun deleteRental(rental: Int): Boolean

    fun updateRental(
        rental: Int,
        date: LocalDate,
        startDuration: Int,
        endDuration: Int,
        club: Int,
        court: Int,
        user: Int,
    ): Rental
}
