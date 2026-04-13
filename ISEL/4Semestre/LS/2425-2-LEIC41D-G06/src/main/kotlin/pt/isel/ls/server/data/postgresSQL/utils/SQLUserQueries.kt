package pt.isel.ls.server.data.postgresSQL.utils

/**
 * Consultas SQL para manipulação de courts na base de dados.
 */
const val SQL_CREATE_USER = "INSERT INTO utilizador (name, email, token, password) VALUES (?, ?, ?, ?) RETURNING *"

const val SQL_USER_DETAILS_BY_ID = "SELECT * FROM utilizador WHERE uid = ?"

const val SQL_USER_DETAILS_BY_TOKEN = "SELECT * FROM utilizador WHERE token = ?"

const val SQL_USER_DETAILS_BY_EMAIL = "SELECT * FROM utilizador WHERE email = ?"

const val SQL_USERID_AND_TOKEN = """
    SELECT uid, token FROM utilizador
    WHERE name = ? AND password = ?
"""
