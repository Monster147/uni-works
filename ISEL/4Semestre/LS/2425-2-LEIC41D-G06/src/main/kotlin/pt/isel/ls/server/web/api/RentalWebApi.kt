package pt.isel.ls.server.web.api

import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.routing.path
import pt.isel.ls.server.common.Errors
import pt.isel.ls.server.logRequest
import pt.isel.ls.server.services.RentalServices
import pt.isel.ls.server.services.UserServices
import pt.isel.ls.server.web.errorToHttp
import pt.isel.ls.server.web.models.RentalInput
import pt.isel.ls.server.web.models.RentalOutput

/**
 * Classe que define a API web para os Rentals.
 * Contém métodos para criar um rental, obter detalhes de um rental, obter rentals por data/club/court,
 * obter os horários disponíveis, obter os rentals de um user, eliminar e atualizar rentals.
 *
 * @property rentalServices Serviço para manipulação dos rentals.
 * @property userServices Serviço para manipulação dos users.
 */

class RentalWebApi(
    private val rentalServices: RentalServices,
    private val userServices: UserServices,
) {
    /**
     * Função que serve para criar um rental.
     *
     * Chama a função auxiliar handleRequest para tratar a requisição HTTP e capturar erros.
     * Guarda no val rental os dados do novo rental(rid, date, startDuration, endDuration, user, court e club)
     * a partir do body da requisição.
     * Guarda no val owner o ID do utilizador que está a criar o rental, obtido através do header "authorization".
     * Caso o owner seja null, lança um erro indicando que o token está em falta.
     * Guarda no val id o id do novo rental criado através do serviço dos rentals.
     * Por fim, retorna uma resposta HTTP com o status de criação (201 Created) e os detalhes do novo rental criado.
     *
     *
     * @param request A requisição HTTP com os dados do novo rental (rid, date, startDuration, endDuration, user, court e club).
     * @return Response A resposta HTTP com o status de criação e os detalhes do novo rental.
     */
    fun createRental(request: Request): Response =
        handleRequest {
            logRequest(request)
            val rental = Json.decodeFromString<RentalInput>(request.bodyString())
            val owner = request.header("authorization")?.let { userServices.getUserIdByToken(it) }
            if (owner == null) throw Errors.missingToken()
            val id =
                rentalServices.createNewRental(
                    rental.date,
                    rental.startDuration,
                    rental.endDuration,
                    rental.club,
                    rental.court,
                    owner,
                )
            responseCreated(
                RentalOutput(
                    id,
                    rental.date,
                    rental.startDuration,
                    rental.endDuration,
                    owner,
                    rental.court,
                    rental.club,
                ),
            )
        }

    /**
     * Função que serve para obter os detalhes de um rental específico.
     *
     * Chama a função auxiliar handleRequest para tratar a requisição HTTP e capturar erros.
     * Guarda no val rentalId o ID do rental a partir do path do request, lança um erro caso esse valor
     * seja null, ou não seja possível convertê-lo para um inteiro.
     * Guarda no val rental os detalhes do rental obtidos através do serviço dos rentals, passando o ID.
     * Caso o rental seja null, lança um erro indicando que o rental não foi encontrado.
     * Por fim, retorna uma resposta HTTP com o status de sucesso (200 OK) e os detalhes do rental.
     *
     * @param request A requisição HTTP que contém o ID do rental no path.
     * @return Response A resposta HTTP com o status de sucesso e os detalhes do rental.
     */

    fun getRentalDetails(request: Request): Response =
        handleRequest {
            logRequest(request)
            val rentalId =
                request.path("rentalId")?.toInt()
                    ?: throw Errors.invalidBody()
            val rental = rentalServices.getRentalDetails(rentalId)
            if (rental != null) {
                responseOk(
                    RentalOutput(
                        rental.rid,
                        rental.date,
                        rental.startDuration,
                        rental.endDuration,
                        rental.user,
                        rental.court,
                        rental.club,
                    ),
                )
            } else {
                errorToHttp(Errors.rentalNotFound(rentalId))
            }
        }

    /**
     * Função que serve para obter os rentals de um determinado club e court numa data específica.
     *
     * Chama a função auxiliar handleRequest para tratar a requisição HTTP e capturar erros.
     * Guarda no val clubId o ID do club a partir do path do request, lança um erro caso esse valor
     * seja null, ou não seja possível convertê-lo para um inteiro.
     * Guarda no val courtId o ID do court a partir do path do request, lança um erro caso esse valor
     * seja null, ou não seja possível convertê-lo para um inteiro.
     * Guarda no val dateStr a data a partir do path do request, caso não exista, assume uma string vazia.
     * Guarda no val date a data convertida de string para LocalDate, caso a string esteja vazia, assume null.
     * Guarda no val skip o número de rentals a serem saltados, obtido a partir do query "skip" do request,
     * caso não exista, assume o valor de 0.
     * Guarda no val limit o número máximo de rentals a serem retornados, obtido a partir do query "limit" do request.
     * Guarda no val rentals a lista de rentals obtidos através do serviço dos rentals, removendo
     * os primeiros "skip" rentals e limitando o número de rentals agarrando os primeiros "limit".
     * Por fim, retorna uma resposta HTTP com o status de sucesso (200 OK) e a lista de rentals.
     *
     * @param request A requisição HTTP que contém o ID do club, court e a data no path.
     * @return Response A resposta HTTP com o status de sucesso e a lista de rentals.
     */
    fun getRentalsByDateClubCourt(request: Request): Response =
        handleRequest {
            logRequest(request)
            val clubId =
                request.path("clubId")?.toInt()
                    ?: throw Errors.invalidBody()
            val courtId =
                request.path("courtId")?.toInt()
                    ?: throw Errors.invalidBody()
            val dateStr = request.path("date") ?: ""
            val date = if (dateStr.isEmpty()) null else LocalDate.parse(dateStr)
            val skip = request.query("skip")?.toIntOrNull() ?: 0
            val limit = request.query("limit")?.toIntOrNull()

            val rentals =
                if (limit != null) {
                    rentalServices.getRentals(clubId, courtId, date).drop(skip).take(limit)
                } else {
                    rentalServices.getRentals(clubId, courtId, date).drop(skip)
                }
            responseOk(
                rentals.map {
                    RentalOutput(
                        it.rid,
                        it.date,
                        it.startDuration,
                        it.endDuration,
                        it.user,
                        it.court,
                        it.club,
                    )
                },
            )
        }

    /**
     * Função que serve para obter os horários disponíveis para um court específico de um club numa data específica.
     *
     * Chama a função auxiliar handleRequest para tratar a requisição HTTP e capturar erros.
     * Guarda no val clubId o ID do club a partir do path do request, lança um erro caso esse valor
     * seja null, ou não seja possível convertê-lo para um inteiro.
     * Guarda no val courtId o ID do court a partir do path do request, lança um erro caso esse valor
     * seja null, ou não seja possível convertê-lo para um inteiro.
     * Guarda no val dateStr a data a partir do path do request, caso não exista, assume uma string vazia.
     * Guarda no val date a data convertida de string para LocalDate, caso a string esteja vazia, assume null.
     * Guarda no val rentals os horários disponíveis obtidos através do serviço dos rentals.
     * Por fim, retorna uma resposta HTTP com o status de sucesso (200 OK) e os horários disponíveis.
     *
     * @param request A requisição HTTP que contém o ID do club, court e a data no path.
     * @return Response A resposta HTTP com o status de sucesso e os horários disponíveis.
     */

    fun getRentalsAvailableHours(request: Request): Response =
        handleRequest {
            logRequest(request)
            val clubId =
                request.path("clubId")?.toInt()
                    ?: throw Errors.invalidBody()
            val courtId =
                request.path("courtId")?.toInt()
                    ?: throw Errors.invalidBody()
            val date =
                LocalDate.parse(
                    request.path("date")
                        ?: throw Errors.invalidBody(),
                )

            val rentals = rentalServices.getRentalsAvailableHours(clubId, courtId, date)

            responseOk(rentals)
        }

    /**
     * Função que serve para obter os rentals de um determinado user.
     *
     * Chama a função auxiliar handleRequest para tratar a requisição HTTP e capturar erros.
     * Guarda no val userId o ID do user a partir do path do request, lança um erro caso esse valor
     * seja null, ou não seja possível convertê-lo para um inteiro.
     * Guarda no val skip o número de rentals a serem saltados, obtido a partir do query "skip" do request,
     * caso não exista, assume o valor de 0.
     * Guarda no val limit o número máximo de rentals a serem retornados, obtido
     * a partir do query "limit" do request.
     * Guarda no val rentals a lista de rentals obtidos através do serviço dos rentals, removendo
     * os primeiros "skip" rentals e limitando o número de rentals agarrando os primeiros "limit".
     * Por fim, retorna uma resposta HTTP com o status de sucesso (200 OK) e a lista de rentals.
     *
     * @param request A requisição HTTP que contém o ID do user no path.
     * @return Response A resposta HTTP com o status de sucesso e a lista de rentals do user.
     */
    fun getRentalsOfUser(request: Request): Response =
        handleRequest {
            logRequest(request)
            val userId =
                request.path("userId")?.toInt()
                    ?: throw Errors.invalidBody()
            val skip = request.query("skip")?.toIntOrNull() ?: 0
            val limit = request.query("limit")?.toIntOrNull()

            val rentals =
                if (limit != null) {
                    rentalServices.getRentalsOfUser(userId).drop(skip).take(limit)
                } else {
                    rentalServices.getRentalsOfUser(userId).drop(skip)
                }

            responseOk(
                rentals.map {
                    RentalOutput(
                        it.rid,
                        it.date,
                        it.startDuration,
                        it.endDuration,
                        it.user,
                        it.court,
                        it.club,
                    )
                },
            )
        }

    /**
     * Função que serve para apagar um rental específico.
     *
     * Chama a função auxiliar handleRequest para tratar a requisição HTTP e capturar erros.
     * Guarda no val rentalId o ID do rental a partir do path do request, lança um erro caso esse valor
     * seja null, ou não seja possível convertê-lo para um inteiro.
     * Guarda no val deleted o resultado da chamada ao serviço dos rentals para apagar o rental.
     * Caso o rental tenha sido apagado com sucesso, retorna uma resposta HTTP com o status de sucesso (200 OK)
     * e uma mensagem de sucesso.
     * Caso o rental não tenha sido encontrado, lança um erro indicando que o rental não foi encontrado.
     *
     * @param request A requisição HTTP que contém o ID do rental no path.
     * @return Response A resposta HTTP com o status de sucesso e uma mensagem de confirmação da eliminação do rental.
     */
    fun deleteRental(request: Request): Response =
        handleRequest {
            logRequest(request)
            val rentalId =
                request.path("rentalId")?.toInt()
                    ?: throw Errors.invalidBody()
            val deleted = rentalServices.deleteRental(rentalId)
            if (deleted) {
                responseOk(mapOf("message" to "Rental deleted successfully"))
            } else {
                errorToHttp(Errors.rentalNotFound(rentalId))
            }
        }

    /**
     * Função que serve para atualizar um rental específico.
     *
     * Chama a função auxiliar handleRequest para tratar a requisição HTTP e capturar erros.
     * Guarda no val rentalId o ID do rental a partir do path do request, lança um erro caso esse valor
     * seja null, ou não seja possível convertê-lo para um inteiro.
     * Guarda no val owner o ID do utilizador que está a atualizar o rental, obtido através do header "authorization",
     * caso não exista, lança um erro indicando que o token está em falta.
     * Guarda no val rentalInput os dados do rental(rid, date, startDuration, endDuration, user, court e club)
     * a partir do body da requisição.
     * Guarda no val updatedRental o rental atualizado obtido através do serviço dos rentals.
     * Por fim, retorna uma resposta HTTP com o status de sucesso (200 OK) e os detalhes do rental atualizado.
     *
     * @param request A requisição HTTP com os dados do rental (rid, date, startDuration, endDuration, user, court e club) a ser atualizado.
     * @return Response A resposta HTTP com o status de sucesso e os detalhes do rental atualizado.
     */
    fun updateRental(request: Request): Response =
        handleRequest {
            logRequest(request)
            val rentalId =
                request.path("rentalId")?.toInt()
                    ?: throw Errors.invalidBody()
            val owner =
                request.header("authorization")?.let { userServices.getUserIdByToken(it) }
                    ?: throw Errors.missingToken()
            val rentalInput = Json.decodeFromString<RentalInput>(request.bodyString())
            val updatedRental =
                rentalServices.updateRental(
                    rentalId,
                    rentalInput.date,
                    rentalInput.startDuration,
                    rentalInput.endDuration,
                    rentalInput.club,
                    rentalInput.court,
                    owner,
                )
            responseOk(
                RentalOutput(
                    updatedRental.rid,
                    updatedRental.date,
                    updatedRental.startDuration,
                    updatedRental.endDuration,
                    updatedRental.user,
                    updatedRental.court,
                    updatedRental.club,
                ),
            )
        }
}
