package pt.isel.ls.server.api

import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import pt.isel.ls.server.data.memory.RentalDataMem
import pt.isel.ls.server.data.memory.Rentals
import pt.isel.ls.server.data.memory.UserDataMem
import pt.isel.ls.server.data.memory.resetMemoryData
import pt.isel.ls.server.services.RentalServices
import pt.isel.ls.server.services.UserServices
import pt.isel.ls.server.web.api.RentalWebApi
import pt.isel.ls.server.web.models.RentalOutput
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RentalWebApiTest {
    private lateinit var rentalServices: RentalServices
    private lateinit var rentalWebApi: RentalWebApi
    private lateinit var userServices: UserServices
    private lateinit var app: RoutingHttpHandler

    // Antes de cada teste, limpa a lista de rentals para que os testes não interfiram uns com os outros
    @BeforeTest
    fun setup() {
        resetMemoryData()
        Rentals.clear()
        rentalServices = RentalServices(RentalDataMem())
        userServices = UserServices(UserDataMem())
        rentalWebApi = RentalWebApi(rentalServices, userServices)
        app =
            routes(
                "rental" bind POST to rentalWebApi::createRental,
                "rental/{rentalId}" bind GET to rentalWebApi::getRentalDetails,
                "rental/{clubId}/{courtId}/{date}" bind GET to rentalWebApi::getRentalsByDateClubCourt,
                "rental/available/{clubId}/{courtId}/{date}" bind GET to rentalWebApi::getRentalsAvailableHours,
                "rental/user/{userId}" bind GET to rentalWebApi::getRentalsOfUser,
                "rental/{rentalId}" bind DELETE to rentalWebApi::deleteRental,
                "rental/{rentalId}" bind POST to rentalWebApi::updateRental,
                "rental/{clubId}/{courtId}" bind GET to rentalWebApi::getRentalsByDateClubCourt,
            )
    }

    @Test
    fun `test createRental`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "rental")
                    .header("Authorization", "123-123")
                    .body(requestBody),
            )
        assertEquals(Status.CREATED, response.status)
        val rental = Json.decodeFromString<RentalOutput>(response.bodyString())
        assertEquals(RentalOutput(Rentals.size, LocalDate(2025, 12, 30), 1, 2, 1, 1, 1), rental)
    }

    @Test
    fun `test createRental with a invalid user`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "rental")
                    .header("Authorization", "999-999")
                    .body(requestBody),
            )
        assertEquals(Status.UNAUTHORIZED, response.status)
    }

    @Test
    fun `test createRental with an invalid club`() {
        val requestBodyNegClub =
            """
            {
                "club": -1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        val requestBodyNonClub =
            """
            {
                "club": 999,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "rental")
                    .header("Authorization", "123-123")
                    .body(requestBodyNegClub),
            )
        assertEquals(Status.BAD_REQUEST, response.status)
        val response2 =
            app(
                Request(POST, "rental")
                    .header("Authorization", "123-123")
                    .body(requestBodyNonClub),
            )
        assertEquals(Status.NOT_FOUND, response2.status)
    }

    @Test
    fun `test createRental with an invalid court`() {
        val requestBodyNegCourt =
            """
            {
                "club": 1,
                "court": -1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        val requestBodyNonCourt =
            """
            {
                "club": 1,
                "court": 999,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "rental")
                    .header("Authorization", "123-123")
                    .body(requestBodyNegCourt),
            )
        assertEquals(Status.BAD_REQUEST, response.status)
        val response2 =
            app(
                Request(POST, "rental")
                    .header("Authorization", "123-123")
                    .body(requestBodyNonCourt),
            )
        assertEquals(Status.NOT_FOUND, response2.status)
    }

    @Test
    fun `test createRental with an invalid court in club`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 3,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "rental")
                    .header("Authorization", "123-123")
                    .body(requestBody),
            )
        assertEquals(Status.NOT_FOUND, response.status)
    }

    @Test
    fun `test createRental with a court already rented`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response =
            app(
                Request(POST, "rental")
                    .header("Authorization", "123-123")
                    .body(requestBody),
            )
        assertEquals(Status.CONFLICT, response.status)
    }

    @Test
    fun `test createRental with an invalid Date`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2024-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "rental")
                    .header("Authorization", "123-123")
                    .body(requestBody),
            )
        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `test createRental with an invalid duration`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 2,
                "endDuration": 1
            }
            """.trimIndent()
        val requestBodySameDuration =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 2,
                "endDuration": 2
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "rental")
                    .header("Authorization", "123-123")
                    .body(requestBody),
            )
        assertEquals(Status.BAD_REQUEST, response.status)
        val responseSameDuration =
            app(
                Request(POST, "rental")
                    .header("Authorization", "123-123")
                    .body(requestBodySameDuration),
            )
        assertEquals(Status.BAD_REQUEST, responseSameDuration.status)
    }

    @Test
    fun `test createRental with a rental that already exists`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response =
            app(
                Request(POST, "rental")
                    .header("Authorization", "123-123")
                    .body(requestBody),
            )
        assertEquals(Status.CONFLICT, response.status)
    }

    @Test
    fun `test getRentalDetails`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response = app(Request(GET, "rental/1"))
        assertEquals(Status.OK, response.status)
        val rental = Json.decodeFromString<RentalOutput>(response.bodyString())
        assertEquals(RentalOutput(1, LocalDate(2025, 12, 30), 1, 2, 1, 1, 1), rental)
    }

    @Test
    fun `test getRentalDetails with a negative ID`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response = app(Request(GET, "rental/-1"))
        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `test getRentalDetails with a non-existent ID`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response = app(Request(GET, "rental/999"))
        assertEquals(Status.NOT_FOUND, response.status)
    }

    @Test
    fun `test getRentalsByDateClubCourt`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response = app(Request(GET, "rental/1/1/2025-12-30"))
        assertEquals(Status.OK, response.status)
        val rentals = Json.decodeFromString<List<RentalOutput>>(response.bodyString())
        assertEquals(listOf(RentalOutput(1, LocalDate(2025, 12, 30), 1, 2, 1, 1, 1)), rentals)
    }

    @Test
    fun `test getRentalsByDateClubCourt with a negative club ID`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response = app(Request(GET, "rental/-1/1/2025-12-30"))
        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `test getRentalsByDateClubCourt with a non existent club ID`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response = app(Request(GET, "rental/999/1/2025-12-30"))
        assertEquals(Status.NOT_FOUND, response.status)
    }

    @Test
    fun `test getRentalsByDateClubCourt with a negative court ID`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response = app(Request(GET, "rental/1/-1/2025-12-30"))
        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `test getRentalsByDateClubCourt with a non existent court ID`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response = app(Request(GET, "rental/1/999/2025-12-30"))
        assertEquals(Status.NOT_FOUND, response.status)
    }

    @Test
    fun `test getRentalsByDateClubCourt with a non existent date`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-31",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response = app(Request(GET, "rental/1/1/2025-12-30"))
        assert(response.bodyString().contains("[]"))
    }

    @Test
    fun `test getRentalsByDateClubCourt without a date`() {
        val requestBody1 =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-31",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody1),
        )
        val requestBody2 =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-31",
                "startDuration": 4,
                "endDuration": 6
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody2),
        )
        val response = app(Request(GET, "rental/1/1/"))
        assertEquals(Status.OK, response.status)
        val rentals = Json.decodeFromString<List<RentalOutput>>(response.bodyString())
        val listRentals =
            listOf(
                RentalOutput(
                    1,
                    LocalDate(2025, 12, 31),
                    1,
                    2,
                    1,
                    1,
                    1,
                ),
                RentalOutput(
                    2,
                    LocalDate(2025, 12, 31),
                    4,
                    6,
                    1,
                    1,
                    1,
                ),
            )
        assertEquals(listRentals, rentals)
    }

    @Test
    fun `test getRentalsByDateClubCourt with a limit and skip`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response = app(Request(GET, "rental/1/1/2025-12-30?limit=1&skip=0"))
        assertEquals(Status.OK, response.status)
        val rentals = Json.decodeFromString<List<RentalOutput>>(response.bodyString())
        assertEquals(listOf(RentalOutput(1, LocalDate(2025, 12, 30), 1, 2, 1, 1, 1)), rentals)
    }

    @Test
    fun `test getRentalsAvailableHours`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response = app(Request(GET, "rental/available/1/1/2025-12-30"))
        assertEquals(Status.OK, response.status)
        val rentals = Json.decodeFromString<List<Int>>(response.bodyString())
        assertEquals(listOf(0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23), rentals)
    }

    @Test
    fun `test getRentalsAvailableHours with a negative club ID`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response = app(Request(GET, "rental/available/-1/1/2025-12-30"))
        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `test getRentalsAvailableHours with a non existent club ID`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response = app(Request(GET, "rental/available/999/1/2025-12-30"))
        assertEquals(Status.NOT_FOUND, response.status)
    }

    @Test
    fun `test getRentalsAvailableHours with a negative court ID`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response = app(Request(GET, "rental/available/1/-1/2025-12-30"))
        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `test getRentalsAvailableHours with a non existent court ID`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response = app(Request(GET, "rental/available/1/999/2025-12-30"))
        assertEquals(Status.NOT_FOUND, response.status)
    }

    @Test
    fun `test getRentalsAvailableHours with an invalid date`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response = app(Request(GET, "rental/available/1/1/2024-12-30"))
        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `test getRentalsAvailableHours for a date with no existing reservations`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-31",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response = app(Request(GET, "rental/available/1/1/2025-12-30"))
        assertEquals(Status.OK, response.status)
        val rentals = Json.decodeFromString<List<Int>>(response.bodyString())
        assertEquals(
            listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23),
            rentals,
        )
    }

    @Test
    fun `test getRentalsOfUser`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        val requestBody2 =
            """
            {
                "club": 3,
                "court": 4,
                "date": "2025-12-31",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        val requestBody3 =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 0,
                "endDuration": 1
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody2),
        )
        app(
            Request(POST, "rental")
                .header("Authorization", "456-456")
                .body(requestBody3),
        )
        val response = app(Request(GET, "rental/user/1"))
        assertEquals(Status.OK, response.status)
        val rentals = Json.decodeFromString<List<RentalOutput>>(response.bodyString())
        assertEquals(
            listOf(
                RentalOutput(1, LocalDate(2025, 12, 30), 1, 2, 1, 1, 1),
                RentalOutput(2, LocalDate(2025, 12, 31), 1, 2, 1, 4, 3),
            ),
            rentals,
        )
        val response2 = app(Request(GET, "rental/user/2"))
        assertEquals(Status.OK, response2.status)
        val rentals2 = Json.decodeFromString<List<RentalOutput>>(response2.bodyString())
        assertEquals(listOf(RentalOutput(3, LocalDate(2025, 12, 30), 0, 1, 2, 1, 1)), rentals2)
    }

    @Test
    fun `test getRentalsOfUser with a negative user ID`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response = app(Request(GET, "rental/user/-1"))
        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `test getRentalsOfUser with a non existent user ID`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response = app(Request(GET, "rental/user/999"))
        assertEquals(Status.NOT_FOUND, response.status)
    }

    @Test
    fun `test deleteRental`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        val requestBody2 =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-31",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody2),
        )
        val response = app(Request(DELETE, "rental/1"))
        assertEquals(Status.OK, response.status)
        val response2 = app(Request(GET, "rental/user/1"))
        assertEquals(Status.OK, response2.status)
        val rentals = Json.decodeFromString<List<RentalOutput>>(response2.bodyString())
        assertEquals(listOf(RentalOutput(2, LocalDate(2025, 12, 31), 1, 2, 1, 1, 1)), rentals)
    }

    @Test
    fun `test deleteRental with a negative ID`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response = app(Request(DELETE, "rental/-1"))
        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `test deleteRental with a non existent ID`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val response = app(Request(DELETE, "rental/999"))
        assertEquals(Status.NOT_FOUND, response.status)
    }

    @Test
    fun `test updateRental`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val requestBody2 =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-31",
                "startDuration": 0,
                "endDuration": 2
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "rental/1")
                    .header("Authorization", "123-123")
                    .body(requestBody2),
            )
        assertEquals(Status.OK, response.status)
        val rental = Json.decodeFromString<RentalOutput>(response.bodyString())
        assertEquals(RentalOutput(1, LocalDate(2025, 12, 31), 0, 2, 1, 1, 1), rental)
    }

    @Test
    fun `test updateRental with a negative ID`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val requestBody2 =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-31",
                "startDuration": 0,
                "endDuration": 2
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "rental/-1")
                    .header("Authorization", "123-123")
                    .body(requestBody2),
            )
        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `test updateRental with a non existent ID`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val requestBody2 =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-31",
                "startDuration": 0,
                "endDuration": 2
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "rental/999")
                    .header("Authorization", "123-123")
                    .body(requestBody2),
            )
        assertEquals(Status.NOT_FOUND, response.status)
    }

    @Test
    fun `test updateRental just for date`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val requestBody2 =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-31",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "rental/1")
                    .header("Authorization", "123-123")
                    .body(requestBody2),
            )
        assertEquals(Status.OK, response.status)
        val rental = Json.decodeFromString<RentalOutput>(response.bodyString())
        assertEquals(RentalOutput(1, LocalDate(2025, 12, 31), 1, 2, 1, 1, 1), rental)
    }

    @Test
    fun `test updateRental for duration`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val requestBody2 =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 3,
                "endDuration": 4
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "rental/1")
                    .header("Authorization", "123-123")
                    .body(requestBody2),
            )
        assertEquals(Status.OK, response.status)
        val rental = Json.decodeFromString<RentalOutput>(response.bodyString())
        assertEquals(RentalOutput(1, LocalDate(2025, 12, 30), 3, 4, 1, 1, 1), rental)
    }

    @Test
    fun `test updateRental extended sooner`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val requestBody2 =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 0,
                "endDuration": 2
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "rental/1")
                    .header("Authorization", "123-123")
                    .body(requestBody2),
            )
        assertEquals(Status.OK, response.status)
        val rental = Json.decodeFromString<RentalOutput>(response.bodyString())
        assertEquals(RentalOutput(1, LocalDate(2025, 12, 30), 0, 2, 1, 1, 1), rental)
    }

    @Test
    fun `test updateRental extended later`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val requestBody2 =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 4
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "rental/1")
                    .header("Authorization", "123-123")
                    .body(requestBody2),
            )
        assertEquals(Status.OK, response.status)
        val rental = Json.decodeFromString<RentalOutput>(response.bodyString())
        assertEquals(RentalOutput(1, LocalDate(2025, 12, 30), 1, 4, 1, 1, 1), rental)
    }

    @Test
    fun `test create two rentals, update the first, delete the first one and create again`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody),
        )
        val requestBody2 =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 3,
                "endDuration": 4
            }
            """.trimIndent()
        app(
            Request(POST, "rental")
                .header("Authorization", "123-123")
                .body(requestBody2),
        )
        val response = app(Request(DELETE, "rental/1"))
        assertEquals(Status.OK, response.status)
        val requestBody3 =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        val response2 =
            app(
                Request(POST, "rental")
                    .header("Authorization", "123-123")
                    .body(requestBody3),
            )
        assertEquals(Status.CREATED, response2.status)
    }

    @Test
    fun `test createRental for same date, court and club, for overlapping durations`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "rental")
                    .header("Authorization", "123-123")
                    .body(requestBody),
            )
        assertEquals(Status.CREATED, response.status)
        val rental = Json.decodeFromString<RentalOutput>(response.bodyString())
        assertEquals(RentalOutput(Rentals.size, LocalDate(2025, 12, 30), 1, 2, 1, 1, 1), rental)
        val requestBody2 =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 3
            }
            """.trimIndent()
        val response2 =
            app(
                Request(POST, "rental")
                    .header("Authorization", "123-123")
                    .body(requestBody2),
            )
        assertEquals(Status.CONFLICT, response2.status)
    }

    @Test
    fun `test createRental for same date, court and club, but differents hours`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "rental")
                    .header("Authorization", "123-123")
                    .body(requestBody),
            )
        assertEquals(Status.CREATED, response.status)
        val rental = Json.decodeFromString<RentalOutput>(response.bodyString())
        assertEquals(RentalOutput(Rentals.size, LocalDate(2025, 12, 30), 1, 2, 1, 1, 1), rental)
        val requestBody2 =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 0,
                "endDuration": 1
            }
            """.trimIndent()
        val response2 =
            app(
                Request(POST, "rental")
                    .header("Authorization", "123-123")
                    .body(requestBody2),
            )
        assertEquals(Status.CREATED, response2.status)
        val rental2 = Json.decodeFromString<RentalOutput>(response2.bodyString())
        assertEquals(RentalOutput(Rentals.size, LocalDate(2025, 12, 30), 0, 1, 1, 1, 1), rental2)

        val requestBody3 =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 2,
                "endDuration": 6
            }
            """.trimIndent()
        val response3 =
            app(
                Request(POST, "rental")
                    .header("Authorization", "123-123")
                    .body(requestBody3),
            )
        assertEquals(Status.CREATED, response3.status)
        val rental3 = Json.decodeFromString<RentalOutput>(response3.bodyString())
        assertEquals(RentalOutput(Rentals.size, LocalDate(2025, 12, 30), 2, 6, 1, 1, 1), rental3)
    }

    @Test
    fun `test updateRental for same date, court and club,for overlapping durations`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "rental")
                    .header("Authorization", "123-123")
                    .body(requestBody),
            )
        assertEquals(Status.CREATED, response.status)
        val rental = Json.decodeFromString<RentalOutput>(response.bodyString())
        assertEquals(RentalOutput(Rentals.size, LocalDate(2025, 12, 30), 1, 2, 1, 1, 1), rental)
        val requestBody2 =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 2,
                "endDuration": 6
            }
            """.trimIndent()
        val response2 =
            app(
                Request(POST, "rental")
                    .header("Authorization", "123-123")
                    .body(requestBody2),
            )
        assertEquals(Status.CREATED, response2.status)
        val rental2 = Json.decodeFromString<RentalOutput>(response2.bodyString())
        assertEquals(RentalOutput(Rentals.size, LocalDate(2025, 12, 30), 2, 6, 1, 1, 1), rental2)

        val requestBodyUpdate =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 0,
                "endDuration": 4
            }
            """.trimIndent()
        val responseUpdate =
            app(
                Request(POST, "rental/1")
                    .header("Authorization", "123-123")
                    .body(requestBodyUpdate),
            )
        assertEquals(Status.CONFLICT, responseUpdate.status)
    }

    @Test
    fun `test updateRental for same date, court and club but differents hours`() {
        val requestBody =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 1,
                "endDuration": 2
            }
            """.trimIndent()
        val response =
            app(
                Request(POST, "rental")
                    .header("Authorization", "123-123")
                    .body(requestBody),
            )
        assertEquals(Status.CREATED, response.status)
        val rental = Json.decodeFromString<RentalOutput>(response.bodyString())
        assertEquals(RentalOutput(Rentals.size, LocalDate(2025, 12, 30), 1, 2, 1, 1, 1), rental)
        val requestBody2 =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 2,
                "endDuration": 6
            }
            """.trimIndent()
        val response2 =
            app(
                Request(POST, "rental")
                    .header("Authorization", "123-123")
                    .body(requestBody2),
            )
        assertEquals(Status.CREATED, response2.status)
        val rental2 = Json.decodeFromString<RentalOutput>(response2.bodyString())
        assertEquals(RentalOutput(Rentals.size, LocalDate(2025, 12, 30), 2, 6, 1, 1, 1), rental2)

        val requestBodyUpdate =
            """
            {
                "club": 1,
                "court": 1,
                "date": "2025-12-30",
                "startDuration": 0,
                "endDuration": 2
            }
            """.trimIndent()
        val responseUpdate =
            app(
                Request(POST, "rental/1")
                    .header("Authorization", "123-123")
                    .body(requestBodyUpdate),
            )
        assertEquals(Status.OK, responseUpdate.status)
        val rentalUpdate = Json.decodeFromString<RentalOutput>(responseUpdate.bodyString())
        assertEquals(RentalOutput(1, LocalDate(2025, 12, 30), 0, 2, 1, 1, 1), rentalUpdate)
    }
}
