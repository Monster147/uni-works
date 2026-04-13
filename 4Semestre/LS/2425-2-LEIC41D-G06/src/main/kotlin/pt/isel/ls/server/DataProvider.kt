package pt.isel.ls.server

/**
 * Função auxiliar cujo objetivo é ver se existe a variável de ambiente DATA_TYPE,
 * dependendo do seu valor, retorna qual método de data deve ser utilizado, ou seja,
 * se deve ser utilizado o DataMem(memória local) ou uma base de dados(postgres).
 *
 * @param localProvider Função que fornece os dados em memória local.
 * @param postgresProvider Função que fornece os dados na base de dados Postgres.
 */
fun <T> provideData(
    localProvider: () -> T,
    postgresProvider: () -> T,
): T =
    when (System.getenv("DATA_TYPE")) {
        "LOCAL" -> localProvider()
        "POSTGRES" -> postgresProvider()
        else -> throw IllegalArgumentException("Invalid data type")
    }

/**
 * Função auxiliar cujo objetivo é ver se existe a variável de ambiente PORT,
 * dependendo do seu valor, retorna qual porta deve ser utilizada.
 */
fun providePort(): Int = System.getenv("PORT").toIntOrNull() ?: throw IllegalArgumentException("Invalid port number")
