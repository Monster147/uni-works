package pt.isel

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1

class QueryableBuilderExplicit<T : Any>(
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

    override fun iterator(): Iterator<T> {
        return object : Iterator<T> {
            private var preparedStatement: PreparedStatement? = null
            private var resultSet: ResultSet? = null
            private var hasNext: Boolean? = null
            private var closed = false

            private fun ensureInitialized() {
                if (preparedStatement == null && resultSet == null) {
                    buildSQLQuery(sql, conditions, orderByClauses).let { finalSql ->
                        preparedStatement =
                            connection.prepareStatement(finalSql).apply {
                                conditions.forEachIndexed { index, (_, value) ->
                                    when (value) {
                                        is Enum<*> -> setObject(index + 1, value, Types.OTHER)
                                        else -> setObject(index + 1, value)
                                    }
                                }
                            }
                        resultSet = preparedStatement?.executeQuery()
                    }
                }
            }

            private fun ensureHasNext() {
                ensureInitialized()
                if (hasNext == null && !closed) {
                    hasNext = resultSet?.next() ?: false
                    if (hasNext == false) close()
                }
            }

            override fun hasNext(): Boolean {
                ensureHasNext()
                return hasNext == true
            }

            override fun next(): T {
                ensureHasNext()
                if (hasNext != true) throw NoSuchElementException()
                val rs = resultSet ?: throw IllegalStateException("ResultSet not initialized")
                val entity = mapRowToEntity(rs)
                hasNext = null
                return entity
            }

            private fun close() {
                if (!closed) {
                    resultSet?.close()
                    preparedStatement?.close()
                    closed = true
                }
            }
        }
    }
}
