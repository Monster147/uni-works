package pt.isel

import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1

class QueryableBuilderImplicit<T : Any>(
    private val connection: Connection,
    private var sql: String,
    private val properties: Map<KParameter, String?>,
    private val mapRowToEntity: (ResultSet) -> T,
) : Queryable<T> {
    private val conditions = mutableListOf<Pair<String, Any?>>()
    private val orderByClauses = mutableListOf<String>()

    private fun <V> resolveColumnName(prop: KProperty1<T, V>): String {
        val param = properties.keys.find { it.name == prop.name }
        return properties[param] ?: prop.name
    }

    override fun <V> whereEquals(
        prop: KProperty1<T, V>,
        value: V,
    ): Queryable<T> {
        conditions.add(resolveColumnName(prop) to value)
        return this
    }

    override fun <V> orderBy(prop: KProperty1<T, V>): Queryable<T> {
        orderByClauses.add(resolveColumnName(prop))
        return this
    }

    override fun iterator(): Iterator<T> =
        sequence {
            buildSQLQuery(sql, conditions, orderByClauses).let { finalSql ->
                connection.prepareStatement(finalSql).use { stmt ->
                    conditions.forEachIndexed { index, (_, value) ->
                        when (value) {
                            is Enum<*> -> stmt.setObject(index + 1, value, Types.OTHER)
                            else -> stmt.setObject(index + 1, value)
                        }
                    }
                    stmt.executeQuery().use { rs ->
                        while (rs.next()) {
                            yield(mapRowToEntity(rs))
                        }
                    }
                }
            }
        }.iterator()
}
