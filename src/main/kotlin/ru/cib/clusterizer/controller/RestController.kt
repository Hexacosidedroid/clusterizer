package ru.cib.clusterizer.controller

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

    @PostMapping("/setDockerConnection")
    fun setDockerConnection(@RequestBody config: DockerConfig) {
        val client = dockerApiService.connectToDocker(
            config.host,
            Registry(config.url, config.user, config.password),
            Tls(config.verify, config.certPath)
        )
        dockerApiService.pingDocker(client)
    }

    @GetMapping("/listOfImages")
    fun getListOfImages() {
    }
}