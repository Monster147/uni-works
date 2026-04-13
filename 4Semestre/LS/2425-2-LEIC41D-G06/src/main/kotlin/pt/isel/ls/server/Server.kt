package pt.isel.ls.server

import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.routing.ResourceLoader
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.singlePageApp
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.slf4j.LoggerFactory
import pt.isel.ls.server.data.memory.ClubDataMem
import pt.isel.ls.server.data.memory.CourtsDataMem
import pt.isel.ls.server.data.memory.RentalDataMem
import pt.isel.ls.server.data.memory.UserDataMem
import pt.isel.ls.server.data.memory.resetMemoryData
import pt.isel.ls.server.data.postgresSQL.ClubPostgresSQL
import pt.isel.ls.server.data.postgresSQL.CourtsPostgresSQL
import pt.isel.ls.server.data.postgresSQL.RentalPostgresSQL
import pt.isel.ls.server.data.postgresSQL.UserPostgresSQL
import pt.isel.ls.server.services.ClubServices
import pt.isel.ls.server.services.CourtsServices
import pt.isel.ls.server.services.RentalServices
import pt.isel.ls.server.services.UserServices
import pt.isel.ls.server.web.api.ClubWebApi
import pt.isel.ls.server.web.api.CourtsWebApi
import pt.isel.ls.server.web.api.RentalWebApi
import pt.isel.ls.server.web.api.UserWebApi

/**
 * Define a val logger para registar as mensagens do servidor com o nome "HTTPServer".
 */
private val logger = LoggerFactory.getLogger("HTTPServer")

/**
 * Vals para data a ser utilizada dos clubs, users, courts e rentals.
 * Essas vals são definidas através da função auxiliar provideData.
 */
private val clubData = provideData(::ClubDataMem, ::ClubPostgresSQL)
private val usersData = provideData(::UserDataMem, ::UserPostgresSQL)
private val courtsData = provideData(::CourtsDataMem, ::CourtsPostgresSQL)
private val rentalData = provideData(::RentalDataMem, ::RentalPostgresSQL)

/**
 * Services dos clubs, users, courts e rentals.
 * Essas vals são definidas através das classes correspondentes.
 */
private val clubServices = ClubServices(clubData)
private val usersServices = UserServices(usersData)
private val courtServices = CourtsServices(courtsData)
private val rentalServices = RentalServices(rentalData)

/**
 * Web APIs dos clubs, users, courts e rentals.
 * Essas vals são definidas através das classes correspondentes.
 */
val userWebApi = UserWebApi(usersServices)
val courtsWebApi = CourtsWebApi(courtServices)
val rentalWebApi = RentalWebApi(rentalServices, usersServices)
val clubWebApi = ClubWebApi(clubServices, usersServices)

/**
 * Função auxiliar para registar as informações do pedido HTTP recebido.
 * Regista o método, URI, content-type e accept do pedido.
 */
fun logRequest(request: Request) {
    logger.info(
        "incoming request: method={}, uri={}, content-type={} accept={}",
        request.method,
        request.uri,
        request.header("content-type"),
        request.header("accept"),
    )
}

/**
 * Função que inicia o servidor HTTP.
 * Regista as rotas e os respetivos métodos para os users, clubs, courts e rentals.
 * Inicia o servidor Jetty no port 8080 onde ficará ativo até que o aplicativo seja cancelado.
 */

fun main() {
    resetMemoryData()

    val userRoutes =
        routes(
            "/" bind POST to userWebApi::createUser,
            "/login" bind POST to userWebApi::getUserTokenAndId,
            "/{userId}" bind GET to userWebApi::getUserDetails,
        )
    val clubRoutes =
        routes(
            "/" bind POST to clubWebApi::createClub,
            "/{clubId}" bind GET to clubWebApi::getClubDetails,
            "/" bind GET to clubWebApi::getAllClubs,
            "/search/{name}" bind GET to clubWebApi::searchClubByName,
        )
    val courtRoutes =
        routes(
            "/" bind POST to courtsWebApi::createCourt,
            "/{courtId}" bind GET to courtsWebApi::getCourtDetails,
            "/clubs/{clubId}" bind GET to courtsWebApi::getAllCourtsFromClub,
        )
    val rentalRoutes =
        routes(
            "/" bind POST to rentalWebApi::createRental,
            "/{rentalId}" bind GET to rentalWebApi::getRentalDetails,
            "/clubs/{clubId}/courts/{courtId}/date/{date}" bind GET to rentalWebApi::getRentalsByDateClubCourt,
            "/users/{userId}" bind GET to rentalWebApi::getRentalsOfUser,
            "/clubs/{clubId}/courts/{courtId}" bind GET to rentalWebApi::getRentalsByDateClubCourt,
            "/available/clubs/{clubId}/courts/{courtId}/date/{date}" bind GET to rentalWebApi::getRentalsAvailableHours,
            "/{rentalId}" bind DELETE to rentalWebApi::deleteRental,
            "/{rentalId}" bind POST to rentalWebApi::updateRental,
        )
    val app =
        routes(
            "/users" bind userRoutes,
            "/clubs" bind clubRoutes,
            "/courts" bind courtRoutes,
            "/rentals" bind rentalRoutes,
            singlePageApp(ResourceLoader.Directory("static-content")),
        )

    val jettyServer = app.asServer(Jetty(providePort())).start()
    logger.info("server started listening")

    Thread.currentThread().join()
    jettyServer.stop()

    logger.info("leaving Main")
}
