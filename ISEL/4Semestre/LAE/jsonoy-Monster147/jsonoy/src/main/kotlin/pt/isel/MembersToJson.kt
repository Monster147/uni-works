package pt.isel

import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

fun Any.membersToJson(): String {
    val klass = this::class
    val jsonMembers =
        klass.memberProperties.mapNotNull { prop ->
            val name = prop.findAnnotation<ToJsonPropName>()?.name ?: prop.name
            val propValue = prop.call(this)
            val formatterAnnotation = prop.findAnnotation<ToJsonFormatter>()
            val jsonValue =
                if (formatterAnnotation != null && propValue != null) {
                    val formatter = formatterAnnotation.formatter.createInstance()
                    formatter.format(propValue)
                } else {
                    when (propValue) {
                        is String -> "\"$propValue\""
                        is Number, is Boolean -> propValue.toString()
                        is Collection<*> -> propValue.joinToString(prefix = "[", postfix = "]") { it?.membersToJson() ?: "null" }
                        is Map<*, *> ->
                            propValue.entries.joinToString(
                                prefix = "{",
                                postfix = "}",
                            ) { "\"${it.key}\": ${it.value?.membersToJson() ?: "null"}" }
                        else -> propValue?.membersToJson()
                    }
                }
            jsonValue?.let { "\"$name\": $it" }
        }

    val jsonFunctions =
        klass.declaredMemberFunctions.map { function ->
            val name = function.findAnnotation<ToJsonPropName>()?.name ?: function.name
            val functionValue = function.call(this)
            val formatterAnnotation = function.findAnnotation<ToJsonFormatter>()
            val jsonValue =
                if (formatterAnnotation != null && functionValue != null) {
                    val formatter = formatterAnnotation.formatter.createInstance()
                    formatter.format(functionValue)
                } else {
                    when (functionValue) {
                        is String -> "\"$functionValue\""
                        is Number, is Boolean -> functionValue.toString()
                        is Map<*, *> ->
                            functionValue.entries.joinToString(
                                prefix = "{",
                                postfix = "}",
                            ) { "\"${it.key}\": ${it.value?.membersToJson() ?: "null"}" }

                        is Collection<*> ->
                            functionValue.joinToString(
                                prefix = "[",
                                postfix = "]",
                            ) { it?.membersToJson() ?: "null" }

                        else -> functionValue?.membersToJson()
                    }
                }
            "\"${name}\": $jsonValue"
        }

    return (jsonMembers + jsonFunctions).joinToString(prefix = "{", postfix = "}", separator = ", ")
}
