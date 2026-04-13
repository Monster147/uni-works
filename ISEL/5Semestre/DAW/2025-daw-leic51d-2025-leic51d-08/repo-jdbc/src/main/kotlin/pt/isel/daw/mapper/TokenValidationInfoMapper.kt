package pt.isel.daw.mapper

import pt.isel.daw.TokenValidationInfo
import java.sql.ResultSet

fun ResultSet.getTokenValidationInfo(columnLabel: String): TokenValidationInfo? = getString(columnLabel)?.let { TokenValidationInfo(it) }
