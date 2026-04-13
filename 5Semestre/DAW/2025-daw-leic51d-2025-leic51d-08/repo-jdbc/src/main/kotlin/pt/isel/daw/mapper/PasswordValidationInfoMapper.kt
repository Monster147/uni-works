package pt.isel.daw.mapper

import pt.isel.daw.PasswordValidationInfo
import java.sql.ResultSet

fun ResultSet.getPasswordValidationInfo(columnLabel: String): PasswordValidationInfo? =
    getString(columnLabel)?.let { PasswordValidationInfo(it) }
