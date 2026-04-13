package pt.isel.ls.server.services

import kotlinx.datetime.LocalDate
import pt.isel.ls.server.common.AppError
import pt.isel.ls.server.data.memory.Clubs
import pt.isel.ls.server.data.memory.Courts
import pt.isel.ls.server.data.memory.RentalDataMem
import pt.isel.ls.server.data.memory.Rentals
import pt.isel.ls.server.data.memory.Users
import pt.isel.ls.server.domain.Club
import pt.isel.ls.server.domain.Court
import pt.isel.ls.server.domain.Rental
import pt.isel.ls.server.domain.User
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RentalServicesTest {
    private lateinit var rentalServices: RentalServices

    // Antes de cada teste, limpa todas as listas para que os testes não interfiram uns com os outros
    @BeforeTest
    fun setup() {
        Rentals.clear()
        Courts.clear()
        Clubs.clear()
        Users.clear()
        rentalServices = RentalServices(RentalDataMem())
    }

    @Test
    fun `test createNewRental and then test getDetails`() {
        val date = LocalDate(2025, 12, 31)
        val startDuration = 0
        val endDuration = 1
        val user = User(1, "User de Teste", "test@gmail.com")
        Users.add(user)
        val club = Club(1, "Clube de Teste")
        Clubs.add(club)
        val court = Court(1, "Court de Teste", club.cid)
        Courts.add(court)

        val rid = rentalServices.createNewRental(date, startDuration, endDuration, club.cid, court.crid, user.uid)
        assertEquals(1, rid)

        val rental = rentalServices.getRentalDetails(rid)
        assertNotNull(rental)
        assertEquals(date, rental.date)
        assertEquals(startDuration, rental.startDuration)
        assertEquals(endDuration, rental.endDuration)
        assertEquals(user.uid, rental.user)
        assertEquals(club.cid, rental.club)
        assertEquals(court.crid, rental.court)
    }

    @Test
    fun `test createNewRental with an invalid user`() {
        val date = LocalDate(2025, 12, 31)
        val startDuration = 0
        val endDuration = 1
        val user = User(3, "John", "doe@gmail.com")
        val club = Club(1, "Clube de Teste")
        Clubs.add(club)
        val court = Court(1, "Court de Teste", club.cid)
        Courts.add(court)
        assertFailsWith<AppError> {
            rentalServices.createNewRental(date, startDuration, endDuration, club.cid, court.crid, user.uid)
        }
    }

    @Test
    fun `test createNewRental with an invalid club`() {
        val date = LocalDate(2025, 12, 31)
        val startDuration = 0
        val endDuration = 1
        val user = User(1, "John", "doe@gmail.com")
        Users.add(user)
        val club = Club(4, "Clube de Teste")
        val court = Court(1, "Court de Teste", club.cid)
        Courts.add(court)
        assertFailsWith<AppError> {
            rentalServices.createNewRental(date, startDuration, endDuration, club.cid, court.crid, user.uid)
        }
    }

    @Test
    fun `test createNewRental with an invalid court`() {
        val date = LocalDate(2025, 12, 31)
        val startDuration = 0
        val endDuration = 1
        val user = User(1, "John", "doe@gmail.com")
        Users.add(user)
        val club = Club(1, "Clube de Teste")
        Clubs.add(club)
        val court = Court(3, "Court de Teste", club.cid)
        assertFailsWith<AppError> {
            rentalServices.createNewRental(date, startDuration, endDuration, club.cid, court.crid, user.uid)
        }
    }

    @Test
    fun `test createNewRental with an invalid court in club`() {
        val date = LocalDate(2025, 12, 31)
        val startDuration = 0
        val endDuration = 1
        val user = User(1, "John", "doe@gmail.com")
        Users.add(user)
        val club = Club(1, "Clube de Teste")
        Clubs.add(club)
        val court = Court(3, "Court de Teste", 3)
        Courts.add(court)
        assertFailsWith<AppError> {
            rentalServices.createNewRental(date, startDuration, endDuration, club.cid, court.crid, user.uid)
        }
    }

    @Test
    fun `test createNewRental with a court already rented`() {
        val date = LocalDate(2025, 12, 31)
        val startDuration = 0
        val endDuration = 1
        val user = User(1, "John", "doe@gmail.com")
        Users.add(user)
        val club = Club(1, "Clube de Teste")
        Clubs.add(club)
        val court = Court(1, "Court de Teste", club.cid)
        Courts.add(court)
        val rental = Rental(1, date, startDuration, endDuration, 1, court.crid, club.cid)
        Rentals.add(rental)
        assertFailsWith<AppError> {
            rentalServices.createNewRental(date, startDuration, endDuration, club.cid, court.crid, user.uid)
        }
    }

    @Test
    fun `test createNewRental with an invalid Date`() {
        val pastDate = LocalDate(2023, 3, 17)
        assertFailsWith<AppError> {
            rentalServices.createNewRental(pastDate, 0, 1, 1, 1, 1)
        }
    }

    @Test
    fun `test createNewRental with an invalid duration`() {
        val date = LocalDate(2025, 12, 31)
        assertFailsWith<AppError> {
            rentalServices.createNewRental(date, 1, 0, 1, 1, 1)
        }
    }

    @Test
    fun `test createNewRental with duplicate club, court and name`() {
        val date = LocalDate(2025, 12, 31)
        val startDuration = 0
        val endDuration = 1
        val user = User(1, "User de Teste", "test@gmail.com")
        Users.add(user)
        val club = Club(1, "Clube de Teste")
        Clubs.add(club)
        val court = Court(1, "Court de Teste", club.cid)
        Courts.add(court)

        rentalServices.createNewRental(date, startDuration, endDuration, club.cid, court.crid, user.uid)
        assertFailsWith<AppError> {
            rentalServices.createNewRental(
                date,
                startDuration,
                endDuration,
                club.cid,
                court.crid,
                user.uid,
            )
        }
    }

    @Test
    fun `test getRentals`() {
        val date = LocalDate(2025, 12, 31)
        val user1 = User(1, "User de Teste 1", "test1@gmail.com")
        Users.add(user1)
        val user2 = User(2, "User de Teste 2", "test2@gmail.com")
        Users.add(user2)
        val club = Club(1, "Clube de Teste")
        Clubs.add(club)
        val court = Court(1, "Court de Teste 1", club.cid)
        Courts.add(court)
        rentalServices.createNewRental(date, 0, 2, club.cid, court.crid, user1.uid)
        rentalServices.createNewRental(date, 4, 6, club.cid, court.crid, user2.uid)

        val rentals = rentalServices.getRentals(1, 1, date)
        assertEquals(2, rentals.size)
    }

    @Test
    fun `test getRentals with invalid club`() {
        val date = LocalDate(2025, 12, 31)
        val club = Club(5, "Clube de Teste")
        val court = Court(1, "Court de Teste", club.cid)
        Courts.add(court)
        assertFailsWith<AppError> { rentalServices.getRentals(club.cid, court.crid, date) }
    }

    @Test
    fun testGetRentalsWithInvalidCourt() {
        val date = LocalDate(2025, 12, 31)
        val club = Club(1, "Clube de Teste")
        val court = Court(3, "Court de Teste", club.cid)
        Clubs.add(club)
        assertFailsWith<AppError> { rentalServices.getRentals(club.cid, court.crid, date) }
    }

    @Test
    fun `test getRentalDetails with a negative ID`() {
        assertFailsWith<AppError> { rentalServices.getRentalDetails(-1) }
    }

    @Test
    fun `test getRentalDetails with a non existent ID`() {
        val rental = rentalServices.getRentalDetails(4)
        assertNull(rental)
    }

    @Test
    fun `test getRentalsOfUser`() {
        val date = LocalDate(2025, 12, 31)
        val user = User(1, "User de Teste", "test@gmail.com")
        Users.add(user)
        val club = Club(1, "Clube de Teste")
        Clubs.add(club)
        val court1 = Court(1, "Court de Teste 1", club.cid)
        Courts.add(court1)
        val court2 = Court(2, "Court de Teste 2", club.cid)
        Courts.add(court2)
        rentalServices.createNewRental(date, 0, 1, club.cid, court1.crid, user.uid)
        rentalServices.createNewRental(date, 2, 3, club.cid, court2.crid, user.uid)

        val rentals = rentalServices.getRentalsOfUser(1)
        assertEquals(2, rentals.size)
    }

    @Test
    fun `test getRentalsAvailableHours`() {
        val date = LocalDate(2025, 12, 31)
        val user1 = User(1, "User de Teste 1", "test1@gmail.com")
        Users.add(user1)
        val user2 = User(2, "User de Teste 2", "test2@gmail.com")
        Users.add(user2)
        val club = Club(1, "Clube de Teste")
        Clubs.add(club)
        val court = Court(1, "Court de Teste 1", club.cid)
        Courts.add(court)
        rentalServices.createNewRental(date, 0, 2, club.cid, court.crid, user1.uid)
        rentalServices.createNewRental(date, 4, 6, club.cid, court.crid, user2.uid)

        val availableHours = rentalServices.getRentalsAvailableHours(1, 1, date)
        assertEquals(20, availableHours.size)
        assertTrue(2 in availableHours)
        assertTrue(3 in availableHours)
        assertFalse(0 in availableHours)
        assertFalse(1 in availableHours)
        assertFalse(4 in availableHours)
        assertFalse(5 in availableHours)
    }

    @Test
    fun `test deleteRental`() {
        val date = LocalDate(2025, 12, 31)
        val user = User(1, "User de Teste 1", "test1@gmail.com")
        Users.add(user)
        val club = Club(1, "Clube de Teste")
        Clubs.add(club)
        val court = Court(1, "Court de Teste 1", club.cid)
        Courts.add(court)
        val rid = rentalServices.createNewRental(date, 0, 1, club.cid, court.crid, user.uid)
        val remove = rentalServices.deleteRental(rid)
        assertTrue(remove)
        assertEquals(0, Rentals.size)
    }

    @Test
    fun `test updateRental`() {
        val date = LocalDate(2025, 12, 31)
        val updatedDate = LocalDate(2026, 1, 1)
        val startDuration = 0
        val endDuration = 1
        val updateStartDuration = 2
        val updateEndDuration = 3
        val user = User(1, "User de Teste", "test@gmail.com")
        Users.add(user)
        val club = Club(1, "Clube de Teste")
        Clubs.add(club)
        val court = Court(1, "Court de Teste", club.cid)
        Courts.add(court)
        val rid = rentalServices.createNewRental(date, startDuration, endDuration, club.cid, court.crid, user.uid)
        val updatedRid =
            rentalServices.updateRental(
                rid,
                updatedDate,
                updateStartDuration,
                updateEndDuration,
                club.cid,
                court.crid,
                user.uid,
            )
        assertEquals(rid, updatedRid.rid)
        val updatedRental = rentalServices.getRentalDetails(rid)
        assertNotNull(updatedRental)
        assertEquals(updatedDate, updatedRental.date)
        assertEquals(updateStartDuration, updatedRental.startDuration)
        assertEquals(updateEndDuration, updatedRental.endDuration)
    }

    @Test
    fun `test updateRental just for date`() {
        val date = LocalDate(2025, 12, 31)
        val updatedDate = LocalDate(2026, 1, 1)
        val startDuration = 0
        val endDuration = 1
        val user = User(1, "User de Teste", "test@gmail.com")
        Users.add(user)
        val club = Club(1, "Clube de Teste")
        Clubs.add(club)
        val court = Court(1, "Court de Teste", club.cid)
        Courts.add(court)
        val rid = rentalServices.createNewRental(date, startDuration, endDuration, club.cid, court.crid, user.uid)
        val updatedRid =
            rentalServices.updateRental(rid, updatedDate, startDuration, endDuration, club.cid, court.crid, user.uid)
        assertEquals(rid, updatedRid.rid)
        val updatedRental = rentalServices.getRentalDetails(rid)
        assertNotNull(updatedRental)
        assertEquals(updatedDate, updatedRental.date)
    }

    @Test
    fun `test updateRental for duration`() {
        val date = LocalDate(2025, 12, 31)
        val startDuration = 0
        val endDuration = 1
        val updateStartDuration = 2
        val updateEndDuration = 3
        val user = User(1, "User de Teste", "test@gmail.com")
        Users.add(user)
        val club = Club(1, "Clube de Teste")
        Clubs.add(club)
        val court = Court(1, "Court de Teste", club.cid)
        Courts.add(court)
        val rid = rentalServices.createNewRental(date, startDuration, endDuration, club.cid, court.crid, user.uid)
        val updatedRid =
            rentalServices.updateRental(
                rid,
                date,
                updateStartDuration,
                updateEndDuration,
                club.cid,
                court.crid,
                user.uid,
            )
        assertEquals(rid, updatedRid.rid)
        val updatedRental = rentalServices.getRentalDetails(rid)
        assertNotNull(updatedRental)
        assertEquals(updateStartDuration, updatedRental.startDuration)
        assertEquals(updateEndDuration, updatedRental.endDuration)
    }

    @Test
    fun `test updateRental extend sooner`() {
        val date = LocalDate(2025, 12, 31)
        val startDuration = 1
        val endDuration = 2
        val updateStartDuration = 0
        val user = User(1, "User de Teste", "test@gmail.com")
        Users.add(user)
        val club = Club(1, "Clube de Teste")
        Clubs.add(club)
        val court = Court(1, "Court de Teste", club.cid)
        Courts.add(court)
        val rid = rentalServices.createNewRental(date, startDuration, endDuration, club.cid, court.crid, user.uid)
        val updatedRid =
            rentalServices.updateRental(rid, date, updateStartDuration, endDuration, club.cid, court.crid, user.uid)
        assertEquals(rid, updatedRid.rid)
        val updatedRental = rentalServices.getRentalDetails(rid)
        assertNotNull(updatedRental)
        assertEquals(updateStartDuration, updatedRental.startDuration)
    }

    @Test
    fun `test updateRental duration later`() {
        val date = LocalDate(2025, 12, 31)
        val startDuration = 0
        val endDuration = 1
        val updateEndDuration = 3
        val user = User(1, "User de Teste", "test@gmail.com")
        Users.add(user)
        val club = Club(1, "Clube de Teste")
        Clubs.add(club)
        val court = Court(1, "Court de Teste", club.cid)
        Courts.add(court)
        val rid = rentalServices.createNewRental(date, startDuration, endDuration, club.cid, court.crid, user.uid)
        val updatedRid =
            rentalServices.updateRental(rid, date, startDuration, updateEndDuration, club.cid, court.crid, user.uid)
        assertEquals(rid, updatedRid.rid)
        val updatedRental = rentalServices.getRentalDetails(rid)
        assertNotNull(updatedRental)
        assertEquals(updateEndDuration, updatedRental.endDuration)
    }

    @Test
    fun `test create two rentals, delete the first one and create again`() {
        val date1 = LocalDate(2025, 12, 30)
        val date2 = LocalDate(2025, 12, 31)
        val startDuration = 0
        val endDuration = 1
        val user = User(1, "User de Teste", "test@gmail.com")
        Users.add(user)
        val club = Club(1, "Clube de Teste")
        Clubs.add(club)
        val court = Court(1, "Court de Teste", club.cid)
        Courts.add(court)
        val rid1 = rentalServices.createNewRental(date1, startDuration, endDuration, club.cid, court.crid, user.uid)
        assertEquals(1, rid1)
        val rid2 = rentalServices.createNewRental(date2, startDuration, endDuration, club.cid, court.crid, user.uid)
        assertEquals(2, rid2)
        val removed = rentalServices.deleteRental(rid1)
        assertTrue(removed)
        val rid1v2 = rentalServices.createNewRental(date1, startDuration, endDuration, club.cid, court.crid, user.uid)
        assertEquals(1, rid1v2)
    }

    @Test
    fun `test createRental for same date, court and club, for overlapping durations`() {
        val date = LocalDate(2025, 12, 31)
        val startDuration = 1
        val endDuration = 4
        val startDuration2 = 2
        val endDuration2 = 6
        val user = User(1, "User de Teste", "test@gmail.com")
        Users.add(user)
        val club = Club(1, "Clube de Teste")
        Clubs.add(club)
        val court = Court(1, "Court de Teste", club.cid)
        Courts.add(court)
        rentalServices.createNewRental(date, startDuration, endDuration, club.cid, court.crid, user.uid)
        assertFailsWith<AppError> { rentalServices.createNewRental(date, startDuration2, endDuration2, club.cid, court.crid, user.uid) }
    }

    @Test
    fun `test createRental for same date, court and club, but differents hours`() {
        val date = LocalDate(2025, 12, 31)
        val startDuration = 1
        val endDuration = 4
        val startDuration2 = 0
        val endDuration2 = 1
        val user = User(1, "User de Teste", "test@gmail.com")
        Users.add(user)
        val club = Club(1, "Clube de Teste")
        Clubs.add(club)
        val court = Court(1, "Court de Teste", club.cid)
        Courts.add(court)
        rentalServices.createNewRental(date, startDuration, endDuration, club.cid, court.crid, user.uid)
        rentalServices.createNewRental(date, startDuration2, endDuration2, club.cid, court.crid, user.uid)
        val rentals = rentalServices.getRentals(club.cid, court.crid, date)
        assertEquals(2, rentals.size)
    }

    @Test
    fun `test updateRental for same date, court and club,for overlapping durations`() {
        val date = LocalDate(2025, 12, 31)
        val startDuration = 1
        val endDuration = 4
        val startDuration2 = 4
        val endDuration2 = 8
        val updateStartDuration = 5
        val updateEndDuration = 7
        val user = User(1, "User de Teste", "test@gmail.com")
        Users.add(user)
        val club = Club(1, "Clube de Teste")
        Clubs.add(club)
        val court = Court(1, "Court de Teste", club.cid)
        Courts.add(court)
        val rid = rentalServices.createNewRental(date, startDuration, endDuration, club.cid, court.crid, user.uid)
        val rid2 = rentalServices.createNewRental(date, startDuration2, endDuration2, club.cid, court.crid, user.uid)
        assertFailsWith<AppError> {
            rentalServices.updateRental(rid, date, updateStartDuration, updateEndDuration, club.cid, court.crid, user.uid)
        }
    }

    @Test
    fun `test updateRental for same date, court and club but differents hours`() {
        val date = LocalDate(2025, 12, 31)
        val startDuration = 1
        val endDuration = 4
        val startDuration2 = 4
        val endDuration2 = 8
        val updateStartDuration = 2
        val updateEndDuration = 4
        val user = User(1, "User de Teste", "test@gmail.com")
        Users.add(user)
        val club = Club(1, "Clube de Teste")
        Clubs.add(club)
        val court = Court(1, "Court de Teste", club.cid)
        Courts.add(court)
        val rid = rentalServices.createNewRental(date, startDuration, endDuration, club.cid, court.crid, user.uid)
        rentalServices.createNewRental(date, startDuration2, endDuration2, club.cid, court.crid, user.uid)
        rentalServices.updateRental(rid, date, updateStartDuration, updateEndDuration, club.cid, court.crid, user.uid)
        val rentals = rentalServices.getRentals(club.cid, court.crid, date)
        assertEquals(2, rentals.size)
    }
}
