package ru.cib.clusterizer.controller.websocket

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.asFlux
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import ru.cib.clusterizer.dao.rest.ImageRequest
import ru.cib.clusterizer.domain.config.ConfigId
import ru.cib.clusterizer.domain.docker.DockerApiService

@Component
class PullImageWebSocketHandler(
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

        // Ожидаем входящее сообщение с JSON, преобразуем его в ImageRequest
        val requestMessage = session.receive().asFlow().first()
        val request = objectMapper.readValue(requestMessage.payloadAsText, ImageRequest::class.java)

        // Получаем Flow с данными и отправляем их клиенту в виде текстовых сообщений
        session.send(
            apiService.pullImage(request)
                .asFlux()
                .map { item ->
                    session.textMessage(objectMapper.writeValueAsString(item))
                }
        ).awaitSingleOrNull()
        session.close().awaitSingleOrNull()
    }
}