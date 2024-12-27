package ru.cib.clusterizer.controller

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.Image
import com.github.dockerjava.api.model.Info
import com.github.dockerjava.api.model.Version
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import ru.cib.clusterizer.dao.docker.Registry
import ru.cib.clusterizer.dao.docker.Tls
import ru.cib.clusterizer.dao.rest.DockerConfig
import ru.cib.clusterizer.service.DockerApiService

@RestController
class RestController(
    val dockerApiService: DockerApiService
) {
    var client: DockerClient? = null

    @PostMapping("/connect")
    fun setDockerConnection(@RequestBody config: DockerConfig): Version? {
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
}