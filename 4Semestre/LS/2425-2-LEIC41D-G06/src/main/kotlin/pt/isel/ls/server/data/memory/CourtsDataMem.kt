package pt.isel.ls.server.data.memory

import pt.isel.ls.server.common.Errors
import pt.isel.ls.server.data.CourtsData
import pt.isel.ls.server.domain.Court

/**
 * Classe que implementa a interface [CourtsData] para manipulação de courts em memória.
 * Esta classe permite criar courts, obter detalhes de um court específico
 * e listar todos os courts associados a um club específico.
 */
class CourtsDataMem : CourtsData {
    /**
     * Função que cria um court associado a um clube.
     *
     * Caso o club não exista, lança um erro de clube não encontrado.
     * Caso já exista um court com o mesmo nome associado a um club com o mesmo nome, lança um erro de court duplicado.
     * Guarda no val crid o ID do novo court, que é o tamanho atual da lista de courts mais 1.
     * Guarda no val court um novo objeto Court com o ID, nome e ID do club.
     * Adiciona o novo court à lista de courts.
     * Por fim, retorna o ID do novo court criado.
     *
     * @param name O nome do court a ser criado.
     * @param club O ID do club ao qual o court será associado.
     * @return O ID do novo court criado.
     */
    override fun createNewCourt(
        name: String,
        club: Int,
    ): Int {
        if (!Clubs.any { it.cid == club }) throw Errors.clubNotFound(club)
        if (Courts.any { it.name == name && it.club == club }) throw Errors.duplicateCourt(name)
        val crid = Courts.size + 1
        val court = Court(crid, name, club)
        Courts.add(court)
        return crid
        // verifica se o club existe(cid), caso exista irá criar um court
    }

    /**
     * Função que obtém os detalhes de um court específico.
     *
     * Verifica se existe um court com o ID fornecido.
     * Se existir, retorna o court correspondente, caso contrário, retorna null.
     *
     * @param courtId O ID do court cujos detalhes pretende-se obter.
     * @return Os detalhes do court ou null se o court não existir.
     */
    override fun getCourtDetails(courtId: Int): Court? {
        return Courts.find { it.crid == courtId }
        // Verificar se existe um court com esse crid, se existir devolver respetivo court
    }

    /**
     * Função que obtém todos os courts associados a um club específico.
     *
     * Verifica se o club existe. Se não existir, lança um erro que diz que o club não foi encontrado.
     * Filtra a lista de courts para retornar apenas aqueles associados ao ID do club fornecido.
     *
     * @param club O ID do club cujos courts pretende-se obter.
     * @return Uma lista de courts associados ao club especificado.
     */
    override fun getAllCourtsFromAClub(club: Int): List<Court> {
        // check(Clubs.any { it.cid == club }) { "Club not found" }
        if (!Clubs.any { it.cid == club }) throw Errors.clubNotFound(club)
        return Courts.filter { it.club == club }
        // Verifica se existe algum court associado a esse club(CID), se existir devolve uma lista com todos os courts associados
    }
}
