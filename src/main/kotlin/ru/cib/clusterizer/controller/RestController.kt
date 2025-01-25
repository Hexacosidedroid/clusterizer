package ru.cib.clusterizer.controller

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.*
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import ru.cib.clusterizer.dao.docker.Registry
import ru.cib.clusterizer.dao.docker.Tls
import ru.cib.clusterizer.dao.rest.DockerConfigRequest
import ru.cib.clusterizer.dao.rest.ImageRequest
import ru.cib.clusterizer.service.DockerApiService
import ru.cib.clusterizer.service.DockerConnectionService

@RestController
class RestController(
    private val apiService: DockerApiService
) {
    var client: DockerClient? = null

    @PostMapping("/connect")
    fun setDockerConnection(
        @RequestBody config: DockerConfigRequest
    ): Version? {
        client = apiService.connect(
            config.host,
            Registry(config.url, config.user, config.password),
            Tls(config.verify, config.certPath)
        )
        apiService.ping(client)
        return apiService.version(client)
    }

    @GetMapping("/ping")
    fun getPing(): ResponseEntity<Any> {
        val result = apiService.ping(client)
        return if (result) {
            ResponseEntity(HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/info")
    fun getInfo(): ResponseEntity<Any> {
        val result = apiService.info(client)
        return if (result != null) {
            ResponseEntity(result, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/version")
    fun getVersion(): ResponseEntity<Any> {
        val result = apiService.version(client)
        return if (result != null) {
            ResponseEntity(result, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/pullImage", produces = [MediaType.APPLICATION_NDJSON_VALUE])
    suspend fun pullImage(
        @RequestBody request: ImageRequest
    ): Flow<PullResponseItem> {
        val result = apiService.pullImage(client, request)
        return result
    }

    @PostMapping("/pushImage", produces = [MediaType.APPLICATION_NDJSON_VALUE])
    suspend fun pushImage(
        @RequestBody request: ImageRequest
    ): Flow<PushResponseItem> {
        val result = apiService.pushImage(client, request)
        return result
    }

    @PostMapping("/createImage")
    fun createImage(
        @RequestParam("repo") repo: String,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Any> {
        val result = apiService.createImage(client, repo, file.inputStream)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PostMapping("/loadImage")
    suspend fun loadImage(
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Any> {
        val result = apiService.loadImage(client, file.inputStream)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @GetMapping("/searchImages")
    fun searchImages(
        @RequestParam("search") term: String
    ): ResponseEntity<Any> {
        val result = apiService.searchImages(client, term)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @DeleteMapping("/removeImage")
    fun removeImage(
        @RequestParam("id") id: String
    ): ResponseEntity<Any> {
        val result = apiService.removeImage(client, id)
        return if (result)
            ResponseEntity(HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @GetMapping("/listOfImages")
    fun getListOfImages(): ResponseEntity<Any> {
        val result = apiService.listOfImages(client)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @GetMapping("/inspectImage")
    fun inspectImage(
        @RequestParam("id") id: String
    ): ResponseEntity<Any> {
        val result = apiService.inspectImage(client, id)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PostMapping("/saveImage")
    fun saveImage(
        @RequestBody request: ImageRequest
    ) : ResponseEntity<Any> {
        val result = apiService.saveImage(client, request)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK) //TODO return as file
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @GetMapping("/listOfContainers")
    fun getListOfContainers(
        @RequestParam("all") all: Boolean
    ): ResponseEntity<Any> {
        val result = apiService.listOfContainers(client, all)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PostMapping("/createContainer")
    fun createContainer(
        @RequestBody request: ImageRequest
    ): ResponseEntity<Any> {
        val result = apiService.createContainer(client, request)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PostMapping("/startContainer")
    fun startContainer(
        @RequestParam("id") id: String
    ): ResponseEntity<Any> {
        val result = apiService.startContainer(client, id)
        return if (result)
            ResponseEntity(HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @GetMapping("/inspectContainer")
    fun inspectContainer(
        @RequestParam("id") id: String
    ): ResponseEntity<Any> {
        val result = apiService.inspectContainer(client, id)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @DeleteMapping("/removeContainer")
    fun removeContainer(
        @RequestParam("id") id: String,
        @RequestParam("force") force: Boolean
    ): ResponseEntity<Any> {
        val result = apiService.removeContainer(client, id, force)
        return if (result)
            ResponseEntity(HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @GetMapping("/waitContainer", produces = [MediaType.APPLICATION_NDJSON_VALUE])
    suspend fun waitContainer(
        @RequestParam("id") id: String
    ) : Flow<WaitResponse> {
        val result = apiService.waitContainer(client, id)
        return result
    }

    @GetMapping("/logContainer", produces = [MediaType.APPLICATION_NDJSON_VALUE])
    suspend fun logContainer(
        @RequestParam("id") id: String
    ) : Flow<Frame> {
        val result = apiService.logContainer(client, id)
        return result
    }

    @GetMapping("/diffContainer")
    fun diffContainer(
        @RequestParam("id") id: String
    ): ResponseEntity<Any> {
        val result = apiService.diffContainer(client, id)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PostMapping("/stopContainer")
    fun stopContainer(
        @RequestParam("id") id: String
    ): ResponseEntity<Any> {
        val result = apiService.stopContainer(client, id)
        return if (result)
            ResponseEntity(HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @DeleteMapping("/killContainer")
    fun killContainer(
        @RequestParam("id") id: String
    ): ResponseEntity<Any> {
        val result = apiService.killContainer(client, id)
        return if (result)
            ResponseEntity(HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PostMapping("/renameContainer")
    fun renameContainer(
        @RequestParam("id") id: String,
        @RequestParam("name") name: String
    ): ResponseEntity<Any> {
        val result = apiService.renameContainer(client, id, name)
        return if (result)
            ResponseEntity(HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PostMapping("/restartContainer")
    fun restartContainer(
        @RequestParam("id") id: String
    ): ResponseEntity<Any> {
        val result = apiService.restartContainer(client, id)
        return if (result)
            ResponseEntity(HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @GetMapping("/topContainer")
    fun topContainer(
        @RequestParam("id") id: String
    ): ResponseEntity<Any> {
        val result = apiService.topContainer(client, id)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @GetMapping("/events", produces = [MediaType.APPLICATION_NDJSON_VALUE])
    fun events(): Flow<Event> {
        val result = apiService.events(client)
        return result
    }

}