package ru.cib.clusterizer.controller.websocket

import kotlinx.coroutines.reactor.asFlux
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import ru.cib.clusterizer.domain.config.ConfigId
import ru.cib.clusterizer.domain.docker.DockerApiService

@Component
class EventsWebSocketHandler(
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

        session.send(
            apiService.events()
                .asFlux()
                .map { event ->
                    session.textMessage(objectMapper.writeValueAsString(event))
                }
        ).awaitSingleOrNull()

        session.close().awaitSingleOrNull()
    }
}