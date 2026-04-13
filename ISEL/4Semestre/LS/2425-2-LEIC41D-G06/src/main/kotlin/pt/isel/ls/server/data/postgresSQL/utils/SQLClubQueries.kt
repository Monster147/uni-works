package pt.isel.ls.server.data.postgresSQL.utils

/**
 * Consultas SQL para manipulação de clubs na base de dados.
 */
const val SQL_CREATE_CLUB = "INSERT INTO club (name, owner) VALUES (?, ?) RETURNING *"

const val SQL_CLUB_DETAILS = "SELECT * FROM club WHERE cid = ?"

const val SQL_CLUBS = "SELECT * FROM club"
