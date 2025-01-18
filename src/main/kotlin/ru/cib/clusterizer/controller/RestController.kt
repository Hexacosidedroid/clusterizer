package ru.cib.clusterizer.controller

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.Version
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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

    @GetMapping("/listOfImages")
    fun getListOfImages() = ResponseEntity(dockerApiService.listOfImages(client), HttpStatus.OK)

    @GetMapping("/info")
    fun getInfo() = ResponseEntity(dockerApiService.info(client), HttpStatus.OK)

    @GetMapping("/listOfContainers")
    fun getListOfContainers() = ResponseEntity(dockerApiService.listOfContainers(client), HttpStatus.OK)

    @PostMapping("/pullImage")
    fun pullImageOnHost(@RequestBody request: ImageRequest): ResponseEntity<Any> {
        val result = dockerApiService.pullImage(client, request)
        return if (result == true)
            ResponseEntity<Any>(HttpStatus.OK)
        else
            ResponseEntity<Any>(HttpStatus.INTERNAL_SERVER_ERROR)
    }
}