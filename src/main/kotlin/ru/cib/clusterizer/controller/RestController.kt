package ru.cib.clusterizer.controller

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.Container
import com.github.dockerjava.api.model.Version
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController
import ru.cib.clusterizer.dao.docker.Registry
import ru.cib.clusterizer.dao.docker.Tls
import ru.cib.clusterizer.dao.rest.DockerConfigRequest
import ru.cib.clusterizer.dao.rest.ImageRequest
import ru.cib.clusterizer.service.DockerApiService

@RestController
class RestController(
    val dockerApiService: DockerApiService
) {
    var client: DockerClient? = null

    @PostMapping("/connect")
    fun setDockerConnection(@RequestBody config: DockerConfigRequest): Version? {
        client = dockerApiService.connect(
            config.host,
            Registry(config.url, config.user, config.password),
            Tls(config.verify, config.certPath)
        )
        dockerApiService.ping(client)
        return dockerApiService.version(client)
    }

    @GetMapping("/ping")
    fun getPing(): ResponseEntity<String> {
        val result = dockerApiService.ping(client)
        return if (result) {
            ResponseEntity(HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/info")
    fun getInfo(): ResponseEntity<Any> {
        val result = dockerApiService.info(client)
        return if (result != null) {
            ResponseEntity(result, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/version")
    fun getVersion(): ResponseEntity<Any> {
        val result = dockerApiService.version(client)
        return if (result != null) {
            ResponseEntity(result, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/pullImage")
    fun pullImage(@RequestBody request: ImageRequest): ResponseEntity<Any> {
        val result = dockerApiService.pullImage(client, request)
        return if (result == true)
            ResponseEntity(HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PostMapping("/pushImage")
    fun pushImage(@RequestBody request: ImageRequest): ResponseEntity<Any> {
        val result = dockerApiService.pushImage(client, request)
        return if (result == true)
            ResponseEntity(HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @GetMapping("/searchImages")
    fun searchImages(@RequestParam("search") term: String): ResponseEntity<Any> {
        val result = dockerApiService.searchImages(client, term)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @DeleteMapping("/removeImage")
    fun removeImage(@RequestParam("id") id: String): ResponseEntity<Any> {
        val result = dockerApiService.removeImage(client, id)
        return if (result)
            ResponseEntity(HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @GetMapping("/inspectImage")
    fun inspectImage(@RequestParam("id") id: String): ResponseEntity<Any> {
        val result = dockerApiService.inspectImage(client, id)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @GetMapping("/listOfImages")
    fun getListOfImages(): ResponseEntity<Any> {
        val result = dockerApiService.listOfImages(client)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @GetMapping("/listOfContainers")
    fun getListOfContainers(): ResponseEntity<Any> {
        val result = dockerApiService.listOfContainers(client)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PostMapping("/createContainer")
    fun createContainer(@RequestBody request: ImageRequest): ResponseEntity<Any> {
        val result = dockerApiService.createContainer(client, request)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @PostMapping("/startContainer")
    fun startContainer(@RequestParam("id") id: String): ResponseEntity<Any> {
        val result = dockerApiService.startContainer(client, id)
        return if (result)
            ResponseEntity(HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @GetMapping("/inspectContainer")
    fun inspectContainer(@RequestParam("id") id: String): ResponseEntity<Any> {
        val result = dockerApiService.inspectContainer(client, id)
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @DeleteMapping("/removeContainer")
    fun removeContainer(@RequestParam("id") id: String, @RequestParam("force") force: Boolean): ResponseEntity<Any> {
        val result = dockerApiService.removeContainer(client, id, force)
        return if (result)
            ResponseEntity(HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }
}