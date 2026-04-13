package pt.isel.ls.server.api

import kotlinx.serialization.json.Json
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import pt.isel.ls.server.data.memory.ClubDataMem
import pt.isel.ls.server.data.memory.Clubs
import pt.isel.ls.server.data.memory.UserDataMem
import pt.isel.ls.server.data.memory.resetMemoryData
import pt.isel.ls.server.services.ClubServices
import pt.isel.ls.server.services.UserServices
import pt.isel.ls.server.web.api.ClubWebApi
import pt.isel.ls.server.web.models.ClubOutput
import pt.isel.ls.server.web.models.ClubsName
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ClubWebApiTest {
    private lateinit var userServices: UserServices
    private lateinit var clubServices: ClubServices
    private lateinit var clubWebApi: ClubWebApi
    private lateinit var app: RoutingHttpHandler

    // Antes de cada teste, limpa a lista de clubs para que os testes não interfiram uns com os outros
    @BeforeTest
    fun setup() {
        resetMemoryData()
        Clubs.clear()
        userServices = UserServices(UserDataMem())
        clubServices = ClubServices(ClubDataMem())
        clubWebApi = ClubWebApi(clubServices, userServices)
        app =
            routes(
                "club" bind POST to clubWebApi::createClub,
                "club/{clubId}" bind GET to clubWebApi::getClubDetails,
                "club" bind GET to clubWebApi::getAllClubs,
                "club/search/{name}" bind GET to clubWebApi::searchClubByName,
            )
    }

    @Test
    fun `test createClub`() {
        val requestBody =
            """
            {
                "name": "Clube de Teste"
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "club")
                    .header("Authorization", "123-123")
                    .body(requestBody),
            )
        assertEquals(Status.CREATED, response.status)
        val club = Json.decodeFromString<ClubOutput>(response.bodyString())
        assertEquals(ClubOutput(Clubs.size, "Clube de Teste", 1), club)
    }

    @Test
    fun `test createClub with empty name`() {
        val requestBody =
            """
            {
                "name": ""
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "club")
                    .header("Authorization", "123-123")
                    .body(requestBody),
            )
        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `test createClub with invalid authorization token`() {
        val requestBody =
            """
            {
                "name": "Clube de Teste"
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "club")
                    .header("Authorization", "-1")
                    .body(requestBody),
            )
        assertEquals(Status.UNAUTHORIZED, response.status)
    }

    @Test
    fun `test createClub with duplicate name and owner`() {
        val requestBody =
            """
            {
                "name": "Clube de Ténis"
            }
            """.trimIndent()
        app(
            Request(POST, "club")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response =
            app(
                Request(POST, "club")
                    .header("Authorization", "123-123")
                    .body(requestBody),
            )
        assertEquals(Status.CONFLICT, response.status)
    }

    @Test
    fun `test valid getClubDetails`() {
        val requestBody =
            """
            {
                "name": "Clube de Ténis"
            }
            """.trimIndent()
        app(
            Request(POST, "club")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response = app(Request(GET, "club/1"))
        assertEquals(Status.OK, response.status)
        val club = Json.decodeFromString<ClubOutput>(response.bodyString())
        assertEquals(ClubOutput(Clubs.size, "Clube de Ténis", 1), club)
    }

    @Test
    fun `test getClubDetails with negative ID`() {
        val response = app(Request(GET, "club/-1"))
        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `test getClubDetails with non-existent ID`() {
        val response = app(Request(GET, "club/999"))
        assertEquals(Status.NOT_FOUND, response.status)
    }

    @Test
    fun `test getAllClubs`() {
        val requestBody =
            """
            {
                "name": "Clube de Teste"
            }
            """.trimIndent()
        app(
            Request(POST, "club")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val requestBody2 =
            """
            {
                "name": "Clube de Teste2"
            }
            """.trimIndent()
        app(
            Request(POST, "club")
                .header("Authorization", "123-123")
                .body(requestBody2),
        )
        val response = app(Request(GET, "club"))
        assertEquals(Status.OK, response.status)
        val clubs = Json.decodeFromString<List<ClubsName>>(response.bodyString())
        assertEquals(Clubs.size, clubs.size)
        assertEquals(ClubsName(Clubs[0].name, Clubs[0].cid), clubs[0])
        assertEquals(ClubsName(Clubs[1].name, Clubs[1].cid), clubs[1])
    }

    @Test
    fun `test getAllClubs with limit and skip`() {
        val requestBody =
            """
            {
                "name": "Clube de Teste"
            }
            """.trimIndent()
        app(
            Request(POST, "club")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val requestBody2 =
            """
            {
                "name": "Clube de Teste2"
            }
            """.trimIndent()
        app(
            Request(POST, "club")
                .header("Authorization", "123-123")
                .body(requestBody2),
        )
        val response = app(Request(GET, "club?skip=1&limit=1"))
        assertEquals(Status.OK, response.status)
        val clubs = Json.decodeFromString<List<ClubsName>>(response.bodyString())
        assertEquals(1, clubs.size)
        assertEquals(Clubs[1].name, clubs[0].name)
    }

    @Test
    fun `test searchClubByName with partial name`() {
        val requestBody =
            """
            {
                "name": "Clube de Ténis"
            }
            """.trimIndent()
        app(
            Request(POST, "club")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val requestBody2 =
            """
            {
                "name": "Clube de Futebol"
            }
            """.trimIndent()
        app(
            Request(POST, "club")
                .header("Authorization", "123-123")
                .body(requestBody2),
        )

        val response = app(Request(GET, "club/search/Clube"))
        assertEquals(Status.OK, response.status)
        val clubs = Json.decodeFromString<List<ClubsName>>(response.bodyString())
        assertEquals(2, clubs.size)
        assertEquals("Clube de Ténis", clubs[0].name)
        assertEquals("Clube de Futebol", clubs[1].name)
    }

    @Test
    fun `test searchClubByName with the full name`() {
        val requestBody =
            """
            {
                "name": "Clube de Ténis"
            }
            """.trimIndent()
        app(
            Request(POST, "club")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val requestBody2 =
            """
            {
                "name": "Clube de Futebol"
            }
            """.trimIndent()
        app(
            Request(POST, "club")
                .header("Authorization", "123-123")
                .body(requestBody2),
        )

        val response = app(Request(GET, "club/search/ClubedeFutebol"))
        assertEquals(Status.OK, response.status)
        val clubs = Json.decodeFromString<List<ClubsName>>(response.bodyString())
        assertEquals(1, clubs.size)
        assertEquals("Clube de Futebol", clubs[0].name)
    }

    @Test
    fun `test searchClubByName with empty name`() {
        val response = app(Request(GET, "club/search/ "))
        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `test searchClubByName with name that doest exist`() {
        val response = app(Request(GET, "club/search/ALGO "))
        assertEquals(Status.NOT_FOUND, response.status)
    }
}
