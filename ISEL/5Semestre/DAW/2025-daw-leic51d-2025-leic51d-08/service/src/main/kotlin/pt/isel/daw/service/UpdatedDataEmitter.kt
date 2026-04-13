package pt.isel.daw.service

interface UpdatedDataEmitter {
    fun emit(signal: UpdatedData)

    fun onCompletion(callback: () -> Unit)

    fun onError(callback: (Throwable) -> Unit)
}
