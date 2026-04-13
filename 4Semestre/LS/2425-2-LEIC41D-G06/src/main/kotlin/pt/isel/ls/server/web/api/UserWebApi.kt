package pt.isel.ls.server.web.api

import kotlinx.serialization.json.Json
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.routing.path
import pt.isel.ls.server.common.Errors
import pt.isel.ls.server.logRequest
import pt.isel.ls.server.services.UserServices
import pt.isel.ls.server.web.errorToHttp
import pt.isel.ls.server.web.models.UserInput
import pt.isel.ls.server.web.models.UserOutput
import pt.isel.ls.server.web.models.UserTokenIdInput
import pt.isel.ls.server.web.models.UserTokenIdOutput

/**
 * Classe que define a API web para os Users.
 * Contém métodos para criar um user, obter os detalhes de um determinado user
 * e obter o token e o id de um user.
 *
 * @property userServices Serviço para manipulação dos users.
 */

class UserWebApi(
    private val userServices: UserServices,
) {
    /**
     * Função que vai servir para criar um user.
     *
     * Chama a função auxiliar handleRequest para tratar a requisição HTTP e capturar erros.
     * Guarda no val user os dados do novo user (nome, email e password) a partir do body da requisição.
     * Guarda no val (token, id) o token e o id do novo user criado através do serviço dos users,
     * Por fim, retorna uma resposta HTTP com o status de criação (201 Created) e os detalhes do novo user criado.
     * Caso ocorra um erro ao criar a resposta, lança um erro do server.
     *
     * @param request A requisição HTTP que contém os dados do novo user.
     * @return Response A resposta HTTP com o status de criação e os detalhes do novo user.
     */
    fun createUser(request: Request): Response =
        handleRequest {
            logRequest(request)
            val user = Json.decodeFromString<UserInput>(request.bodyString())
            val (token, id) = userServices.createNewUser(user.name, user.email, user.password)
            try {
                responseCreated(UserOutput(id, user.name, user.email, token.toString(), user.password))
            } catch (e: Exception) {
                // Log a exceção para depuração
                println("Erro ao criar resposta: ${e.message}")
                throw Errors.serverError()
            }
        }

    /**
     * Função que vai servir para obter os detalhes de um determinado user.
     *
     * Chama a função auxiliar handleRequest para tratar a requisição HTTP e capturar erros.
     * Guarda no val userId o ID do user a partir do path do request, lançando um erro caso
     * esse valor seja null, ou caso não seja possível convertê-lo num valor inteiro.
     * Guarda no val user os detalhes do user obtidos através do serviço dos users, passando o ID.
     * Por fim, retorna uma resposta HTTP com o status de sucesso (200 OK) e os detalhes do user caso este exista
     * caso o user nao exista lança um erro userNotFound.
     *
     * @param request A requisição HTTP que contém o ID do user no path.
     * @return Response A resposta HTTP com o status de sucesso e os detalhes do user.
     */
    fun getUserDetails(request: Request): Response =
        handleRequest {
            logRequest(request)
            val userId =
                request.path("userId")?.toInt()
                    ?: throw Errors.invalidBody()
            val user = userServices.getUserDetails(userId)
            if (user != null) {
                responseOk(UserOutput(user.uid, user.name, user.email, user.token))
            } else {
                errorToHttp(Errors.userNotFound(userId.toString()))
            }
        }

    /**
     * Função que vai servir para obter o token e o id de um user.
     *
     * Chama a função auxiliar handleRequest para tratar a requisição HTTP e capturar erros.
     * Guarda no val user os dados de autenticação (nome e password) a partir do body da requisição.
     * Guarda no val result o resultado da chamada ao serviço dos users, que retorna um par contendo o token e o id do user.
     * Guarda no val token o token do user e no val userId o id do user.
     * Caso o token ou o id sejam nulos, lança um erro indicando que o user não foi encontrado.
     * Por fim, retorna uma resposta HTTP com o status de sucesso (200 OK) e o token e id do user.
     *
     * @param request A requisição HTTP que contém os dados de autenticação do user.
     * @return Response A resposta HTTP com o status de sucesso, token e id do user.
     */
    fun getUserTokenAndId(request: Request): Response =
        handleRequest {
            logRequest(request)
            val a = (request.bodyString())
            println(a)
            val user = Json.decodeFromString<UserTokenIdInput>(request.bodyString())
            val result = userServices.getUserTokenAndId(user.name, user.password)
            val token = result?.first
            val userId = result?.second
            if (token != null && userId != null) {
                responseOk(UserTokenIdOutput(token, userId))
            } else {
                errorToHttp(Errors.userNotFound(user.toString()))
            }
        }
}
