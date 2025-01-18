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
    fun getListOfImages() = dockerApiService.listOfImages(client)

    @GetMapping("/info")
    fun getInfo() = dockerApiService.info(client)

    @GetMapping("/listOfContainers")
    fun getListOfContainers() = dockerApiService.listOfContainers(client)

    @PostMapping("/pullImage")
    fun pullImageOnHost(@RequestBody request: ImageRequest) {
        val result = dockerApiService.pullImage(client, request)
        if (result == true)
            ResponseEntity<String>(HttpStatus.OK)
        else
            ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR)
    }
}