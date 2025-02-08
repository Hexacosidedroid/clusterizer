package ru.cib.clusterizer.controller.websocket

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.asFlux
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import ru.cib.clusterizer.dao.rsocket.LogContainerRequest
import ru.cib.clusterizer.domain.config.ConfigId
import ru.cib.clusterizer.domain.docker.DockerApiService

@Component
class LogContainerWebSocketHandler(
    apiServices: Map<ConfigId, DockerApiService>
) : CoroutineDockerWebSocketHandler(apiServices) {

    override suspend fun handleInternal(session: WebSocketSession) {
        val configId = extractConfigId(session) ?: run {
            session.close().awaitSingleOrNull()
            return
        }

        val apiService = apiServices[configId] ?: run {
            handleMissingApiService(session, configId)
            return
        }

        // Ожидаем входящее сообщение с данными запроса (LogContainerRequest)
        val requestMessage = session.receive().asFlow().first()
        val request = objectMapper.readValue(requestMessage.payloadAsText, LogContainerRequest::class.java)

        session.send(
            apiService.logContainer(request.id, request.follow, request.tail)
                .asFlux()
                .map { record ->
                    session.textMessage(objectMapper.writeValueAsString(record))
                }
        ).awaitSingleOrNull()

        session.close().awaitSingleOrNull()
    }
}