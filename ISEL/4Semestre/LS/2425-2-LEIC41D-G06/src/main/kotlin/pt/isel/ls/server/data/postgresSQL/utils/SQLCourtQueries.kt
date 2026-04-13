package pt.isel.ls.server.data.postgresSQL.utils

/**
 * Consultas SQL para manipulação de courts na base de dados.
 */
const val SQL_CREATE_COURT = "INSERT INTO court (name, club) VALUES (?, ?) RETURNING *"

const val SQL_COURT_DETAILS = "SELECT * FROM court WHERE crid = ?"

const val SQL_COURTS_CLUB = "SELECT * FROM court WHERE club = ?"
