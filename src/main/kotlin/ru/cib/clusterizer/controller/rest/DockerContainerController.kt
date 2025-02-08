package ru.cib.clusterizer.controller.rest

import com.github.dockerjava.api.model.WaitResponse
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.cib.clusterizer.dao.rest.ImageRequest
import ru.cib.clusterizer.domain.config.ConfigId
import ru.cib.clusterizer.domain.docker.DockerApiService

@RestController
@RequestMapping("api/docker/container")
class DockerContainerController(
    private val apiServices: Map<ConfigId, DockerApiService>
) {
    
    @GetMapping("/{configId}/listOfContainers")
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

    @PostMapping("/{configId}/createContainer")
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

    @PostMapping("/{configId}/startContainer")
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

    @GetMapping("/{configId}/inspectContainer")
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

    @DeleteMapping("/{configId}/removeContainer")
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

    @GetMapping("/{configId}/waitContainer", produces = [MediaType.APPLICATION_NDJSON_VALUE])
    suspend fun waitContainer(
        @PathVariable("configId") configId: ConfigId,
        @RequestParam("id") id: String
    ): Flow<WaitResponse> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.waitContainer(id)
        return result
    }

    @GetMapping("/{configId}/diffContainer")
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

    @PostMapping("/{configId}/stopContainer")
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

    @DeleteMapping("/{configId}/killContainer")
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

    @PostMapping("/{configId}/renameContainer")
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

    @PostMapping("/{configId}/restartContainer")
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

    @GetMapping("/{configId}/topContainer")
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
}