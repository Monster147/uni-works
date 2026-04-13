package pt.isel.ls.server.web.api

import kotlinx.serialization.json.Json
import org.http4k.core.Response
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.OK

/*
fun responseCreated(body: Any):Response {
    return Response(CREATED)
        .header("content-type", "application/json")
        .body(Json.encodeToString(body))
}
*/

/**
 * Funções auxiliares para criar respostas HTTP com o corpo serializado em JSON.
 * Essas funções são utilizadas para retornar respostas de sucesso ou erro com o conteúdo apropriado.
 */
inline fun <reified T> responseCreated(body: T): Response =
    Response(CREATED)
        .header("content-type", "application/json")
        .body(Json.encodeToString(body))

inline fun <reified T> responseOk(body: T): Response =
    Response(OK)
        .header("content-type", "application/json")
        .body(Json.encodeToString(body))

inline fun <reified T> responseInternalServerError(body: T): Response =
    Response(INTERNAL_SERVER_ERROR)
        .header("content-type", "application/json")
        .body(Json.encodeToString(Json.encodeToString(body)))
