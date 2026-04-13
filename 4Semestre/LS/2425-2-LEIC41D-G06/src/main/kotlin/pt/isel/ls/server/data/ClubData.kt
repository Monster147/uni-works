package pt.isel.ls.server.data

import pt.isel.ls.server.domain.Club
import pt.isel.ls.server.web.models.ClubsName

/**
 * Interface que define os métodos para manipulação de dados relacionados com [Club].
 */
interface ClubData {
    fun createClubByName(
        name: String,
        owner: Int,
    ): Int

    fun getClubDetails(cid: Int): Club?

    fun getClubs(): List<ClubsName>

    fun searchClubsByName(name: String): List<ClubsName>
}
