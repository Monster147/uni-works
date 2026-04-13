package pt.isel.ls.server.data.memory

import pt.isel.ls.server.common.Errors
import pt.isel.ls.server.data.ClubData
import pt.isel.ls.server.domain.Club
import pt.isel.ls.server.web.models.ClubsName

/**
 * Classe que implementa a interface [ClubData] para manipulação de clubs em memória.
 * Contém métodos para criar um club, obter os detalhes de um determinado club, obter todos os clubs existentes
 * e para pesquisar clubs pelo nome.
 */

class ClubDataMem : ClubData {
    /**
     * Função que vai servir para criar um novo club.
     *
     * Caso o nome do club já exista e o owner dele for o mesmo, lança um erro de duplicação.
     * Guarda no val cid o ID do novo club, que é o tamanho atual da lista de clubs mais 1.
     * Cria um objeto [Club] com o ID, nome e owner.
     * Adiciona o novo club à lista de clubs.
     * Por fim, retorna o ID do novo club criado.
     *
     * @param name O nome do novo club.
     * @param owner O ID do utilizador que está a criar o club.
     * @return O ID do novo club criado.
     */
    override fun createClubByName(
        name: String,
        owner: Int,
    ): Int {
        if (Clubs.any { it.name == name && it.owner == owner }) throw Errors.duplicateClub(name, owner)
        val cid = Clubs.size + 1
        val club = Club(cid, name, owner)
        Clubs.add(club)
        return cid
        // Verificar se o respetivo nome não está presente no array e associá-lo a um Int unico
    }

    /**
     * Função que vai servir para obter os detalhes de um determinado club.
     *
     * Caso o ID do club esteja presente na lista de clubs, retorna o club correspondente.
     *
     * @param cid O ID do club cujos detalhes se pretende obter.
     * @return Os detalhes do club ou null se o club não existir.
     */
    override fun getClubDetails(cid: Int): Club? {
        return Clubs.find { it.cid == cid }
        // Verificar se o clube existe,se existir devolver o clube.
    }

    /**
     * Função que vai servir para obter todos os clubs existentes.
     *
     * Guarda no val clubsNames uma lista mutável de [ClubsName].
     * Percorre a lista de Clubs e adiciona cada club à lista clubsNames como um objeto ClubsName,
     * Por fim, retorna a lista de clubsNames.
     *
     * @return A lista de todos os clubs existentes.
     */
    override fun getClubs(): List<ClubsName> {
        val clubsNames = mutableListOf<ClubsName>()
        Clubs.forEach {
            clubsNames.add(ClubsName(it.name, it.cid))
        }
        return clubsNames
        // Devolver uma lista de todos os clubes (só nome)
    }

    /**
     * Função que vai servir para pesquisar clubs pelo nome.
     *
     * Guarda no val query o nome do club em letras minúsculas e sem espaços.
     * Filtra a lista de Clubs, verificando se o nome do club em letras minúsculas e sem espaços contém o query.
     * Mapeia os resultados filtrados para uma lista de [ClubsName], contendo o nome e o ID do club.
     * Por fim, retorna a lista de ClubsName.
     *
     * @param name O nome do club a ser pesquisado.
     * @return A lista de clubs cujo nome contém a string pesquisada.
     */
    override fun searchClubsByName(name: String): List<ClubsName> {
        val query = name.lowercase().replace(" ", "")
        return Clubs
            .filter {
                it.name
                    .lowercase()
                    .replace(" ", "")
                    .contains(query)
            }.map {
                ClubsName(it.name, it.cid)
            }
    }
}
