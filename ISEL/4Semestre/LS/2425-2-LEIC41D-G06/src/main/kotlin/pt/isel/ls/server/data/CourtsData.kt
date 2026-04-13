package pt.isel.ls.server.data

import pt.isel.ls.server.domain.Court

/**
 * Interface que define os métodos para manipulação de dados relacionados com [Court].
 */
interface CourtsData {
    fun createNewCourt(
        name: String,
        club: Int,
    ): Int

    fun getCourtDetails(club: Int): Court?

    fun getAllCourtsFromAClub(club: Int): List<Court>
}
