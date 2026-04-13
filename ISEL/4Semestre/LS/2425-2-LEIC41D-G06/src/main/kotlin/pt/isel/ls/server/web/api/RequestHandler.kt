package pt.isel.ls.server.web.api

import org.http4k.core.Response
import pt.isel.ls.server.common.AppError
import pt.isel.ls.server.web.errorToHttp
import pt.isel.ls.server.web.models.HttpErrorResponse

/**
 * Função que trata as requisições HTTP, capturando erros específicos da aplicação
 * e retornando respostas apropriadas.
 */
fun handleRequest(block: () -> Response): Response =
    try {
        block()
    } catch (ex: AppError) {
        errorToHttp(ex)
    } catch (ex: Exception) {
        responseInternalServerError(HttpErrorResponse(500, "An unexpected error occurred"))
    }
