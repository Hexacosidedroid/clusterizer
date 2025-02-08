package ru.cib.clusterizer.config.websocket

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import ru.cib.clusterizer.controller.websocket.EventsWebSocketHandler
import ru.cib.clusterizer.controller.websocket.LogContainerWebSocketHandler
import ru.cib.clusterizer.controller.websocket.PullImageWebSocketHandler

@Configuration
class WebSocketConfig(
    private val pullImageWebSocketHandler: PullImageWebSocketHandler,
    private val eventsWebSocketHandler: EventsWebSocketHandler,
    private val logContainerWebSocketHandler: LogContainerWebSocketHandler
) {

    @Bean
    fun handlerMapping(): HandlerMapping = SimpleUrlHandlerMapping().apply {
        order = -1
        urlMap = mapOf(
            "/ws/docker/image/{configId}/pullImage" to pullImageWebSocketHandler,
            "/ws/docker/client/{configId}/events" to eventsWebSocketHandler,
            "/ws/docker/client/{configId}/logContainer" to logContainerWebSocketHandler
        )
    }

    @Bean
    fun handlerAdapter() = WebSocketHandlerAdapter()
}