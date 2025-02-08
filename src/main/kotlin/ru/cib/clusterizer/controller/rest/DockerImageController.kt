package ru.cib.clusterizer.controller.rest

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.cib.clusterizer.dao.rest.ImageRequest
import ru.cib.clusterizer.domain.config.ConfigId
import ru.cib.clusterizer.domain.docker.DockerApiService

@RestController
@RequestMapping("api/docker/image")
class DockerImageController(
    private val apiServices: Map<ConfigId, DockerApiService>
) {

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
}