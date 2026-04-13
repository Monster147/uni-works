package pt.isel.ls.server.api

import kotlinx.serialization.json.Json
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import pt.isel.ls.server.data.memory.UserDataMem
import pt.isel.ls.server.data.memory.Users
import pt.isel.ls.server.data.memory.resetMemoryData
import pt.isel.ls.server.services.UserServices
import pt.isel.ls.server.web.api.UserWebApi
import pt.isel.ls.server.web.models.UserOutput
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UserWebApiTest {
    private lateinit var userServices: UserServices
    private lateinit var userWebApi: UserWebApi
    private lateinit var app: RoutingHttpHandler

    // Antes de cada teste, limpa a lista de users para que os testes não interfiram uns com os outros
    @BeforeTest
    fun setup() {
        resetMemoryData()
        Users.clear()
        userServices = UserServices(UserDataMem())
        userWebApi = UserWebApi(userServices)
        app =
            routes(
                "user" bind POST to userWebApi::createUser,
                "user/{userId}" bind GET to userWebApi::getUserDetails,
            )
    }

    @Test
    fun `test createUser`() {
        val requestBody =
            """
            {
                "name": "John Doe",
                "email": "johnDoe@test.com",
                "password": "testJohnDoe"
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "user")
                    .body(requestBody),
            )
        assertEquals(Status.CREATED, response.status)
        val user = Json.decodeFromString<UserOutput>(response.bodyString())
        assertEquals(UserOutput(Users.size, "John Doe", "johnDoe@test.com", user.token, "testJohnDoe"), user)
    }

    @Test
    fun `test createUser with empty name`() {
        val requestBody =
            """
            {
                "name": "",
                "email": "johnDoe@test.com",
                "password": "testJohnDoe"
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "user")
                    .body(requestBody),
            )
        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `test createUser with empty email`() {
        val requestBody =
            """
            {
                "name": "John Doe",
                "email": "",
                "password": "testJohnDoe"
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "user")
                    .body(requestBody),
            )
        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `test createUser with duplicate email`() {
        val requestBody =
            """
            {
                "name": "Test",
                "email": "test@example.com",
                "password": "testPassword"
            }
            """.trimIndent()
        app(
            Request(POST, "user")
                .body(requestBody),
        )
        val response =
            app(
                Request(POST, "user")
                    .body(requestBody),
            )
        assertEquals(Status.CONFLICT, response.status)
    }

    @Test
    fun `test getUserDetails with invalid ID`() {
        val response = app(Request(GET, "user/-1"))
        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `test getUserDetails with non-existent ID`() {
        val response = app(Request(GET, "user/999"))
        assertEquals(Status.NOT_FOUND, response.status)
    }

    @Test
    fun `test valid getUserDetails`() {
        val requestBody =
            """
            {
                "name": "John Doe",
                "email": "johnDoe@test.com",
                "password": "testJohnDoe"
            }
            """.trimIndent()
        app(
            Request(POST, "user")
                .body(requestBody),
        )
        val response = app(Request(GET, "user/1"))
        val user = Json.decodeFromString<UserOutput>(response.bodyString())
        assertEquals(UserOutput(Users.size, "John Doe", "johnDoe@test.com", user.token, user.password), user)
    }
}
