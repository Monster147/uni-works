package pt.isel.ls.server.data.memory

import kotlinx.datetime.LocalDate
import pt.isel.ls.server.domain.Club
import pt.isel.ls.server.domain.Court
import pt.isel.ls.server.domain.Rental
import pt.isel.ls.server.domain.User

/**
 * Listas dos Users, Clubs, Courts e Rentals que serão utilizadas como base de dados em memória.
 */
val Users = mutableListOf<User>()
val Clubs = mutableListOf<Club>()
val Courts = mutableListOf<Court>()
val Rentals = mutableListOf<Rental>()

/**
 * Dados iniciais para Users, Clubs, Courts e Rentals.
 */
private val initialUsers =
    listOf(
        // password: password123
        User(1, "José", "jose@gmail.com", "123-123", "OBF:1lfg1i9a1lmp1vny1z7o1x1b1z7e1vn41lj11i6o1lc2"),
        // password: password
        User(2, "Ricardo", "ricardo@gmail.com", "456-456", "OBF:1v2j1uum1xtv1zej1zer1xtn1uvk1v1v"),
    )

private val initialClubs =
    listOf(
        Club(1, "Clube de Ténis", 1),
        Club(2, "Clube de Futebol", 2),
        Club(3, "Clube de Basquetebol", 1),
    )

private val initialCourts =
    listOf(
        Court(1, "Court 1", 1),
        Court(2, "Court 2", 1),
        Court(3, "Court 3", 2),
        Court(4, "Court 4", 3),
        Court(5, "Court 5", 3),
        Court(6, "Court 6", 3),
    )

private val initialRentals =
    listOf(
        Rental(1, LocalDate(2025, 10, 1), 10, 12, 1, 1, 1),
        Rental(2, LocalDate(2025, 10, 1), 4, 5, 1, 1, 1),
        Rental(3, LocalDate(2025, 5, 1), 12, 14, 1, 1, 1),
    )

/**
 * Função para reiniciar os dados em memória, restaurando os valores iniciais.
 */
fun resetMemoryData() {
    Users.clear()
    Users.addAll(initialUsers)

    Clubs.clear()
    Clubs.addAll(initialClubs)

    Courts.clear()
    Courts.addAll(initialCourts)

    Rentals.clear()
    Rentals.addAll(initialRentals)
}
