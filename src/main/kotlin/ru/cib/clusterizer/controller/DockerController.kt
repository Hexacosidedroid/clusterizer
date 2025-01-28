package ru.cib.clusterizer.controller

import com.github.dockerjava.api.model.*
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import ru.cib.clusterizer.dao.rest.ImageRequest
import ru.cib.clusterizer.domain.config.ConfigId
import ru.cib.clusterizer.domain.docker.DockerApiService
import ru.cib.clusterizer.domain.docker.DockerLogRecord

@CrossOrigin
@RestController
@RequestMapping("api/docker")
class DockerController(
    private val apiServices: Map<ConfigId, DockerApiService>
) {

    @GetMapping("/{configId}/ping")
    fun getPing(@PathVariable("configId") configId: ConfigId): ResponseEntity<Any> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.ping()
        return if (result) {
            ResponseEntity(HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/{configId}/info")
    fun getInfo(@PathVariable("configId") configId: ConfigId): ResponseEntity<Any> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.info()
        return if (result != null) {
            ResponseEntity(result, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/{configId}/version")
    fun getVersion(@PathVariable("configId") configId: ConfigId): ResponseEntity<Any> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.version()
        return if (result != null) {
            ResponseEntity(result, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/{configId}/pullImage", produces = [MediaType.APPLICATION_NDJSON_VALUE])
    suspend fun pullImage(
        @RequestBody request: ImageRequest,
        @PathVariable("configId") configId: ConfigId,
    ): Flow<PullResponseItem> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.pullImage(request)
        return result
    }

    @PostMapping("/{configId}/pushImage", produces = [MediaType.APPLICATION_NDJSON_VALUE])
    suspend fun pushImage(
        @RequestBody request: ImageRequest,
        @PathVariable("configId") configId: ConfigId
    ): Flow<PushResponseItem> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.pushImage(request)
        return result
    }

    @PostMapping("/{configId}/createImage")
    fun createImage(
        @RequestParam("repo") repo: String,
        @RequestParam("file") file: MultipartFile,
        @PathVariable("configId") configId: ConfigId
    ): ResponseEntity<Any> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.createImage(repo, file.inputStream)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PostMapping("/{configId}/loadImage")
    suspend fun loadImage(
        @RequestParam("file") file: MultipartFile,
        @PathVariable("configId") configId: ConfigId
    ): ResponseEntity<Any> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.loadImage(file.inputStream)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @GetMapping("/{configId}/searchImages")
    fun searchImages(
        @PathVariable("configId") configId: ConfigId,
        @RequestParam("search") term: String
    ): ResponseEntity<Any> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.searchImages(term)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @DeleteMapping("/{configId}/removeImage")
    fun removeImage(
        @PathVariable("configId") configId: ConfigId,
        @RequestParam("id") id: String
    ): ResponseEntity<Any> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.removeImage(id)
        return if (result)
            ResponseEntity(HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @GetMapping("/{configId}/listOfImages")
    fun getListOfImages(@PathVariable("configId") configId: ConfigId): ResponseEntity<Any> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.listOfImages()
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @GetMapping("/{configId}/inspectImage")
    fun inspectImage(
        @PathVariable("configId") configId: ConfigId,
        @RequestParam("id") id: String
    ): ResponseEntity<Any> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.inspectImage(id)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PostMapping("/{configId}/saveImage")
    fun saveImage(
        @PathVariable("configId") configId: ConfigId,
        @RequestBody request: ImageRequest,
    ): ResponseEntity<Any> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.saveImage(request)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK) //TODO return as file
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @GetMapping("/{configId}/listOfContainers")
    fun getListOfContainers(
        @PathVariable("configId") configId: ConfigId,
        @RequestParam("all") all: Boolean = false
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

    @GetMapping("/{configId}/logContainer", produces = [MediaType.APPLICATION_NDJSON_VALUE])
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

    @GetMapping("/{configId}/events", produces = [MediaType.APPLICATION_NDJSON_VALUE])
    suspend fun events(@PathVariable("configId") configId: ConfigId): Flow<Event> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.events()
        return result
    }

}