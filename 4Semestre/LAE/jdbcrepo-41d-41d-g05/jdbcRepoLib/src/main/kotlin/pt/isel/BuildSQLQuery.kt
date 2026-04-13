package pt.isel

fun buildSQLQuery(
    queryOG: String,
    whereClauses: List<Pair<String, Any?>>,
    orderByClauses: List<String>,
): String {
    val whereClause = whereClauses.joinToString(" AND ") { "${it.first} = ?" }
    val orderByClause = orderByClauses.joinToString(", ") { "$it ASC" }
    val finalSql =
        buildString {
            append(queryOG)
            if (whereClause.isNotEmpty()) append(" WHERE $whereClause")
            if (orderByClause.isNotEmpty()) append(" ORDER BY $orderByClause")
        }
    return finalSql
}
