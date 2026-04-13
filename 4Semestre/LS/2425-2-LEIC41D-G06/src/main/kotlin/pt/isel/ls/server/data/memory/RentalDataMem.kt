package pt.isel.ls.server.data.memory

import kotlinx.datetime.LocalDate
import pt.isel.ls.server.common.Errors
import pt.isel.ls.server.data.RentalData
import pt.isel.ls.server.domain.Rental

/**
 * Classe que implementa a interface [RentalData] para manipulação de rentals em memória.
 * Contém métodos para criar um rental, obter detalhes de um rental, obter rentals por data/club/court,
 * obter os horários disponíveis, obter os rentals de um user, eliminar e atualizar rentals.
 */
class RentalDataMem : RentalData {
    /**
     * Função que cria um rental.
     *
     * Caso o user, club ou court não existam, lança um erro correspondente.
     * Caso já exista um rental com os mesmos parâmetros (data, hora de início, hora de fim, club e court),
     * lança um erro de duplicação.
     * Caso o court já esteja reservado para o mesmo club e data, para o intervalo de horas especificado,
     * lança um erro de court já reservado.
     * Guarda no val rid o ID do novo rental, que é o próximo ID disponível.
     * Cria um objeto Rental.
     * Adiciona o novo rental à lista de rentals.
     * Por fim, retorna o ID do novo rental criado (rid).
     *
     * @param date Data do rental.
     * @param startDuration Hora de início do rental.
     * @param endDuration Hora de fim do rental.
     * @param club ID do club onde o court está localizado.
     * @param court ID do court onde o rental será realizado.
     * @param user ID do user que está a fazer o rental.
     * @return O ID do novo rental criado (rid).
     */
    override fun createNewRental(
        date: LocalDate,
        startDuration: Int,
        endDuration: Int,
        club: Int,
        court: Int,
        user: Int,
    ): Int {
        if (!Users.any { it.uid == user }) {
            throw Errors.userNotFound(user.toString())
        }
        if (!Clubs.any { it.cid == club }) {
            throw Errors.clubNotFound(club)
        }
        if (!Courts.any { it.crid == court && it.club == club }) {
            throw Errors.courtNotFound(court)
        }
        if (
            Rentals.any {
                it.club == club &&
                    it.court == court &&
                    it.date == date &&
                    it.startDuration == startDuration &&
                    it.endDuration == endDuration
            }
        ) {
            throw Errors.duplicateRental(club, court, date)
        }

        if (
            Rentals.any {
                it.club == club &&
                    it.court == court &&
                    it.date == date &&
                    (
                        startDuration in it.startDuration..<it.endDuration ||
                            (
                                endDuration in it.startDuration..it.endDuration &&
                                    endDuration != it.startDuration
                            )
                    )
            }
        ) {
            val availableHours = getRentalsAvailableHours(club, court, date)
            throw Errors.alreadyRented(club, court, date, startDuration, endDuration, availableHours)
        }
        val rid =
            Rentals
                .map { it.rid }
                .sorted()
                .let { ids ->
                    (1..(ids.lastOrNull() ?: 0)).find { it !in ids } ?: (ids.size + 1)
                }
        val rental = Rental(rid, date, startDuration, endDuration, user, court, club)
        Rentals.add(rental)
        return rid
        // Verificar se existe o court e o club, verificar se estes estão relacionados, se está disponível e se a data é válida.
        // Se tudo estiver correto retorna id da mesma(rid)
    }

    /**
     * Função que obtém os detalhes de um rental específico.
     *
     * Verifica se existe um rental com o ID fornecido (rid).
     * Se existir, retorna o rental correspondente, caso contrário, retorna null.
     *
     * @param rid O ID do rental cujos detalhes se pretende obter.
     * @return Os detalhes do rental ou null se o rental não existir.
     */
    override fun getRentalDetails(rid: Int): Rental? {
        return Rentals.find { it.rid == rid }
        // Verificar se existe um rental com esse rid, se existir devolver respetivo rental
    }

    /**
     * Função que obtém os rentals de um court específico num club,
     * sendo também possível filtrar segundo uma data específica.
     *
     * Caso o club ou court não existam, lança um erro correspondente.
     * Caso o court não esteja associado ao club, lança um erro de court não associado ao club.
     * Por fim, filtra a lista de rentals para retornar aqueles que correspondem ao club, court específicos e, se
     * foi fornecido, uma data específica.
     *
     * @param club O ID do club cujos rentals pretende-se obter.
     * @param court O ID do court cujos rentals pretende-se obter.
     * @param date A data específica para a qual se pretende obter os rentals.
     * @return Uma lista de rentals associados ao club, court específicos e, se
     * foi fornecido, uma data específica.
     */
    override fun getRentals(
        club: Int,
        court: Int,
        date: LocalDate?,
    ): List<Rental> {
        if (!Clubs.any { it.cid == club }) throw Errors.clubNotFound(club)
        if (!Courts.any { it.crid == court }) throw Errors.courtNotFound(court)
        if (!Courts.any { it.club == club }) throw Errors.clubNotInCourt(court, club)
        return if (date == null) {
            Rentals.filter { it.club == club && it.court == court }.sortedBy { it.rid }
        } else {
            Rentals.filter { it.club == club && it.court == court && it.date == date }.sortedBy { it.rid }
        }
        // Verificar se existe rental com esse club, court e data, se existir devolve o/os respetivo(s) rental
    }

    /**
     * Função que obtém os rentals de um user específico.
     *
     * Verifica se o user existe. Se não existir, lança um erro de user não encontrado.
     * Filtra a lista de rentals para retornar aqueles associados ao ID do user fornecido.
     *
     * @param user O ID do user cujos rentals pretende-se obter.
     * @return Uma lista de rentals associados ao user especificado.
     */
    override fun getRentalsOfUser(user: Int): List<Rental> {
        if (!Users.any { it.uid == user }) throw Errors.userNotFound(user.toString())
        return Rentals.filter { it.user == user }.sortedBy { it.rid }
        // Verificar se existe rental com esse user, se existir devolve o/os respetivo(s) rental
    }

    /**
     * Função que obtém as horas disponíveis para rentals num court específico de um club numa data específica.
     *
     * Caso o club ou court não existam, lança um erro correspondente.
     * Caso o court não esteja associado ao club, lança um erro de court não associado ao club.
     * Guarda no val rentals uma lista de rentals filtrada pelo club, court e data especificados.
     * Guarda no val hours uma lista mutável de horas de 0 a 23.
     * Percorre cada rental na lista de rentals e remove as horas ocupadas (do início ao fim do rental) da lista de horas disponíveis.
     * Por fim, retorna a lista de horas disponíveis.
     *
     * @param club O ID do club cujos rentals pretende-se obter.
     * @param court O ID do court cujos rentals pretende-se obter.
     * @param date A data específica para a qual se pretende obter as horas disponíveis.
     * @return Uma lista de horas disponíveis para rentals no court especificado.
     */
    override fun getRentalsAvailableHours(
        club: Int,
        court: Int,
        date: LocalDate,
    ): List<Int> {
        if (!Clubs.any { it.cid == club }) throw Errors.clubNotFound(club)
        if (!Courts.any { it.crid == court }) throw Errors.courtNotFound(court)
        if (!Courts.any { it.club == club }) throw Errors.clubNotInCourt(court, club)
        val rentals = Rentals.filter { it.club == club && it.court == court && it.date == date }
        val hours = (0..23).toMutableList()
        rentals.forEach { rental ->
            for (hour in rental.startDuration..<rental.endDuration) {
                hours.remove(hour)
            }
        }
        // Verificar se existe rental com esse club, court e data, se existir devolve as horas disponíveis
        return hours
    }

    /**
     * Função que apaga um rental específico.
     *
     * Verifica se existe um rental com o ID fornecido (rid).
     * Se não existir, lança um erro de rental não encontrado.
     * Caso exista, apaga o rental da lista de rentals e retorna true.
     *
     * @param rental O ID do rental a ser eliminado.
     * @return true se o rental foi apagado com sucesso, caso contrário, lança um erro.
     */
    override fun deleteRental(rental: Int): Boolean {
        if (!Rentals.any { it.rid == rental }) throw Errors.rentalNotFound(rental)
        // Verificar se existe rental com esse rid, se existir remove o rental e devolve true
        return Rentals.removeIf { it.rid == rental }
    }

    /**
     * Função que atualiza um rental específico.
     *
     * Verifica se o rental, club, court e user existem. Se não existirem, lança erros correspondentes.
     * Verifica se já existe um rental com os mesmos parâmetros (data, hora de início, hora de fim, club e court).
     * Se existir, lança um erro de duplicação.
     * Verifica se o court já está reservado para o mesmo club e data, para o intervalo de horas especificado.
     * Se estiver reservado, lança um erro de court já reservado.
     * Atualiza o rental existente com os novos parâmetros e retorna o novo rental atualizado.
     *
     * @param rental O ID do rental a ser atualizado.
     * @param date A nova data do rental.
     * @param startDuration A nova hora de início do rental.
     * @param endDuration A nova hora de fim do rental.
     * @param club O ID do club onde o court está localizado.
     * @param court O ID do court onde o rental será realizado.
     * @param user O ID do user que está a fazer o rental.
     * @return O novo rental atualizado.
     */
    override fun updateRental(
        rental: Int,
        date: LocalDate,
        startDuration: Int,
        endDuration: Int,
        club: Int,
        court: Int,
        user: Int,
    ): Rental {
        if (!Rentals.any { it.rid == rental }) throw Errors.rentalNotFound(rental)
        if (!Clubs.any { it.cid == club }) throw Errors.clubNotFound(club)
        if (!Courts.any { it.crid == court }) throw Errors.courtNotFound(court)
        if (!Courts.any { it.club == club }) throw Errors.clubNotInCourt(court, club)
        if (!Users.any { it.uid == user }) throw Errors.userNotFound(user.toString())
        if (Rentals.any {
                it.club == club &&
                    it.court == court &&
                    it.date == date &&
                    (
                        it.startDuration == startDuration &&
                            it.endDuration == endDuration
                    )
            }
        ) {
            throw Errors.duplicateRental(club, court, date)
        }
        if (Rentals.any {
                it.rid != rental &&
                    it.club == club &&
                    it.court == court &&
                    it.date == date &&
                    (
                        startDuration in it.startDuration..<it.endDuration ||
                            (endDuration in it.startDuration..it.endDuration) &&
                            endDuration != it.startDuration
                    )
            }
        ) {
            val availableHours = getRentalsAvailableHours(club, court, date)
            throw Errors.alreadyRented(club, court, date, startDuration, endDuration, availableHours)
        }
        val index = Rentals.indexOfFirst { it.rid == rental }
        val newRental = Rental(rental, date, startDuration, endDuration, user, court, club)
        if (index != -1) {
            Rentals[index] = newRental
        } else {
            throw Errors.rentalNotFound(rental)
        }
        // Verificar se existe rental com esse rid, court, club e user. Se existir remove o rental e coloca um novo rental com a
        // data, startDuration e endDuration atualizados
        return newRental
    }
}
