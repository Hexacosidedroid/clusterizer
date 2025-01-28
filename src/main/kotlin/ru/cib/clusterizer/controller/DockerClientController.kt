package ru.cib.clusterizer.controller

import com.github.dockerjava.api.model.*
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController
import ru.cib.clusterizer.domain.config.ConfigId
import ru.cib.clusterizer.domain.docker.DockerApiService

@CrossOrigin
@RestController
@RequestMapping("api/docker")
class DockerClientController(
    private val apiServices: Map<ConfigId, DockerApiService>
) {

    @GetMapping("/{configId}/client/ping")
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

    @GetMapping("/{configId}/client/info")
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

    @GetMapping("/{configId}/client/version")
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

    @GetMapping("/{configId}/client/events", produces = [MediaType.APPLICATION_NDJSON_VALUE])
    suspend fun events(@PathVariable("configId") configId: ConfigId): Flow<Event> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.events()
        return result
    }

}