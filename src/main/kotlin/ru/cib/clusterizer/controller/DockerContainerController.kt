package ru.cib.clusterizer.controller

import com.github.dockerjava.api.model.Statistics
import com.github.dockerjava.api.model.WaitResponse
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.cib.clusterizer.dao.rest.ImageRequest
import ru.cib.clusterizer.domain.config.ConfigId
import ru.cib.clusterizer.domain.docker.DockerApiService
import ru.cib.clusterizer.domain.docker.DockerLogRecord

@CrossOrigin
@RestController
@RequestMapping("api/docker")
class DockerContainerController(
    private val apiServices: Map<ConfigId, DockerApiService>
) {
    
    @GetMapping("/{configId}/container/listOfContainers")
    fun getListOfContainers(
        @PathVariable("configId") configId: ConfigId,
        @RequestParam("all") all: Boolean
    ): ResponseEntity<Any> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.listOfContainers(all)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PostMapping("/{configId}/container/createContainer")
    fun createContainer(
        @PathVariable("configId") configId: ConfigId,
        @RequestBody request: ImageRequest
    ): ResponseEntity<Any> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.createContainer(request)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PostMapping("/{configId}/container/startContainer")
    fun startContainer(
        @PathVariable("configId") configId: ConfigId,
        @RequestParam("id") id: String
    ): ResponseEntity<Any> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.startContainer(id)
        return if (result)
            ResponseEntity(HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @GetMapping("/{configId}/container/inspectContainer")
    fun inspectContainer(
        @PathVariable("configId") configId: ConfigId,
        @RequestParam("id") id: String
    ): ResponseEntity<Any> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.inspectContainer(id)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @DeleteMapping("/{configId}/container/removeContainer")
    fun removeContainer(
        @PathVariable("configId") configId: ConfigId,
        @RequestParam("id") id: String,
        @RequestParam("force") force: Boolean
    ): ResponseEntity<Any> {

        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.removeContainer(id, force)
        return if (result)
            ResponseEntity(HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @GetMapping("/{configId}/container/waitContainer", produces = [MediaType.APPLICATION_NDJSON_VALUE])
    suspend fun waitContainer(
        @PathVariable("configId") configId: ConfigId,
        @RequestParam("id") id: String
    ): Flow<WaitResponse> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.waitContainer(id)
        return result
    }

    @GetMapping("/{configId}/container/logContainer", produces = [MediaType.APPLICATION_NDJSON_VALUE])
    suspend fun logContainer(
        @PathVariable("configId") configId: ConfigId,
        @RequestParam("id") id: String,
        @RequestParam("follow") follow: Boolean = true,
        @RequestParam("tail") tail: Int? = null
    ): Flow<DockerLogRecord> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.logContainer(id, follow, tail)
        return result
    }

    @GetMapping("/{configId}/container/diffContainer")
    fun diffContainer(
        @PathVariable("configId") configId: ConfigId,
        @RequestParam("id") id: String
    ): ResponseEntity<Any> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.diffContainer(id)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PostMapping("/{configId}/container/stopContainer")
    fun stopContainer(
        @PathVariable("configId") configId: ConfigId,
        @RequestParam("id") id: String
    ): ResponseEntity<Any> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.stopContainer(id)
        return if (result)
            ResponseEntity(HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @DeleteMapping("/{configId}/container/killContainer")
    fun killContainer(
        @PathVariable("configId") configId: ConfigId,
        @RequestParam("id") id: String
    ): ResponseEntity<Any> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.killContainer(id)
        return if (result)
            ResponseEntity(HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PostMapping("/{configId}/container/renameContainer")
    fun renameContainer(
        @PathVariable("configId") configId: ConfigId,
        @RequestParam("id") id: String,
        @RequestParam("name") name: String
    ): ResponseEntity<Any> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.renameContainer(id, name)
        return if (result)
            ResponseEntity(HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PostMapping("/{configId}/container/restartContainer")
    fun restartContainer(
        @PathVariable("configId") configId: ConfigId,
        @RequestParam("id") id: String
    ): ResponseEntity<Any> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.restartContainer(id)
        return if (result)
            ResponseEntity(HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @GetMapping("/{configId}/container/topContainer")
    fun topContainer(
        @PathVariable("configId") configId: ConfigId,
        @RequestParam("id") id: String
    ): ResponseEntity<Any> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.topContainer(id)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @GetMapping("/{configId}/client/stats", produces = [MediaType.APPLICATION_NDJSON_VALUE])
    suspend fun statsContainer(
        @PathVariable("configId") configId: ConfigId,
        @RequestParam("id") id: String
    ): Flow<Statistics> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.statsContainer(id)
        return result
    }
}