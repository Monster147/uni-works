package pt.isel

import java.sql.Connection
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import java.time.LocalDate
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

open class RepositoryReflect<K : Any, T : Any>(
    protected val connection: Connection,
    private val domainKlass: KClass<T>,
) : Repository<K, T> {
    private val tableName: String
    private val pKProperty: String
    protected val primaryConstructor: KFunction<T>
    protected val constructorParams: Collection<KParameter>
    private val entityProperties: Collection<KProperty1<*, *>>
    private val primaryKey: KProperty1<*, *>
    private val properties: Map<String, KProperty1<T, *>>
    private val KParameter.columnName: String?
        get() = findAnnotation<Column>()?.name ?: name

    init {
        tableName = domainKlass.findAnnotation<Table>()?.name
            ?: throw IllegalArgumentException("Entity '${domainKlass.simpleName}' must have a @Table annotation")
        primaryConstructor = domainKlass.primaryConstructor
            ?: throw IllegalArgumentException("No primary constructor found in ${domainKlass.simpleName}")
        entityProperties = domainKlass.memberProperties
        constructorParams = primaryConstructor.parameters
        primaryKey = entityProperties.first { it.findAnnotation<Pk>() != null }
        pKProperty = primaryKey.name
        properties = domainKlass.memberProperties.associateBy { it.name }
    }

    private val changedProperties = entityProperties - primaryKey
    private val setClause =
        changedProperties.joinToString(", ") { property ->
            val columnName = constructorParams.firstOrNull { it.name == property.name }?.columnName ?: property.name
            "\"$columnName\" = ?"
        }

    private val getByIdQuery = "SELECT * FROM $tableName WHERE $pKProperty = ?"
    private val getAllQuery = "SELECT * FROM $tableName"
    private val updateQuery = "UPDATE $tableName SET $setClause WHERE $pKProperty = ?"
    private val deleteQuery = "DELETE FROM $tableName WHERE $pKProperty = ?"

    companion object {
        private val repositoryMap = mutableMapOf<Any, RepositoryReflect<Any, Any>>()
        const val GET_BY_ID_QUERY = "SELECT * FROM %s WHERE %s = ?"
        const val GET_ALL_QUERY = "SELECT * FROM %s"
        const val UPDATE_QUERY = "UPDATE %s SET %s WHERE %s = ?"
        const val DELETE_QUERY = "DELETE FROM %s WHERE %s = ?"
    }

    override fun getById(id: K): T? =
        executeQuery(GET_BY_ID_QUERY.format(tableName, pKProperty), { it.setObject(1, id) }) { rs ->
            if (rs.next()) mapResultSetToObjects(rs) else null
        }

    override fun getAll(): List<T> =
        executeQuery(GET_ALL_QUERY.format(tableName)) { rs ->
            val list = mutableListOf<T>()
            while (rs.next()) {
                list.add(mapResultSetToObjects(rs))
            }
            list
        }

    override fun update(entity: T) {
        connection.prepareStatement(UPDATE_QUERY.format(tableName, setClause, pKProperty)).use { statement ->
            changedProperties.forEachIndexed { index, property ->
                val value = domainKlass.memberProperties.find { it.name == property.name }?.get(entity)
                when (value) {
                    is Int, is String, is Long, is Boolean,
                    is Float, is Double, is Date, is LocalDate,
                    -> statement.setObject(index + 1, value)

                    is Enum<*> -> statement.setObject(index + 1, value, Types.OTHER)
                    else -> {
                        val type = value?.let { it::class }
                        require(type != null) { "Invalid type" }
                        val pk = type.memberProperties.find { it.findAnnotation<Pk>() != null }
                        require(pk != null) { "No Primary Key found in ${type.simpleName}" }
                        statement.setObject(index + 1, pk.call(value))
                    }
                }
            }
            statement.setObject(changedProperties.size + 1, primaryKey.call(entity))
            statement.executeUpdate()
        }
    }

    override fun deleteById(id: K) {
        connection.prepareStatement(DELETE_QUERY.format(tableName, pKProperty)).use { statement ->
            statement.setObject(1, id)
            statement.executeUpdate()
        }
    }

    private fun <R> executeQuery(
        query: String,
        prepare: (PreparedStatement) -> Unit = {},
        process: (ResultSet) -> R,
    ): R {
        connection.prepareStatement(query).use { statement ->
            prepare(statement)
            statement.executeQuery().use { rs ->
                return process(rs)
            }
        }
    }

    override fun findAll(): Queryable<T> {
        val params = constructorParams.associateWith { it.columnName }
        // return QueryableBuilderExplicit(connection, GET_ALL_QUERY.format(tableName), params, ::mapResultSetToObjects)
        return QueryableBuilderImplicit(connection, GET_ALL_QUERY.format(tableName), params, ::mapResultSetToObjects)
    }

    open fun mapResultSetToObjects(rs: ResultSet) =
        primaryConstructor.callBy(
            constructorParams.associateWith { parameter ->
                val columnName = parameter.columnName
                val type = parameter.type.classifier
                requireNotNull(type) { "Invalid type" }
                when (type) {
                    Int::class, String::class, Long::class, Boolean::class,
                    Float::class, Double::class,
                    Date::class, LocalDate::class,
                    -> rs.getObject(columnName)
                    is KClass<*> -> {
                        if (type.isSubclassOf(Enum::class)) {
                            type.java.enumConstants.first { string ->
                                rs.getString(columnName) == "$string"
                            }
                        } else {
                            if (repositoryMap[type] == null) {
                                repositoryMap[type] = RepositoryReflect(connection, type as KClass<Any>)
                            }
                            repositoryMap[type]?.getById(rs.getObject(columnName))
                        }
                    }
                    else -> throw IllegalArgumentException("Unsupported type: $type")
                }
            },
        )
}
