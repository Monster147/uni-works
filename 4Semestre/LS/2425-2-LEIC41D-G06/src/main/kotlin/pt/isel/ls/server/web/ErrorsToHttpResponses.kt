package pt.isel.ls.server.web

import kotlinx.serialization.json.Json
import org.http4k.core.Response
import org.http4k.core.Status
import pt.isel.ls.server.common.AppError
import pt.isel.ls.server.common.ErrorCode
import pt.isel.ls.server.web.models.HttpErrorResponse

/**
 * Mapeia códigos de erro para status codes HTTP.
 * Este mapa é utilizado para converter erros específicos da aplicação para as respetivas
 * respostas HTTP apropriadas.
 */

private val errorCodeToStatusMap =
    mapOf(
        ErrorCode.MISSING_PARAMETER to Status.BAD_REQUEST,
        ErrorCode.INVALID_PARAMETER to Status.BAD_REQUEST,
        ErrorCode.INVALID_BODY to Status.BAD_REQUEST,
        ErrorCode.CLUB_NOT_FOUND to Status.NOT_FOUND,
        ErrorCode.COURT_NOT_FOUND to Status.NOT_FOUND,
        ErrorCode.ID_NOT_FOUND to Status.NOT_FOUND,
        ErrorCode.RENTAL_NOT_FOUND to Status.NOT_FOUND,
        ErrorCode.USER_NOT_FOUND to Status.NOT_FOUND,
        ErrorCode.MISSING_TOKEN to Status.UNAUTHORIZED,
        ErrorCode.DUPLICATE_CLUB to Status.CONFLICT,
        ErrorCode.DUPLICATE_COURT to Status.CONFLICT,
        ErrorCode.DUPLICATE_USER to Status.CONFLICT,
        ErrorCode.DUPLICATE_RENTAL to Status.CONFLICT,
        ErrorCode.CLUB_NOT_IN_COURT to Status.CONFLICT,
        ErrorCode.COURT_ALREADY_RENTED to Status.CONFLICT,
        ErrorCode.SERVER_ERROR to Status.INTERNAL_SERVER_ERROR,
        ErrorCode.CLUB_NAME_NOT_FOUND to Status.NOT_FOUND,
        ErrorCode.NAME_NOT_FOUND to Status.NOT_FOUND,
        ErrorCode.PASSWORD_NOT_FOUND to Status.NOT_FOUND,
    )

/**
 * Função que converte um erro da aplicação numa resposta HTTP.
 *
 * Guarda no val status o status code HTTP apropriado para o erro,
 * utilizando o mapa `errorCodeToStatusMap` para mapear o código de erro.
 * Guarda no val errorResponse uma instância de `HttpErrorResponse` com o código de erro e detalhes.
 * Por fim, retorna uma resposta HTTP com o status code apropriado e o corpo contendo os detalhes do erro em formato JSON.
 *
 * @param error O erro da aplicação a ser convertido.
 * @return Response A resposta HTTP correspondente ao erro, com o status code apropriado e detalhes do erro.
 */
fun errorToHttp(error: AppError): Response {
    val status = errorCodeToStatusMap[error.code] ?: Status.INTERNAL_SERVER_ERROR
    val errorResponse = HttpErrorResponse(error.code.code, error.details)

    return Response(status)
        .header("content-type", "application/json")
        .body(Json.encodeToString(errorResponse))
}
