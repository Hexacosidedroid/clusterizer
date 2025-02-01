package ru.cib.clusterizer.controller

import com.github.dockerjava.api.model.Event
import com.github.dockerjava.api.model.PullResponseItem
import kotlinx.coroutines.flow.Flow
import org.springframework.http.MediaType
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import ru.cib.clusterizer.dao.rest.ImageRequest
import ru.cib.clusterizer.dao.rsocket.LogContainerRequest
import ru.cib.clusterizer.domain.config.ConfigId
import ru.cib.clusterizer.domain.docker.DockerApiService
import ru.cib.clusterizer.domain.docker.DockerLogRecord

@Controller
class DockerRSocketController(
    private val apiServices: Map<ConfigId, DockerApiService>
) {

    @MessageMapping("docker.image.{configId}.pullImage")
    suspend fun pullImage(request: ImageRequest, @DestinationVariable configId: ConfigId): Flow<PullResponseItem> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.pullImage(request)
        return result
    }

    @MessageMapping("docker.client.{configId}.events")
    suspend fun events(@DestinationVariable configId: ConfigId): Flow<Event> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.events()
        return result
    }

    @MessageMapping("docker.client.{configId}.logContainer")
    suspend fun logContainer(
        @DestinationVariable("configId") configId: ConfigId,
        request: LogContainerRequest
    ): Flow<DockerLogRecord> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.logContainer(request.id, request.follow, request.tail)
        return result
    }

}