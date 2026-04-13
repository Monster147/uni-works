package pt.isel.ls.server.api

import kotlinx.serialization.json.Json
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import pt.isel.ls.server.data.memory.Courts
import pt.isel.ls.server.data.memory.CourtsDataMem
import pt.isel.ls.server.data.memory.resetMemoryData
import pt.isel.ls.server.services.CourtsServices
import pt.isel.ls.server.web.api.CourtsWebApi
import pt.isel.ls.server.web.models.CourtOutput
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CourtsWebApiTest {
    private lateinit var courtsServices: CourtsServices
    private lateinit var courtsWebApi: CourtsWebApi
    private lateinit var app: RoutingHttpHandler

    // Antes de cada teste, limpa a lista de courts para que os testes não interfiram uns com os outros
    @BeforeTest
    fun setup() {
        resetMemoryData()
        Courts.clear()
        courtsServices = CourtsServices(CourtsDataMem())
        courtsWebApi = CourtsWebApi(courtsServices)
        app =
            routes(
                "court" bind POST to courtsWebApi::createCourt,
                "court/{courtId}" bind GET to courtsWebApi::getCourtDetails,
                "court/club/{clubId}" bind GET to courtsWebApi::getAllCourtsFromClub,
            )
    }

    @Test
    fun `test createCourt`() {
        val requestBody =
            """
            {
                "name": "Court de Teste",
                "club": 1
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "court")
                    .body(requestBody),
            )
        assertEquals(Status.CREATED, response.status)
        val court = Json.decodeFromString<CourtOutput>(response.bodyString())
        assertEquals(CourtOutput(Courts.size, "Court de Teste", 1), court)
    }

    @Test
    fun `test createCourt with empty name`() {
        val requestBody =
            """
            {
                "name": "",
                "club": 1
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "court")
                    .body(requestBody),
            )
        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `test createCourt with negative club ID`() {
        val requestBody =
            """
            {
                "name": "Court de Teste",
                "club": -1
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "court")
                    .body(requestBody),
            )
        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `test createCourt with non-existent club ID`() {
        val requestBody =
            """
            {
                "name": "Court de Teste",
                "club": 4
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "court")
                    .body(requestBody),
            )
        assertEquals(Status.NOT_FOUND, response.status)
    }

    @Test
    fun `test createCourt with duplicate name for the same club`() {
        val requestBody =
            """
            {
                "name": "Court de Teste",
                "club": 1
            }
            """.trimIndent()
        app(Request(POST, "court").body(requestBody))
        val response =
            app(
                Request(POST, "court")
                    .body(requestBody),
            )
        assertEquals(Status.CONFLICT, response.status)
    }

    @Test
    fun `test getCourtDetails`() {
        val requestBody =
            """
            {
                "name": "Court de Teste",
                "club": 1
            }
            """.trimIndent()
        app(Request(POST, "court").body(requestBody))
        val requestBody2 =
            """
            {
                "name": "Court de Teste",
                "club": 2
            }
            """.trimIndent()
        app(Request(POST, "court").body(requestBody2))
        val response = app(Request(GET, "court/1"))
        assertEquals(Status.OK, response.status)
        val response2 = app(Request(GET, "court/2"))
        assertEquals(Status.OK, response2.status)
        val court = Json.decodeFromString<CourtOutput>(response.bodyString())
        assertEquals(CourtOutput(Courts.size - 1, "Court de Teste", 1), court)
        val court2 = Json.decodeFromString<CourtOutput>(response2.bodyString())
        assertEquals(CourtOutput(Courts.size, "Court de Teste", 2), court2)
    }

    @Test
    fun `test getCourtDetails with negative club ID`() {
        val response = app(Request(GET, "court/-1"))
        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `test getCourtDetails with non-existent club ID`() {
        val response = app(Request(GET, "court/999"))
        assertEquals(Status.NOT_FOUND, response.status)
    }

    @Test
    fun `test getAllCourtsFromClub`() {
        val requestBody =
            """
            {
                "name": "Court de Teste",
                "club": 1
            }
            """.trimIndent()
        app(Request(POST, "court").body(requestBody))
        val requestBody2 =
            """
            {
                "name": "Court de Teste",
                "club": 2
            }
            """.trimIndent()
        app(Request(POST, "court").body(requestBody2))
        val requestBody3 =
            """
            {
                "name": "Court de Teste2",
                "club": 1
            }
            """.trimIndent()
        app(Request(POST, "court").body(requestBody3))
        val response = app(Request(GET, "court/club/1"))
        assertEquals(Status.OK, response.status)
        val response2 = app(Request(GET, "court/club/2"))
        assertEquals(Status.OK, response2.status)
        val courts = Json.decodeFromString<List<CourtOutput>>(response.bodyString())
        assertEquals(2, courts.size)
        assertEquals(CourtOutput(Courts[0].crid, Courts[0].name, Courts[0].club), courts[0])
        assertEquals(CourtOutput(Courts[2].crid, Courts[2].name, Courts[2].club), courts[1])
        val courts2 = Json.decodeFromString<List<CourtOutput>>(response2.bodyString())
        assertEquals(1, courts2.size)
        assertEquals(CourtOutput(Courts[1].crid, Courts[1].name, Courts[1].club), courts2[0])
    }

    @Test
    fun `test getAllCourtsFromClub with negative club ID`() {
        val response = app(Request(GET, "court/club/-1"))
        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `test getAllCourtsFromClub with non-existent club ID`() {
        val response = app(Request(GET, "court/club/999"))
        assertEquals(Status.NOT_FOUND, response.status)
    }

    @Test
    fun `test getAllCourtsFromClub with limit and skip`() {
        val requestBody =
            """
            {
                "name": "Court de Teste",
                "club": 1
            }
            """.trimIndent()
        app(Request(POST, "court").body(requestBody))
        val requestBody2 =
            """
            {
                "name": "Court de Teste",
                "club": 2
            }
            """.trimIndent()
        app(Request(POST, "court").body(requestBody2))
        val requestBody3 =
            """
            {
                "name": "Court de Teste2",
                "club": 1
            }
            """.trimIndent()
        app(Request(POST, "court").body(requestBody3))
        val response = app(Request(GET, "court/club/1?limit=1&skip=0"))
        assertEquals(Status.OK, response.status)
        val courts = Json.decodeFromString<List<CourtOutput>>(response.bodyString())
        assertEquals(1, courts.size)
        assertEquals(CourtOutput(Courts[0].crid, Courts[0].name, Courts[0].club), courts[0])
    }
}
