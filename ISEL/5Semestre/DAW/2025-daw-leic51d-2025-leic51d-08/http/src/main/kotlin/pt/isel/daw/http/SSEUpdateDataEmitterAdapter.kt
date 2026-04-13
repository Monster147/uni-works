package pt.isel.daw.http

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import pt.isel.daw.service.UpdatedData
import pt.isel.daw.service.UpdatedDataEmitter

class SSEUpdateDataEmitterAdapter(
    private val sseEmitter: SseEmitter,
) : UpdatedDataEmitter {
    override fun emit(signal: UpdatedData) {
        val msg =
            when (signal) {
                is UpdatedData.Message ->
                    SseEmitter
                        .event()
                        .id(signal.id.toString())
                        .name("message")
                        .data(signal)

                is UpdatedData.KeepAlive -> SseEmitter.event().comment(signal.toString())
            }
        sseEmitter.send(msg)
    }

    override fun onCompletion(callback: () -> Unit) {
        sseEmitter.onCompletion(callback)
    }

    override fun onError(callback: (Throwable) -> Unit) {
        sseEmitter.onError(callback)
    }
}
