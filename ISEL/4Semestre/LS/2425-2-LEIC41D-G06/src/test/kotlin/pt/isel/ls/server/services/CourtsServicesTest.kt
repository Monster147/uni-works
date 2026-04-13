package pt.isel.ls.server.services

import pt.isel.ls.server.common.AppError
import pt.isel.ls.server.data.memory.Clubs
import pt.isel.ls.server.data.memory.Courts
import pt.isel.ls.server.data.memory.CourtsDataMem
import pt.isel.ls.server.domain.Club
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CourtsServicesTest {
    // Testes para a classe courtsDataMem

    private lateinit var courtsServices: CourtsServices

    // Antes de cada teste, limpa a lista de courts para que os testes não interfiram uns com os outros
    @BeforeTest
    fun setup() {
        Clubs.clear()
        Courts.clear()
        courtsServices = CourtsServices(CourtsDataMem())
    }

    @Test
    fun `test createNewCourt`() {
        val club = Club(1, "Clube de Teste")
        Clubs.add(club)
        val courtId = courtsServices.createNewCourt("Test", club.cid)
        assert(courtId >= 0) { "Court ID should be non-negative" }
    }

    @Test
    fun `test createNewCourt with empty name`() {
        assertFailsWith<AppError> { courtsServices.createNewCourt("", 1) }
    }

    @Test
    fun `test createNewCourt with negative club ID`() {
        assertFailsWith<AppError> { courtsServices.createNewCourt("Court de Teste", -1) }
    }

    @Test
    fun `test createNewCourt with non-existent club ID`() {
        assertFailsWith<AppError> { courtsServices.createNewCourt("Court de Teste", 3) }
    }

    @Test
    fun `test createCourt with duplicate name for the same club`() {
        val club = Club(1, "Clube de Teste")
        Clubs.add(club)
        courtsServices.createNewCourt("Test", club.cid)
        assertFailsWith<AppError> { courtsServices.createNewCourt("Test", club.cid) }
    }

    @Test
    fun `test getCourtDetails`() {
        val club = Club(1, "Clube de Teste")
        Clubs.add(club)
        courtsServices.createNewCourt("Test", club.cid)
        val court = courtsServices.getCourtDetails(club.cid)
        assertNotNull(court) { "Court should not be null" }
        assert(court.name == "Test") { "Court name should be 'Test'" }
        assert(court.club == club.cid) { "Club ID should match the generated ID" }
    }

    @Test
    fun `test getCourtDetails with negative club ID`() {
        assertFailsWith<AppError> { courtsServices.getCourtDetails(-1) }
    }

    @Test
    fun `test getCourtDetails with non-existent club ID`() {
        val court = courtsServices.getCourtDetails(1)
        assertNull(court)
    }

    @Test
    fun `test getAllCourtsFromAClub`() {
        val club = Club(1, "Clube de Teste")
        Clubs.add(club)
        courtsServices.createNewCourt("Test 1", club.cid)
        courtsServices.createNewCourt("Test 2", club.cid)
        val courts = courtsServices.getAllCourtsFromAClub(club.cid)
        assert(courts.size == 2) { "There should be 2 courts" }
        assert(courts[0].name == "Test 1") { "First court name should be 'Test 1'" }
        assert(courts[1].name == "Test 2") { "Second court name should be 'Test 2'" }
        assert(courts.all { it.club == club.cid }) { "All courts should have the same club ID" }
    }
}
