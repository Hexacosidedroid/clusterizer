package ru.cib.clusterizer.controller.websocket

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import ru.cib.clusterizer.domain.config.ConfigId
import ru.cib.clusterizer.domain.docker.DockerApiService

abstract class CoroutineDockerWebSocketHandler(
    protected val apiServices: Map<ConfigId, DockerApiService>
) : WebSocketHandler {

    protected val objectMapper = jacksonObjectMapper()

    /**
     * Извлекает configId из URL.
     * Ожидаемый формат URL: /ws/…/{configId}/…
     */
    protected fun extractConfigId(session: WebSocketSession): ConfigId? {
        val segments = session.handshakeInfo.uri.path.split("/")
        return if (segments.size > 4 && segments[4].isNotBlank()) ConfigId(segments[4])
        else null
    }

    /**
     * Отправляет сообщение об ошибке и закрывает сессию.
     */
    protected suspend fun handleMissingApiService(session: WebSocketSession, configId: ConfigId) {
        val errorMessage = "Api service for $configId is not found"
        session.send(Mono.just(session.textMessage(errorMessage))).awaitSingleOrNull()
        session.close().awaitSingleOrNull()
    }

    /**
     * Основная логика обработки сессии в suspend‑функции.
     */
    abstract suspend fun handleInternal(session: WebSocketSession)

    override fun handle(session: WebSocketSession): Mono<Void> = mono { handleInternal(session) }.then()
}