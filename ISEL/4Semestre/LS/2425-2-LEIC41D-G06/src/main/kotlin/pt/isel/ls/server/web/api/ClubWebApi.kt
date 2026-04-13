package pt.isel.ls.server.web.api

import kotlinx.serialization.json.Json
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.routing.path
import pt.isel.ls.server.common.Errors
import pt.isel.ls.server.logRequest
import pt.isel.ls.server.services.ClubServices
import pt.isel.ls.server.services.UserServices
import pt.isel.ls.server.web.models.ClubInput
import pt.isel.ls.server.web.models.ClubOutput

/**
 * Classe que define a API web para os Clubs.
 * Contém métodos para criar um club, obter os detalhes de um determinado club, obter todos os clubs existentes
 * e para pesquisar clubs pelo nome.
 *
 * @property clubServices Serviço para manipulação dos clubs.
 * @property userServices Serviço para manipulação dos users.
 */

class ClubWebApi(
    private val clubServices: ClubServices,
    private val userServices: UserServices,
) {
    /**
     * Função que vai servir para criar um club.
     *
     * Chama a função auxiliar handleRequest para tratar a requisição HTTP e capturar erros.
     * Guarda no val club os dados do novo club a partir do body da requisição, neste caso o nome do club.
     * Guarda no val owner o ID do utilizador que está a criar o club, obtido através do header "authorization"
     * caso este parâmetro não esteja preenchido irá lançar um error a dizer que o token não foi encontrado.
     * Guarda no val id o novo club criado através do serviço dos clubs, passando o nome do club e o owner.
     * Por fim retorna uma resposta HTTP com o status de criação (201 Created) e os detalhes do novo club criado.
     *
     * @param request A requisição HTTP que contém os dados do novo club.
     * @return Response A resposta HTTP com o status de criação e os detalhes do novo club.
     */
    fun createClub(request: Request): Response =
        handleRequest {
            logRequest(request)
            val club = Json.decodeFromString<ClubInput>(request.bodyString())
            val owner =
                request.header("authorization")?.let { userServices.getUserIdByToken(it) }
                    ?: throw Errors.missingToken()
            val id = clubServices.createClubByName(club.name, owner)
            responseCreated(ClubOutput(id, club.name, owner))
        }

    /**
     * Função que vai servir para obter os detalhes de um determinado club.
     *
     * Chama a função auxiliar handleRequest para tratar a requisição HTTP e capturar erros.
     * Guarda no val clubId o ID do club a partir do path do request, lançando um erro caso
     * não seja possível obter ou converter o valor num inteiro.
     * Guarda no val club os detalhes do club obtidos através do serviço dos clubs, passando o ID do club,
     * caso o serviço dos clubs retorne null em fez de um club lança um erro a dizer que esse clube não existe.
     * Por fim, retorna uma resposta HTTP com o status de sucesso (200 OK) e os detalhes do club.
     *
     * @param request A requisição HTTP que contém o ID do club no path.
     * @return Response A resposta HTTP com o status de sucesso e os detalhes do club.
     */
    fun getClubDetails(request: Request): Response =
        handleRequest {
            logRequest(request)
            val clubId = request.path("clubId")?.toInt() ?: throw Errors.invalidBody()
            val club =
                clubServices.getClubDetails(clubId)
                    ?: throw Errors.clubNotFound(clubId)

            responseOk(ClubOutput(club.cid, club.name, club.owner))
        }

    /**
     * Função que vai servir para obter todos os clubs existentes.
     *
     * Chama a função auxiliar handleRequest para tratar a requisição HTTP e capturar erros.
     * Guarda no val skip, caso exista, o número de clubs a serem saltados, obtido a partir do query
     * "skip" do request, caso não exista irá assumir o valor de 0.
     * Guarda no val limit, caso exista, o número máximo de clubs a serem retornados, obtido
     * a partir do query "limit" do request,
     * Guarda no val clubs a lista de clubs obtidos através do serviço dos clubs, removendo
     * os primeiros "skip" clubs e limitando o número de clubs agarrando os primeiros "limit",
     * caso este exista.
     * Por fim, retorna uma resposta HTTP com o status de sucesso (200 OK) e a lista de clubs.
     *
     * @param request A requisição HTTP que pode conter os parâmetros de skip e limit.
     * @return Response A resposta HTTP com o status de sucesso e a lista de clubs.
     */
    fun getAllClubs(request: Request): Response =
        handleRequest {
            logRequest(request)
            val skip = request.query("skip")?.toIntOrNull() ?: 0
            val limit = request.query("limit")?.toIntOrNull()
            val clubs =
                if (limit != null) {
                    clubServices.getClubs().drop(skip).take(limit)
                } else {
                    clubServices.getClubs().drop(skip)
                }

            responseOk(clubs)
        }

    /**
     * Função que vai servir para pesquisar clubs pelo nome.
     *
     * Chama a função auxiliar handleRequest para tratar a requisição HTTP e capturar erros.
     * Guarda no val name o nome do club a partir do path do request e remove os espaços em branco.
     * Caso o nome seja nulo ou vazio, lança um erro a dizer que o parâmetro "name" não está presente.
     * Guarda no val clubs a lista de clubs obtidos através do serviço dos clubs com resultado da pesquisa desse nome.
     * Caso a lista de clubs esteja vazia, lança um erro a dizer que o club com esse nome não foi encontrado.
     * Por fim, retorna uma resposta HTTP com o status de sucesso (200 OK) e a lista de clubs encontrados.
     *
     * @param request A requisição HTTP que contém o nome do club no path.
     * @return Response A resposta HTTP com o status de sucesso e a lista de clubs encontrados.
     */
    fun searchClubByName(request: Request): Response =
        handleRequest {
            logRequest(request)
            val name = request.path("name")?.trim()
            if (name == null || name == "") throw Errors.missingParameter("name")
            val clubs = clubServices.searchClubByName(name)
            if (clubs.isEmpty()) throw Errors.clubNameNotFound(name)
            responseOk(clubs)
        }
}
