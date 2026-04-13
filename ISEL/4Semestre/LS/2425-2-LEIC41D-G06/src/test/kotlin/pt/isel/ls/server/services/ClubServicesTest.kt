package pt.isel.ls.server.services

import pt.isel.ls.server.common.AppError
import pt.isel.ls.server.data.memory.ClubDataMem
import pt.isel.ls.server.data.memory.Clubs
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ClubServicesTest {
    // Testes para a classe clubsDataMem

    private lateinit var clubServices: ClubServices

    // Antes de cada teste, limpa a lista de clubes para que os testes não interfiram uns com os outros
    @BeforeTest
    fun setup() {
        Clubs.clear()
        clubServices = ClubServices(ClubDataMem())
    }

    @Test
    fun `test createClubByName`() {
        val name = "Clube de Teste"
        val owner = 1
        val clubId = clubServices.createClubByName(name, owner)
        assert(clubId >= 0) { "Club ID should be positive" }
    }

    @Test
    fun `test createClubByName with empty name`() {
        assertFailsWith<AppError> { clubServices.createClubByName("", 1) }
    }

    @Test
    fun `test createClubByName with negative owner ID`() {
        assertFailsWith<AppError> { clubServices.createClubByName("", -1) }
    }

    @Test
    fun `test createClubByName with duplicate name and owner`() {
        val name = "Clube de teste"
        val owner = 1
        clubServices.createClubByName(name, owner)
        assertFailsWith<AppError> { clubServices.createClubByName(name, owner) }
    }

    @Test
    fun `test getClubDetails`() {
        val name = "Clube de Teste"
        val owner = 1
        val clubId = clubServices.createClubByName(name, owner)
        val club = clubServices.getClubDetails(clubId)
        assertNotNull(club) { "Club should not be null" }
        assert(club.cid == clubId) { "Club ID should match the generated ID" }
    }

    @Test
    fun `test getClubDetails with negative ID`() {
        assertFailsWith<AppError> { clubServices.getClubDetails(-1) }
    }

    @Test
    fun `test getClubDetails with non-existent ID`() {
        val club = clubServices.getClubDetails(1)
        assertNull(club)
    }

    @Test
    fun `test getClubs`() {
        val name1 = "Clube de Teste 1"
        val owner1 = 1
        val name2 = "Clube de Teste 2"
        val owner2 = 2
        clubServices.createClubByName(name1, owner1)
        clubServices.createClubByName(name2, owner2)
        val clubs = clubServices.getClubs()
        assert(clubs.size == 2) { "Clubs list should have 2 elements" }
    }

    @Test
    fun `search a club by its name`() {
        val name = "Clube de Teste"
        val owner = 1
        clubServices.createClubByName(name, owner)
        val clubs = clubServices.searchClubByName(name)
        assertEquals(1, clubs.size)
        assertEquals(name, clubs[0].name)
    }

    @Test
    fun `search a club by its name with empty name`() {
        assertFailsWith<AppError> { clubServices.searchClubByName("") }
    }

    @Test
    fun `search a club by its partial name`() {
        val name = "Clube de Teste"
        val owner = 1
        clubServices.createClubByName(name, owner)
        val clubs = clubServices.searchClubByName("Clube")
        assertEquals(1, clubs.size)
        assertEquals(name, clubs[0].name)
    }

    @Test
    fun `search multiple clubs by name`() {
        val name1 = "Clube de Teste 1"
        val owner1 = 1
        val name2 = "Clube de Teste 2"
        val owner2 = 2
        clubServices.createClubByName(name1, owner1)
        clubServices.createClubByName(name2, owner2)
        val clubs = clubServices.searchClubByName("Clube")
        assertEquals(2, clubs.size)
        assertEquals(name1, clubs[0].name)
        assertEquals(name2, clubs[1].name)
    }

    @Test
    fun `search a club by its name that does not exist`() {
        val name = "Clube de Teste"
        val owner = 1
        clubServices.createClubByName(name, owner)
        val clubs = clubServices.searchClubByName("Clube de Teste2")
        assertEquals(0, clubs.size)
    }

    @Test
    fun `search a club by its name in mutiple clubs`() {
        val name1 = "Clube de Teste 1"
        val owner1 = 1
        val name2 = "Clube de Teste 2"
        val owner2 = 2
        val name3 = "Clube de Teste 3"
        val owner3 = 3
        clubServices.createClubByName(name1, owner1)
        clubServices.createClubByName(name2, owner2)
        clubServices.createClubByName(name3, owner3)
        val clubs = clubServices.searchClubByName("Clube de Teste 1")
        assertEquals(1, clubs.size)
        assertEquals(name1, clubs[0].name)
    }
}
