package pt.isel

import java.io.File
import java.lang.classfile.ClassBuilder
import java.lang.classfile.ClassFile
import java.lang.classfile.ClassFile.ACC_FINAL
import java.lang.classfile.ClassFile.ACC_PRIVATE
import java.lang.classfile.ClassFile.ACC_PUBLIC
import java.lang.classfile.CodeBuilder
import java.lang.classfile.MethodBuilder
import java.lang.constant.ClassDesc
import java.lang.constant.ConstantDescs.CD_Class
import java.lang.constant.ConstantDescs.CD_Object
import java.lang.constant.ConstantDescs.CD_String
import java.lang.constant.ConstantDescs.CD_boolean
import java.lang.constant.ConstantDescs.CD_char
import java.lang.constant.ConstantDescs.CD_double
import java.lang.constant.ConstantDescs.CD_float
import java.lang.constant.ConstantDescs.CD_int
import java.lang.constant.ConstantDescs.CD_long
import java.lang.constant.ConstantDescs.CD_short
import java.lang.constant.ConstantDescs.CD_void
import java.lang.constant.MethodTypeDesc
import java.net.URLClassLoader
import java.sql.Connection
import java.sql.Date
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

/**
 * Fully qualified name of the package where dynamic repository classes will be generated.
 */
private const val PACKAGE_NAME: String = "pt.isel"

/**
 * Directory-like representation of the package name, used to locate or store `.class` files.
 */
private val packageFolder = PACKAGE_NAME.replace(".", "/")

/**
 * Base directory where `.class` files can be generated or from which they will be dynamically loaded.
 */
private val root =
    RepositoryReflect::class.java
        .getResource("/")
        ?.toURI()
        ?.path
        ?: "${System.getProperty("user.dir")}/"

/**
 * ClassLoader that loads dynamically compiled classes from the base directory.
 */
private val rootLoader = URLClassLoader(arrayOf(File(root).toURI().toURL()))

/**
 * Map that acts as a cache for already generated repository instances, indexed by their respective domain `KClass`.
 */
private val repositories = mutableMapOf<KClass<*>, RepositoryReflect<*, *>>()

/**
 * Instantiates or reuses a dynamic repository associated with the provided domain class (version using `Class<T>`).
 *
 * @param K The type of the primary key.
 * @param T The type of the domain entity.
 * @param connection Active database connection.
 * @param domainKlass Java class of the entity.
 * @return Instance of the corresponding dynamic repository.
 */
fun <K : Any, T : Any> loadDynamicRepo(
    connection: Connection,
    domainKlass: Class<T>,
) = loadDynamicRepo<K, T>(connection, domainKlass.kotlin)

/**
 * Instantiates or reuses a dynamic repository associated with the provided domain class (version using `KClass<T>`).
 *
 * This function uses reflection to construct an instance of a dynamically generated repository class
 * that extends `RepositoryReflect`.
 *
 * @param K The type of the primary key.
 * @param T The type of the domain entity.
 * @param connection Active database connection.
 * @param domainKlass Kotlin class representing the domain entity.
 * @return Instance of the corresponding dynamic repository.
 */
fun <K : Any, T : Any> loadDynamicRepo(
    connection: Connection,
    domainKlass: KClass<T>,
) = repositories.getOrPut(domainKlass) {
    buildRepositoryClassfile(domainKlass)
        .constructors
        .first()
        .call(connection) as RepositoryReflect<*, *>
} as RepositoryReflect<K, T>

/**
 * Compiles and dynamically loads a repository class corresponding to the provided domain entity.
 *
 * This function assumes that the generated `.class` file will be placed in the directory specified by `root`.
 * The class name follows the convention `RepositoryDyn{EntityName}`.
 *
 * @param T The type of the domain entity.
 * @param domainKlass Kotlin class representing the domain entity.
 * @return `KClass` representation of the dynamically loaded repository class.
 */
private fun <T : Any> buildRepositoryClassfile(domainKlass: KClass<T>): KClass<out Any> {
    val className = "RepositoryDyn${domainKlass.simpleName}"
    buildRepositoryByteArray(className, domainKlass)
    return rootLoader
        .loadClass("$PACKAGE_NAME.$className")
        .kotlin
}

/**
 * Generates a byte array representing a dynamically created
 * class that extends RepositoryReflect, and then saves it to the
 * corresponding class file.
 */
fun <T : Any> buildRepositoryByteArray(
    className: String,
    domainKlass: KClass<T>,
) {
    val classDesc = ClassDesc.of(PACKAGE_NAME, className)
    val superClass = RepositoryReflect::class.descriptor()
    val connClassDesc = Connection::class.descriptor()
    val kclassClassDesc = KClass::class.descriptor()
    val resSetDesc = ResultSet::class.descriptor()
    val domainKlassClassDesc = domainKlass.descriptor()

    val parameters = domainKlass.constructors.first().parameters

    val nonPrimitiveClasses =
        parameters
            .map {
                it.type.classifier as KClass<*>
            }.filter {
                !it.isSupportedSimpleTypes() && !it.isSubclassOf(Enum::class)
            }.distinct()

    val classBytes =
        ClassFile.of().build(classDesc) { clb: ClassBuilder ->
            clb
                .withSuperclass(superClass)
            nonPrimitiveClasses.forEach {
                clb.withField(it.repoName(), superClass) { fb ->
                    fb.withFlags(ACC_PRIVATE or ACC_FINAL)
                }
            }
            clb.withMethod(
                "<init>",
                MethodTypeDesc.of(
                    CD_void,
                    connClassDesc,
                ),
                ACC_PUBLIC,
            ) { mb: MethodBuilder ->
                mb.withCode { cb: CodeBuilder ->
                    cb
                        .aload(0) // this
                        .aload(1) // connection
                        .ldc(cb.constantPool().classEntry(domainKlassClassDesc))
                        .invokestatic(
                            ClassDesc.of("kotlin.jvm.internal.Reflection"),
                            "getOrCreateKotlinClass",
                            MethodTypeDesc.of(
                                kclassClassDesc,
                                CD_Class,
                            ),
                        ).invokespecial(
                            superClass,
                            "<init>",
                            MethodTypeDesc.of(
                                CD_void,
                                connClassDesc,
                                kclassClassDesc,
                            ),
                        )
                    nonPrimitiveClasses.forEach {
                        val paramRepoClassDesc = buildRepositoryClassfile(it).descriptor()
                        cb
                            .aload(0)
                            .new_(paramRepoClassDesc)
                            .dup()
                            .aload(1)
                            .invokespecial(
                                paramRepoClassDesc,
                                "<init>",
                                MethodTypeDesc.of(
                                    CD_void,
                                    connClassDesc,
                                ),
                            ).putfield(
                                classDesc,
                                it.repoName(),
                                superClass,
                            )
                    }
                    cb.return_()
                }
            }

            clb.withMethod(
                "mapResultSetToObjects",
                MethodTypeDesc.of(domainKlassClassDesc, resSetDesc),
                ACC_PRIVATE,
            ) { mb: MethodBuilder ->
                mb.withMapResultSetToObjects(parameters, domainKlassClassDesc, clb, resSetDesc, classDesc, superClass)
            }
        }
    val file = File("$root$packageFolder/$className.class")
    file.parentFile.mkdirs()
    file.writeBytes(classBytes)
}

private fun KClass<*>.isSupportedSimpleTypes() =
    java.isPrimitive ||
        isSubclassOf(String::class) ||
        isSubclassOf(Date::class)

private fun KClass<*>.repoName() = "${simpleName?.lowercase() ?: "anonClass"}Repo"

fun MethodBuilder.withMapResultSetToObjects(
    parameters: List<KParameter>,
    dKClassDesc: ClassDesc,
    classBuilder: ClassBuilder,
    resSetDesc: ClassDesc,
    classDesc: ClassDesc,
    superClass: ClassDesc,
) {
    withCode { cb ->
        cb.new_(dKClassDesc).dup()
        parameters.forEach { parameter ->
            val colName = parameter.findAnnotation<Column>()?.name ?: parameter.name
            val type = (parameter.type.classifier as KClass<*>)
            when {
                type.isSupportedSimpleTypes() -> cb.dealWithSupportedSimpleTypes(resSetDesc, type, classBuilder, colName)
                type.isSubclassOf(Enum::class) -> {
                    val typeDesc = type.descriptor()
                    cb.dealWithEnumTypes(resSetDesc, typeDesc, classBuilder, colName)
                }
                else -> cb.dealWithOtherTypes(classBuilder, type, resSetDesc, colName, classDesc, superClass, parameter)
            }
        }
        val paramsDescs = parameters.map { it.type.descriptor() }
        cb
            .invokespecial(
                dKClassDesc,
                "<init>",
                MethodTypeDesc.of(
                    CD_void,
                    paramsDescs,
                ),
            ).areturn()
    }
}

private fun CodeBuilder.dealWithSupportedSimpleTypes(
    resSetDesc: ClassDesc,
    type: KClass<*>,
    classBuilder: ClassBuilder,
    colName: String?,
) {
    aload(1)
        .ldc(classBuilder.constantPool().stringEntry(colName))
    invokevirtual(
        resSetDesc,
        "get${type.simpleName?.replaceFirstChar(Char::titlecase)}",
        MethodTypeDesc.of(
            type.descriptor(),
            CD_String,
        ),
    )
}

private fun CodeBuilder.dealWithOtherTypes(
    classBuilder: ClassBuilder,
    type: KClass<*>,
    resSetDesc: ClassDesc,
    colName: String?,
    classDesc: ClassDesc,
    superClass: ClassDesc,
    parameter: KParameter,
) {
    aload(0)
        .getfield(
            classDesc,
            type.repoName(),
            superClass,
        ).aload(1)
        .ldc(classBuilder.constantPool().stringEntry(colName))

    val typeConstParams = type.constructors.first().parameters
    val constParamPk = typeConstParams.firstOrNull { it.findAnnotation<Pk>() != null } ?: typeConstParams.first()
    val pkClass = constParamPk.type.classifier as KClass<*>

    invokevirtual(
        resSetDesc,
        "get${pkClass.simpleName?.replaceFirstChar(Char::titlecase)}",
        MethodTypeDesc.of(
            pkClass.descriptor(),
            CD_String,
        ),
    ).invokevirtual(
        superClass,
        "getById",
        MethodTypeDesc.of(
            CD_Object,
            pkClass.descriptor(),
        ),
    ).checkcast((parameter.type.classifier as KClass<*>).descriptor())
}

private fun CodeBuilder.dealWithEnumTypes(
    resSetDesc: ClassDesc,
    typeDesc: ClassDesc,
    classBuilder: ClassBuilder,
    colName: String?,
) {
    aload(1)
        .ldc(classBuilder.constantPool().stringEntry(colName))
        .invokevirtual(
            resSetDesc,
            "getString",
            MethodTypeDesc.of(
                CD_String,
                CD_String,
            ),
        ).invokestatic(
            typeDesc,
            "valueOf",
            MethodTypeDesc.of(
                typeDesc,
                CD_String,
            ),
        )
}

/**
 * Returns the `ClassDesc` corresponding to this `KClass`, handling primitive types specially.
 *
 * This descriptor is required for bytecode generation and interaction with Java's constant API.
 *
 * @receiver Kotlin class to be described.
 * @return A `ClassDesc` instance representing the type.
 */
fun KClass<*>.descriptor(): ClassDesc =
    if (this.java.isPrimitive) {
        when (this) {
            Char::class -> CD_char
            Short::class -> CD_short
            Int::class -> CD_int
            Long::class -> CD_long
            Float::class -> CD_float
            Double::class -> CD_double
            Boolean::class -> CD_boolean
            else -> {
                throw IllegalStateException("No primitive type for ${this.qualifiedName}!")
            }
        }
    } else {
        ClassDesc.of(this.java.name)
    }

/**
 * Returns the `ClassDesc` corresponding to this `KType`.
 *
 * This function extracts the associated `KClass` and delegates to the extension above.
 *
 * @receiver Kotlin type to be described.
 * @return A `ClassDesc` instance representing the type.
 */
fun KType.descriptor(): ClassDesc {
    val klass = this.classifier as KClass<*>
    return klass.descriptor()
}
