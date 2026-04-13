package pt.isel.ls.server.services

import pt.isel.ls.server.common.Errors
import pt.isel.ls.server.data.CourtsData
import pt.isel.ls.server.domain.Court

/**
 * Classe que define os Services para [Court].
 * Contém métodos para criar um court, obter os detalhes de um court específico
 * e obter todos os courts de um club.
 *
 * @property courtsData Interface utilizada para manipulação dos courts.
 */
class CourtsServices(
    private val courtsData: CourtsData,
) {
    /**
     * Função que vai servir para criar um court.
     *
     * Caso o ID do club seja menor ou igual a 0, lança um erro a dizer que o parâmetro "club" é inválido.
     * Caso o nome do court esteja vazio, lança um erro a dizer que o parâmetro "name" está em falta.
     * Caso o nome do court não corresponda ao regex definido, lança um erro a dizer que o parâmetro "name" é inválido.
     * Por fim, chama a função createNewCourt do courtsData para criar o court que retorna o ID do novo court criado.
     *
     * @param name O nome do novo court.
     * @param club O ID do club ao qual o court pertence.
     * @return O ID do novo [Court] criado.
     */
    fun createNewCourt(
        name: String,
        club: Int,
    ): Int {
        if (club <= 0) throw Errors.invalidParameter("club")
        if (name.isEmpty()) throw Errors.missingParameter("name")
        if (!Regex("^[A-Za-zÀ-ÖØ-öø-ÿ0-9' -]+$").matches(name)) throw Errors.invalidParameter("name")
        return courtsData.createNewCourt(name, club)
    }

    /**
     * Função que vai servir para obter os detalhes de um court específico.
     *
     * Caso o ID do court seja menor ou igual a 0, lança um erro a dizer que o parâmetro "courtId" é inválido.
     * Por fim, chama a função getCourtDetails do courtsData para obter os detalhes do court.
     *
     * @param courtId O ID do court cujos detalhes se pretende obter.
     * @return Os detalhes do [Court] ou null se o court não existir.
     */
    fun getCourtDetails(courtId: Int): Court? {
        if (courtId <= 0) throw Errors.invalidParameter("courtId")
        return courtsData.getCourtDetails(courtId)
    }

    /**
     * Função que vai servir para obter todos os courts de um club.
     *
     * Caso o ID do club seja menor ou igual a 0, lança um erro a dizer que o parâmetro "club" é inválido.
     * Por fim, chama a função getAllCourtsFromAClub do courtsData para obter a lista de todos os courts do club.
     *
     * @param club O ID do club cujos courts se pretende obter.
     * @return A lista de objetos [Court] que correspondem ao club.
     */
    fun getAllCourtsFromAClub(club: Int): List<Court> {
        if (club <= 0) throw Errors.invalidParameter("club")
        return courtsData.getAllCourtsFromAClub(club)
    }
}
