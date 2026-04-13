package pt.isel.daw.mapper

import java.sql.ResultSet
import java.time.Instant

fun ResultSet.getInstant(columnLabel: String): Instant? = getLong(columnLabel).let { if (wasNull()) null else Instant.ofEpochSecond(it) }
