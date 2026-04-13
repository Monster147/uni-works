package pt.isel

fun Any.fieldsToJson(): String {
    val fields = this::class.java.declaredFields
    val jsonFields =
        fields.mapNotNull { field ->
            field.isAccessible = true
            val value = field.get(this)
            val jsonValue =
                when (value) {
                    is String -> "\"$value\""
                    is Number, is Boolean -> value.toString()
                    is Collection<*> ->
                        value.joinToString(
                            prefix = "[",
                            postfix = "]",
                        ) { it?.fieldsToJson() ?: "null" }
                    is Map<*, *> ->
                        value.entries.joinToString(
                            prefix = "{",
                            postfix = "}",
                        ) {
                            "\"${it.key}\": ${it.value?.fieldsToJson() ?: "null"}"
                        }
                    else -> value?.fieldsToJson()
                }
            jsonValue?.let { "\"${field.name}\": $it" }
        }
    return jsonFields.joinToString(prefix = "{", postfix = "}", separator = ", ")
}
