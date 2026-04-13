package pt.isel.ls.server.data.postgresSQL

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toLocalDateTime
import pt.isel.ls.server.common.ErrorCode
import pt.isel.ls.server.common.Errors
import pt.isel.ls.server.data.RentalData
import pt.isel.ls.server.data.postgresSQL.utils.DatabaseConnection
import pt.isel.ls.server.data.postgresSQL.utils.SQL_CHECK_COURT_IN_CLUB
import pt.isel.ls.server.data.postgresSQL.utils.SQL_CHECK_RENTAL_CONFLICT
import pt.isel.ls.server.data.postgresSQL.utils.SQL_CREATE_RENTAL
import pt.isel.ls.server.data.postgresSQL.utils.SQL_DELETE_RENTAL
import pt.isel.ls.server.data.postgresSQL.utils.SQL_RENTALS
import pt.isel.ls.server.data.postgresSQL.utils.SQL_RENTALS_NO_DATE
import pt.isel.ls.server.data.postgresSQL.utils.SQL_RENTALS_USER
import pt.isel.ls.server.data.postgresSQL.utils.SQL_RENTAL_DETAILS
import pt.isel.ls.server.data.postgresSQL.utils.SQL_UPDATE_RENTAL
import pt.isel.ls.server.data.postgresSQL.utils.checkEntityExists
import pt.isel.ls.server.domain.Rental
import java.sql.Connection
import java.sql.Date
import java.sql.ResultSet

/**
 * Classe que implementa a interface [RentalData] para manipulação de rentals numa base de dados.
 * Contém métodos para criar um rental, obter detalhes de um rental, obter rentals por data/club/court,
 * obter os horários disponíveis, obter os rentals de um user, eliminar e atualizar rentals.
 */
class RentalPostgresSQL : RentalData {
    /**
     * Função extensão que vai servir para converter um LocalDate num objeto Date do SQL.
     */
    private fun LocalDate.toSqlDate(): Date = Date.valueOf(this.toString())

    /**
     * Função de extensão que converte um objeto Date do SQL em um LocalDate do Kotlin.
     * Se a conversão falhar, retorna a data atual.
     */
    private fun Date.tolocalDate() =
        this.toLocalDate()?.toKotlinLocalDate() ?: Clock.System
            .now()
            .toLocalDateTime(TimeZone.UTC)
            .date

    /**
     * Função que verifica se um court pertence a um club específico.
     *
     * Lança um erro se o court não pertencer ao club.
     *
     * @param con A conexão com a base de dados.
     * @param court O ID do court a ser verificado.
     * @param club O ID do club ao qual o court deve pertencer.
     */
    private fun checkCourtInClub(
        con: Connection,
        court: Int,
        club: Int,
    ) {
        con.prepareStatement(SQL_CHECK_COURT_IN_CLUB).use { stmt ->
            stmt.setInt(1, court)
            stmt.setInt(2, club)
            val rs = stmt.executeQuery()
            if (!rs.next()) throw Errors.clubNotInCourt(club, court)
        }
    }

    /**
     * Função que verifica se já existe um rental para o court e club especificados
     *
     * Lança um erro se já existir um rental para o court e club especificados
     *
     * @param con A conexão com a base de dados.
     * @param club O ID do club ao qual o court pertence.
     * @param court O ID do court a ser verificado.
     * @param date A data do rental a ser verificado.
     * @param startDuration A hora de início do rental a ser verificado.
     * @param endDuration A hora de fim do rental a ser verificado.
     * @throws ErrorCode.COURT_ALREADY_RENTED se já existir um rental para o court e club especificados.
     */
    private fun checkRentalConflict(
        con: Connection,
        club: Int,
        court: Int,
        date: LocalDate,
        startDuration: Int,
        endDuration: Int,
    ) {
        con.prepareStatement(SQL_CHECK_RENTAL_CONFLICT).use { stmt ->
            stmt.setInt(1, club)
            stmt.setInt(2, court)
            stmt.setDate(3, date.toSqlDate())
            stmt.setInt(4, startDuration)
            stmt.setInt(5, startDuration)
            stmt.setInt(6, endDuration)
            stmt.setInt(7, endDuration)
            val rs = stmt.executeQuery()
            if (rs.next()) {
                val availableHours = getRentalsAvailableHours(club, court, date)
                throw Errors.alreadyRented(club, court, date, startDuration, endDuration, availableHours)
            }
        }
    }

    /**
     * Função de extensão que converte um ResultSet num objeto Rental.
     *
     * @return Um objeto [Rental] com os dados do [ResultSet].
     */
    private fun ResultSet.toRental() =
        Rental(
            getInt("rid"),
            getDate("date").tolocalDate(),
            getInt("startDuration"),
            getInt("endDuration"),
            getInt("utilizador"),
            getInt("court"),
            getInt("club"),
        )

    /**
     * Função de extensão que converte um ResultSet numa lista de objetos Rental.
     *
     * Itera sobre o ResultSet e adiciona cada Rental à lista.
     *
     * @return Uma lista de objetos [Rental] com os dados do ResultSet.
     */
    private fun ResultSet.mapToRentalList(): List<Rental> =
        mutableListOf<Rental>().apply {
            while (next()) add(toRental())
        }

    /**
     * Função que cria um rental.
     *
     * Lança um erro se o utilizador, club ou court não existirem,
     * se o court não pertencer ao club, ou se já existir um rental nesse data e horas para esse court.
     * Guarda no val rental o novo rental criado por meio de uma consulta SQL.
     * Caso ocorra algum erro durante a criação do rental, a transação é revertida e o erro é lançado.
     * Efetua o commit da transação.
     * Por fim, retorna o ID do novo rental criado.
     *
     * @param date A data do rental a ser criado.
     * @param startDuration A hora de início do rental a ser criado.
     * @param endDuration A hora de fim do rental a ser criado.
     * @param club O ID do club ao qual o court pertence.
     * @param court O ID do court a ser alugado.
     * @param user O ID do utilizador que está a criar o rental.
     * @return O ID do [Rental] criado.
     */
    override fun createNewRental(
        date: LocalDate,
        startDuration: Int,
        endDuration: Int,
        club: Int,
        court: Int,
        user: Int,
    ): Int =
        DatabaseConnection.getConnection().use { con ->
            try {
                checkEntityExists(con, "utilizador", "uid", user)
                checkEntityExists(con, "club", "cid", club)
                checkCourtInClub(con, court, club)
                checkRentalConflict(con, club, court, date, startDuration, endDuration)
                val rental =
                    con
                        .prepareStatement(SQL_CREATE_RENTAL)
                        .use { stmt ->
                            stmt.setDate(1, date.toSqlDate())
                            stmt.setInt(2, startDuration)
                            stmt.setInt(3, endDuration)
                            stmt.setInt(4, user)
                            stmt.setInt(5, court)
                            stmt.setInt(6, club)
                            stmt.executeQuery().use { rs ->
                                if (rs.next()) rs.toRental() else throw Errors.serverError()
                            }
                        }
                con.commit()
                rental.rid
            } catch (e: Exception) {
                con.rollback()
                throw e
            }
        }

    /**
     * Função que obtém os detalhes de um rental específico.
     *
     * Retorna um objeto Rental com os detalhes do rental ou null se o rental não existir.
     *
     * @param rid O ID do [Rental] cujos detalhes se pretende obter.
     */
    override fun getRentalDetails(rid: Int): Rental? =
        DatabaseConnection.getConnection().use { con ->
            con.prepareStatement(SQL_RENTAL_DETAILS).use { stmt ->
                stmt.setInt(1, rid)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) rs.toRental() else null
                }
            }
        }

    /**
     * Função que obtém os rentals de um court específico num club em uma data específica.
     *
     * Lança um erro se o club ou court não existirem, ou se o court não pertencer ao club.
     * Se a data for nula, obtém todos os rentals do court no club.
     * Caso contrário, obtém os rentals do court no club na data especificada.
     *
     * @param club O ID do club ao qual o court pertence.
     * @param court O ID do court cujos rentals se pretende obter.
     * @param date A data dos rentals a serem obtidos, ou null para obter todos os rentals do court.
     * @return Uma lista de objetos [Rental] com os rentals do court no club na data especificada.
     */
    override fun getRentals(
        club: Int,
        court: Int,
        date: LocalDate?,
    ): List<Rental> =
        DatabaseConnection.getConnection().use { con ->
            checkEntityExists(con, "club", "cid", club)
            checkEntityExists(con, "court", "crid", court)
            checkCourtInClub(con, court, club)
            if (date == null) {
                con.prepareStatement(SQL_RENTALS_NO_DATE).use { stmt ->
                    stmt.setInt(1, club)
                    stmt.setInt(2, court)
                    stmt.executeQuery().use { rs ->
                        rs.mapToRentalList()
                    }
                }
            } else {
                con
                    .prepareStatement(SQL_RENTALS)
                    .use { stmt ->
                        stmt.setInt(1, club)
                        stmt.setInt(2, court)
                        stmt.setDate(3, date.toSqlDate())
                        stmt.executeQuery().use { rs ->
                            rs.mapToRentalList()
                        }
                    }
            }
        }

    /**
     * Função que obtém os rentals de um utilizador específico.
     *
     * Retorna uma lista de objetos Rental com os rentals do utilizador ou null caso este não tenha rentals ou se nãp existir.
     *
     * @param user O ID do utilizador cujos rentals se pretende obter.
     * @return Uma lista de objetos Rental com os rentals do utilizador.
     */
    override fun getRentalsOfUser(user: Int): List<Rental> =
        DatabaseConnection.getConnection().use { con ->
            con.prepareStatement(SQL_RENTALS_USER).use { stmt ->
                stmt.setInt(1, user)
                stmt.executeQuery().use { rs ->
                    rs.mapToRentalList()
                }
            }
        }

    /**
     * Função que obtém as horas disponíveis para alugar um court em um club em uma data específica.
     *
     * Guarda no val rentals a lista de rentals existentes para o court no club na data especificada.
     * Guarda no val hours uma lista mutável de horas de 0 a 23.
     * Para cada rental, remove as horas ocupadas da lista de horas disponíveis.
     * Por fim, retorna a lista de horas disponíveis.
     *
     * @param con A conexão com a base de dados.
     * @param club O ID do club ao qual o court pertence.
     * @param court O ID do court cujas horas disponíveis se pretende obter.
     * @param date A data para a qual se pretende obter as horas disponíveis.
     * @return Uma lista de horas disponíveis para alugar o court no club na data especificada.
     */
    private fun getRentalsAvailableHours(
        con: Connection,
        club: Int,
        court: Int,
        date: LocalDate,
    ): List<Int> {
        val rentals =
            con.prepareStatement(SQL_RENTALS).use { stmt ->
                stmt.setInt(1, club)
                stmt.setInt(2, court)
                stmt.setDate(3, date.toSqlDate())
                stmt.executeQuery().use { rs ->
                    rs.mapToRentalList()
                }
            }
        val hours = (0..23).toMutableList()
        rentals.forEach { rental ->
            for (hour in rental.startDuration..<rental.endDuration) {
                hours.remove(hour)
            }
        }
        return hours
    }

    /**
     * Função que obtém as horas disponíveis para alugar um court em um club em uma data específica.
     *
     * Retorna uma lista de horas disponíveis para alugar o court no club na data especificada.
     *
     * @param club O ID do club ao qual o court pertence.
     * @param court O ID do court cujas horas disponíveis se pretende obter.
     * @param date A data para a qual se pretende obter as horas disponíveis.
     * @return Uma lista de horas disponíveis para alugar o court no club na data especificada.
     */
    override fun getRentalsAvailableHours(
        club: Int,
        court: Int,
        date: LocalDate,
    ): List<Int> =
        DatabaseConnection.getConnection().use { con ->
            getRentalsAvailableHours(con, club, court, date)
        }

    /**
     * Função que apaga um rental específico.
     *
     * Retorna true se o rental foi apagado com sucesso, ou lança um erro se o rental não existir.
     *
     * @param rental O ID do rental a ser apagado.
     * @return `true` se o rental foi apagado com sucesso, ou lança um erro se o rental não existir.
     */
    override fun deleteRental(rental: Int): Boolean =
        DatabaseConnection.getConnection().use { con ->
            con.prepareStatement(SQL_DELETE_RENTAL).use { stmt ->
                stmt.setInt(1, rental)
                val rs = stmt.executeUpdate()
                if (rs > 0) {
                    con.commit()
                    true
                } else {
                    con.rollback()
                    throw Errors.rentalNotFound(rental)
                }
            }
        }

    /**
     * Função que atualiza um rental específico.
     *
     * Lança um erro se o rental a ser atualizado não possa ser atualizado devido a conflitos de horário.
     * Retorna o rental atualizado.
     *
     * @param rental O ID do rental a ser atualizado.
     * @param date A nova data do rental.
     * @param startDuration A nova hora de início do rental.
     * @param endDuration A nova hora de fim do rental.
     * @param club O ID do club ao qual o court pertence.
     * @param court O ID do court a ser alugado.
     * @param user O ID do utilizador que está a atualizar o rental.
     * @return O [Rental] atualizado.
     */
    override fun updateRental(
        rental: Int,
        date: LocalDate,
        startDuration: Int,
        endDuration: Int,
        club: Int,
        court: Int,
        user: Int,
    ): Rental =
        DatabaseConnection.getConnection().use { con ->
            checkRentalConflict(con, club, court, date, startDuration, endDuration)
            con
                .prepareStatement(SQL_UPDATE_RENTAL)
                .use { stmt ->
                    stmt.setDate(1, date.toSqlDate())
                    stmt.setInt(2, startDuration)
                    stmt.setInt(3, endDuration)
                    stmt.setInt(4, user)
                    stmt.setInt(5, court)
                    stmt.setInt(6, club)
                    stmt.setInt(7, rental)
                    val rs = stmt.executeUpdate()
                    if (rs == 0) {
                        con.rollback()
                        throw Errors.rentalNotFound(rental)
                    }
                    con.commit()
                }
            getRentalDetails(rental) ?: throw Errors.rentalNotFound(rental)
        }
}
