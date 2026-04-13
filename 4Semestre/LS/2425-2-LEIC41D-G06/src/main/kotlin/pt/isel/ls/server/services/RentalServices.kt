package pt.isel.ls.server.services

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import pt.isel.ls.server.common.Errors
import pt.isel.ls.server.data.RentalData
import pt.isel.ls.server.domain.Rental

/**
 * Classe que define os Services para os Rentals.
 * Contém métodos para criar um rental, obter detalhes de um rental, obter rentals por data/club/court,
 * obter os horários disponíveis, obter os rentals de um user, eliminar e atualizar rentals.
 *
 * @property rentalData Interface utilizada para manipulação das reservas.
 */
class RentalServices(
    private val rentalData: RentalData,
) {
    /**
     * Função que vai servir para criar um rental.
     *
     * Verifica se a data é anterior à data atual, se sim, lança um erro de parâmetro inválido.
     * Verifica se a duração de início é maior ou igual à duração de fim, se sim, lança um erro de parâmetro inválido.
     * Verifica se o ID do club é menor ou igual a 0, se sim, lança um erro de parâmetro inválido.
     * Verifica se o ID do court é menor ou igual a 0, se sim, lança um erro de parâmetro inválido.
     * Verifica se as durações estão dentro dos limites permitidos, se não, lança um erro de parâmetro inválido.
     * Por fim, chama a função createNewRental do rentalData para criar o rental que retorna o ID do novo rental criado.
     *
     * @param date A data da reserva.
     * @param startDuration A hora de início da reserva (0-22).
     * @param endDuration A hora de fim da reserva (1-23).
     * @param club O ID do club ao qual pertence a reserva.
     * @param court O ID do court ao qual pertence a reserva.
     * @param user O ID do utilizador que está a fazer a reserva.
     * @return O ID do novo [Rental] criado.
     */
    fun createNewRental(
        date: LocalDate,
        startDuration: Int,
        endDuration: Int,
        club: Int,
        court: Int,
        user: Int,
    ): Int {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val today = now.date
        val currentHour = now.hour
        if (date < today) throw Errors.invalidParameter("date")
        if (startDuration >= endDuration) throw Errors.invalidParameter("duration")
        if (club <= 0) throw Errors.invalidParameter("club")
        if (court <= 0) throw Errors.invalidParameter("court")
        if (startDuration !in 0..22 || endDuration !in 1..23) throw Errors.invalidParameter("duration")
        if (date == today && startDuration <= currentHour) throw Errors.invalidParameter("duration")
        return rentalData.createNewRental(date, startDuration, endDuration, club, court, user)
    }

    /**
     * Função que vai servir para obter os detalhes de um rental específico.
     *
     * Verifica se o ID do rental é menor ou igual a 0, se sim, lança um erro de parâmetro inválido.
     * Por fim, chama a função getRentalDetails do rentalData para obter os detalhes do rental.
     *
     * @param rid O ID do rental cujos detalhes se pretende obter.
     * @return Os detalhes do [Rental] ou null se o rental não existir.
     */
    fun getRentalDetails(rid: Int): Rental? {
        if (rid <= 0) throw Errors.invalidParameter("rid")
        return rentalData.getRentalDetails(rid)
    }

    /**
     * Função que vai servir para obter os rentals de um determinado club, court e data.
     *
     * Verifica se o ID do club ou court é menor, ou igual a 0, se sim, lança um erro de parâmetro inválido.
     * Por fim, chama a função getRentals do rentalData para obter a lista de rentals correspondentes.
     *
     * @param club O ID do club cujos rentals se pretende obter.
     * @param court O ID do court cujos rentals se pretende obter.
     * @param date A data dos rentals a serem obtidos (pode ser nula).
     * @return A lista de objetos [Rental] que correspondem ao club, court e data especificados.
     */
    fun getRentals(
        club: Int,
        court: Int,
        date: LocalDate?,
    ): List<Rental> {
        if (club <= 0) throw Errors.invalidParameter("club")
        if (court <= 0) throw Errors.invalidParameter("court")
        return rentalData.getRentals(club, court, date)
    }

    /**
     * Função que vai servir para obter os rentals de um determinado utilizador.
     *
     * Verifica se o ID do utilizador é menor ou igual a 0, se sim, lança um erro de parâmetro inválido.
     * Por fim, chama a função getRentalsOfUser do rentalData para obter a lista de rentals do utilizador.
     *
     * @param user O ID do utilizador cujos rentals se pretende obter.
     * @return A lista de objetos [Rental] que correspondem ao utilizador especificado.
     */
    fun getRentalsOfUser(user: Int): List<Rental> {
        if (user <= 0) throw Errors.invalidParameter("user")
        return rentalData.getRentalsOfUser(user)
    }

    /**
     * Função que vai servir para obter as horas disponíveis para reservas de um court específico num determinado club e data.
     *
     * Verifica se o ID do club ou court é menor, ou igual a 0, se sim, lança um erro de parâmetro inválido.
     * Verifica se a data é anterior à data atual, se sim, lança um erro de parâmetro inválido.
     * Por fim, chama a função getRentalsAvailableHours do rentalData para obter a lista de horas disponíveis.
     *
     * @param club O ID do club ao qual pertence o court.
     * @param court O ID do court cujas horas disponíveis se pretende obter.
     * @param date A data para a qual se pretende obter as horas disponíveis.
     * @return A lista de horas disponíveis para reservas no court especificado.
     */
    fun getRentalsAvailableHours(
        club: Int,
        court: Int,
        date: LocalDate,
    ): List<Int> {
        if (club <= 0) throw Errors.invalidParameter("club")
        if (court <= 0) throw Errors.invalidParameter("court")
        if (date <
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.UTC)
                .date
        ) {
            throw Errors.invalidParameter("date")
        }
        return rentalData.getRentalsAvailableHours(club, court, date)
    }

    /**
     * Função que vai servir para eliminar um rental específico.
     *
     * Verifica se o ID do rental é menor ou igual a 0, se sim, lança um erro de parâmetro inválido.
     * Por fim, chama a função deleteRental do rentalData para eliminar o rental.
     *
     * @param rental O ID do rental a ser eliminado.
     * @return true se o rental foi eliminado com sucesso, false caso contrário.
     */
    fun deleteRental(rental: Int): Boolean {
        if (rental <= 0) throw Errors.invalidParameter("rental")
        return rentalData.deleteRental(rental)
    }

    /**
     * Função que vai servir para atualizar um rental específico.
     *
     * Verifica se a data é anterior à data atual, se sim, lança um erro de parâmetro inválido.
     * Verifica se a duração de início é maior ou igual à duração de fim, se sim, lança um erro de parâmetro inválido.
     * Verifica se o ID do club é menor ou igual a 0, se sim, lança um erro de parâmetro inválido.
     * Verifica se o ID do court é menor ou igual a 0, se sim, lança um erro de parâmetro inválido.
     * Verifica se o ID do rental é menor ou igual a 0, se sim, lança um erro de parâmetro inválido.
     * Verifica se as durações estão dentro dos limites permitidos, se não, lança um erro de parâmetro inválido.
     * Por fim, chama a função updateRental do rentalData para atualizar o rental e retorna o objeto atualizado.
     *
     * @param rental O ID do rental a ser atualizado.
     * @param date A nova data da reserva.
     * @param startDuration A nova hora de início da reserva (0-22).
     * @param endDuration A nova hora de fim da reserva (1-23).
     * @param club O ID do club ao qual pertence a reserva.
     * @param court O ID do court ao qual pertence a reserva.
     * @param user O ID do utilizador que está a fazer a reserva.
     * @return O objeto [Rental] atualizado.
     */
    fun updateRental(
        rental: Int,
        date: LocalDate,
        startDuration: Int,
        endDuration: Int,
        club: Int,
        court: Int,
        user: Int,
    ): Rental {
        if (date <
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.UTC)
                .date
        ) {
            throw Errors.invalidParameter("date")
        }
        if (startDuration >= endDuration) throw Errors.invalidParameter("duration")
        if (club <= 0) throw Errors.invalidParameter("club")
        if (court <= 0) throw Errors.invalidParameter("court")
        if (rental <= 0) throw Errors.invalidParameter("rental")
        if (startDuration !in 0..22 || endDuration !in 1..23) throw Errors.invalidParameter("duration")
        return rentalData.updateRental(rental, date, startDuration, endDuration, club, court, user)
    }
}
