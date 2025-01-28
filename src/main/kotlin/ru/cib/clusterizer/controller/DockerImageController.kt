package ru.cib.clusterizer.controller

import com.github.dockerjava.api.model.PullResponseItem
import com.github.dockerjava.api.model.PushResponseItem
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.cib.clusterizer.dao.rest.ImageRequest
import ru.cib.clusterizer.domain.config.ConfigId
import ru.cib.clusterizer.domain.docker.DockerApiService

@CrossOrigin
@RestController
@RequestMapping("api/docker")
class DockerImageController(
    private val apiServices: Map<ConfigId, DockerApiService>
) {

    @PostMapping("/{configId}/image/pullImage", produces = [MediaType.APPLICATION_NDJSON_VALUE])
    suspend fun pullImage(
        @RequestBody request: ImageRequest,
        @PathVariable("configId") configId: ConfigId,
    ): Flow<PullResponseItem> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.pullImage(request)
        return result
    }

    @PostMapping("/{configId}/image/pushImage", produces = [MediaType.APPLICATION_NDJSON_VALUE])
    suspend fun pushImage(
        @RequestBody request: ImageRequest,
        @PathVariable("configId") configId: ConfigId
    ): Flow<PushResponseItem> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.pushImage(request)
        return result
    }

    @PostMapping("/{configId}/image/createImage")
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

    @PostMapping("/{configId}/image/loadImage")
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

    @GetMapping("/{configId}/image/searchImages")
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

    @DeleteMapping("/{configId}/image/removeImage")
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

    @GetMapping("/{configId}/image/listOfImages")
    fun getListOfImages(@PathVariable("configId") configId: ConfigId): ResponseEntity<Any> {
        val apiService = apiServices[configId]
            ?: throw RuntimeException("<b525a66c> Api service for $configId is not found")
        val result = apiService.listOfImages()
        return if (result != null)
            ResponseEntity(result, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @GetMapping("/{configId}/image/inspectImage")
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

    @PostMapping("/{configId}/image/saveImage")
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
}