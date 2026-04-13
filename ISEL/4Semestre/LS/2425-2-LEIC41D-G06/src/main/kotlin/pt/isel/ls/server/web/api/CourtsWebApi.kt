package pt.isel.ls.server.web.api

import kotlinx.serialization.json.Json
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.routing.path
import pt.isel.ls.server.common.Errors
import pt.isel.ls.server.logRequest
import pt.isel.ls.server.services.CourtsServices
import pt.isel.ls.server.web.errorToHttp
import pt.isel.ls.server.web.models.CourtInput
import pt.isel.ls.server.web.models.CourtOutput

/**
 * Classe que define a API web para os Courts.
 * Contém métodos para criar um court, obter os detalhes de um court específico
 * e obter todos os courts de um club.
 *
 * @property courtsServices Serviço para manipulação dos courts.
 */
class CourtsWebApi(
    private val courtsServices: CourtsServices,
) {
    /**
     * Função que vai servir para criar um court.
     *
     * Chama a função auxiliar handleRequest para tratar a requisição HTTP e capturar erros.
     * Guarda no val court os dados do novo court(nome e club) a partir do body da requisição.
     * Guarda no val id o id do novo court criado através do serviço dos courts.
     * Por fim, retorna uma resposta HTTP com o status de criação (201 Created) e os detalhes do novo court criado.
     *
     * @param request A requisição HTTP que contém os dados do novo court.
     * @return Response A resposta HTTP com o status de criação e os detalhes do novo court.
     */
    fun createCourt(request: Request): Response =
        handleRequest {
            logRequest(request)
            val court = Json.decodeFromString<CourtInput>(request.bodyString())
            val id = courtsServices.createNewCourt(court.name, court.club)
            responseCreated(CourtOutput(id, court.name, court.club))
        }

    /**
     * Função que vai servir para obter os detalhes de um court específico.
     *
     * Chama a função auxiliar handleRequest para tratar a requisição HTTP e capturar erros.
     * Guarda no val courtId o ID do court a partir do path do request, lançando um erro caso
     * não seja possível obter ou converter o valor num inteiro.
     * Guarda no val court os detalhes do court obtidos através do serviço dos courts.
     * Caso o serviço dos courts retorne null, lança um erro a dizer que esse court não existe.
     * Por fim, retorna uma resposta HTTP com o status de sucesso (200 OK) e os detalhes do court.
     *
     * @param request A requisição HTTP que contém o ID do court no path.
     * @return Response A resposta HTTP com o status de sucesso e os detalhes do court.
     */
    fun getCourtDetails(request: Request): Response =
        handleRequest {
            logRequest(request)
            val courtId =
                request.path("courtId")?.toInt()
                    ?: throw Errors.invalidBody()
            val court = courtsServices.getCourtDetails(courtId)
            if (court != null) {
                responseOk(CourtOutput(court.crid, court.name, court.club))
            } else {
                errorToHttp(Errors.courtNotFound(courtId))
            }
        }

    /**
     * Função que vai servir para obter todos os courts de um club específico.
     *
     * Chama a função auxiliar handleRequest para tratar a requisição HTTP e capturar erros.
     * Guarda no val clubId o ID do club a partir do path do request, lançando um erro caso
     * não seja possível obter ou converter o valor num inteiro.
     * Guarda no val skip o número de rentals a serem saltados, obtido a partir do query "skip" do request,
     * caso não exista, assume o valor de 0.
     * Guarda no val limit o número máximo de rentals a serem retornados, obtido
     * a partir do query "limit" do request.
     * Guarda no val courts a lista de courts obtidos através do serviço dos courts,
     * removendo os primeiros "skip" courts e limitando o número de courts agarrando os primeiros "limit".
     * Por fim, retorna uma resposta HTTP com o status de sucesso (200 OK) e a lista de courts.
     *
     * @param request A requisição HTTP que pode conter os parâmetros de skip e limit.
     * @return Response A resposta HTTP com o status de sucesso e a lista de courts.
     */
    fun getAllCourtsFromClub(request: Request): Response =
        handleRequest {
            logRequest(request)
            val clubId =
                request.path("clubId")?.toInt()
                    ?: throw Errors.invalidBody()
            val skip = request.query("skip")?.toIntOrNull() ?: 0
            val limit = request.query("limit")?.toIntOrNull()
            val courts =
                if (limit != null) {
                    courtsServices.getAllCourtsFromAClub(clubId).drop(skip).take(limit)
                } else {
                    courtsServices.getAllCourtsFromAClub(clubId).drop(skip)
                }
            responseOk(courts.map { CourtOutput(it.crid, it.name, it.club) })
        }
}
