package pt.isel.ls.server.data.postgresSQL.utils

/**
 * Consultas SQL para manipulação de rentals na base de dados.
 */
const val SQL_CHECK_COURT_IN_CLUB = "SELECT 1 FROM court WHERE crid = ? AND club = ?"

const val SQL_CHECK_RENTAL_CONFLICT = """
        SELECT 1 FROM rental
        WHERE club = ? AND court = ? AND date = ?
        AND (
            (? < endDuration AND ? >= startDuration) OR
            (? > startDuration AND ? <= endDuration)
        )
    """

const val SQL_CREATE_RENTAL = """
            INSERT INTO rental (date, startDuration, endDuration, utilizador, court, club)
            VALUES (?, ?, ?, ?, ?, ?) RETURNING *
        """

const val SQL_RENTAL_DETAILS = "SELECT * FROM rental WHERE rid = ?"

const val SQL_RENTALS_NO_DATE = "SELECT * FROM rental WHERE club = ? and court = ? ORDER BY rid ASC"

const val SQL_RENTALS = "SELECT * FROM rental WHERE club = ? and court = ? and date = ? ORDER BY rid ASC"

const val SQL_RENTALS_USER = "SELECT * FROM rental WHERE utilizador = ? ORDER BY rid ASC"

const val SQL_DELETE_RENTAL = "DELETE FROM rental WHERE rid = ?"

const val SQL_UPDATE_RENTAL = """
    UPDATE rental SET date = ?, startDuration = ?, endDuration = ?, utilizador = ?, court = ?, club = ? 
    WHERE rid = ?
"""
