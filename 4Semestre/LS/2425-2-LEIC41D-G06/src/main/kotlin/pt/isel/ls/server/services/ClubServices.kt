package pt.isel.ls.server.services

import pt.isel.ls.server.common.Errors
import pt.isel.ls.server.data.ClubData
import pt.isel.ls.server.domain.Club
import pt.isel.ls.server.web.models.ClubsName

/**
 * Classe que define os Services para os Clubs.
 * Contém métodos para criar um [Club], obter os detalhes de um determinado club, obter todos os clubs existentes
 * e para pesquisar clubs pelo nome.
 *
 * @property clubData Interface utilizada para manipulação dos clubs.
 */
class ClubServices(
    private val clubData: ClubData,
) {
    /**
     * Função que vai servir para criar um club.
     *
     * Caso o nome do club esteja vazio, lança um erro a dizer que o parâmetro "name" está em falta.
     * Caso o nome do club não corresponda ao regex definido, lança um erro a dizer que o parâmetro "name" é inválido.
     * Caso o ID do owner seja menor ou igual a 0, lança um erro a dizer que o parâmetro "owner" é inválido.
     * Por fim, chama a função createClubByName do clubData para criar o club que retorna o ID do novo club criado.
     * @param name O nome do novo club.
     * @param owner O ID do utilizador que está a criar o club.
     * @return O ID do novo [Club] criado.
     * */
    fun createClubByName(
        name: String,
        owner: Int,
    ): Int {
        if (name.isEmpty()) throw Errors.missingParameter("name")
        if (!Regex("^[A-Za-zÀ-ÖØ-öø-ÿ0-9' -]+$").matches(name)) throw Errors.invalidParameter("name")
        if (owner <= 0) throw Errors.invalidParameter("owner")
        return clubData.createClubByName(name, owner)
    }

    /**
     * Função que vai servir para obter os detalhes de um determinado club.
     *
     * Caso o ID do club seja menor ou igual a 0, lança um erro a dizer que o parâmetro "cid" é inválido.
     * Por fim, chama a função getClubDetails do clubData para obter os detalhes do club.
     *
     * @param cid O ID do club cujos detalhes se pretende obter.
     * @return Os detalhes do [Club] ou null se o club não existir.
     */
    fun getClubDetails(cid: Int): Club? {
        if (cid <= 0) throw Errors.invalidParameter("cid")
        return clubData.getClubDetails(cid)
    }

    /**
     * Função que vai servir para obter todos os clubs existentes.
     *
     * Chama a função getClubs do clubData para obter a lista de todos os clubs.
     *
     * @return A lista de todos os clubs existentes.
     */
    fun getClubs(): List<ClubsName> = clubData.getClubs()

    /**
     * Função que vai servir para pesquisar clubs pelo nome.
     *
     * Caso o nome esteja vazio, lança um erro a dizer que o parâmetro "name" está em falta.
     * Por fim, chama a função searchClubsByName do clubData para obter a lista de clubs que correspondem a esse nome.
     *
     * @param name O nome pelo qual se pretende pesquisar os clubs.
     * @return A lista de objetos [ClubsName] que correspondem ao nome pesquisado.
     */
    fun searchClubByName(name: String): List<ClubsName> {
        if (name.isEmpty()) throw Errors.missingParameter("name")
        return clubData.searchClubsByName(name)
    }
}
