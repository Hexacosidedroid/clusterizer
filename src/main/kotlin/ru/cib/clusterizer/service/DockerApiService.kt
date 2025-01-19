package ru.cib.clusterizer.service

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.async.ResultCallback.Adapter
import com.github.dockerjava.api.async.ResultCallbackTemplate
import com.github.dockerjava.api.command.PullImageResultCallback
import com.github.dockerjava.api.model.Image
import com.github.dockerjava.api.model.Info
import com.github.dockerjava.api.model.Version
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.core.command.PushImageResultCallback
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.cib.clusterizer.dao.docker.Registry
import ru.cib.clusterizer.dao.docker.Tls
import ru.cib.clusterizer.dao.rest.ContainerIdRequest
import ru.cib.clusterizer.dao.rest.ImageRequest
import java.time.Duration
import java.util.concurrent.TimeUnit

@Service
class DockerApiService {

    private val logger = LoggerFactory.getLogger(DockerApiService::class.java)

    /*Methods for work with server*/
    fun connect(host: String, registry: Registry?, tls: Tls?): DockerClient {
        try {
            val dockerConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().apply {
                withDockerHost(host)
                withDockerTlsVerify(tls?.verify)
                withDockerCertPath(tls?.certPath)
                withRegistryUrl(registry?.url)
                withRegistryUsername(registry?.user)
                withRegistryPassword(registry?.password)
            }.build()
            val httpClient = ZerodepDockerHttpClient.Builder().apply {
                dockerHost(dockerConfig.dockerHost)
                sslConfig(dockerConfig.sslConfig)
                maxConnections(100)
                connectionTimeout(Duration.ofSeconds(30))
                responseTimeout(Duration.ofSeconds(45))
            }.build()
            return DockerClientImpl.getInstance(dockerConfig, httpClient)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun ping(client: DockerClient?) = try {
        client?.pingCmd()?.exec()
        true
    } catch (e: Exception) {
        logger.error("Error server is unreachable", e)
        false
    }

    fun info(client: DockerClient?) = try {
        client?.infoCmd()?.exec()
    } catch (e: Exception) {
        logger.error("Error server is unreachable", e)
        throw RuntimeException(e)
    }

    fun version(client: DockerClient?) = try {
        client?.versionCmd()?.exec()
    } catch (e: Exception) {
        logger.error("Error server is unreachable", e)
        throw RuntimeException(e)
    }

    /* Methods for work with images on host */

    fun pullImage(client: DockerClient?, imageRequest: ImageRequest) = try {
        client?.pullImageCmd("${imageRequest.name}:${imageRequest.tag}")?.exec(PullImageResultCallback())
            ?.awaitCompletion(30, TimeUnit.SECONDS)
    } catch (e: Exception) {
        logger.error("Failed to pull image ${imageRequest.name}:${imageRequest.tag}", e)
        false
    }

    fun pushImage(client: DockerClient?, imageRequest: ImageRequest) = try {
        client?.pushImageCmd("${imageRequest.name}:${imageRequest.tag}")?.exec(PushImageResultCallback())
            ?.awaitCompletion(30, TimeUnit.SECONDS)
    } catch (e: Exception) {
        logger.error("Failed to push image ${imageRequest.name}:${imageRequest.tag}", e)
        false
    }

    fun searchImages(client: DockerClient?, term: String) = try {
        client?.searchImagesCmd(term)?.exec()
    } catch (e: Exception) {
        logger.error("Failed to search images for request term $term", e)
        throw RuntimeException(e)
    }

    fun removeImage(client: DockerClient?, id: String) = try {
        client?.removeImageCmd(id)?.exec()
        true
    } catch (e: Exception) {
        logger.error("Failed to remove image by id $id", e)
        false
    }

    fun listOfImages(client: DockerClient?): MutableList<Image>? = try {
        client?.listImagesCmd()?.exec()
    } catch (e: Exception) {
        logger.error("Failed to load list of images", e)
        throw RuntimeException(e)
    }

    fun inspectImage(client: DockerClient?, id: String) = try {
        client?.inspectImageCmd(id)?.exec()
    } catch (e: Exception) {
        logger.error("Failed to inspect image $id", e)
        throw RuntimeException(e)
    }

    /*Methods for work with containers */

    fun listOfContainers(client: DockerClient?) = try {
        client?.listContainersCmd()?.exec()
    } catch (e: Exception) {
        logger.error("Failed to load list of containers", e)
        throw RuntimeException(e)
    }

    fun createContainer(client: DockerClient?, imageRequest: ImageRequest) = try {
        client?.createContainerCmd(imageRequest.name)?.exec()
    } catch (e: Exception) {
        logger.error("Failed to create container", e)
        throw RuntimeException(e)
    }

    fun startContainer(client: DockerClient?, containerId: ContainerIdRequest) = try {
        client?.startContainerCmd(containerId.id)?.exec()
        true
    } catch (e: Exception) {
        logger.error("Failed to start container", e)
        false
    }

    fun inspectContainer(client: DockerClient?, containerId: ContainerIdRequest) = try {
        client?.inspectContainerCmd(containerId.id)?.exec()
    } catch (e: Exception) {
        logger.error("Failed to inspect container", e)
        throw RuntimeException(e)
    }

    fun removeContainer(client: DockerClient?, containerId: ContainerIdRequest) = try {
        client?.removeContainerCmd(containerId.id)?.exec()
        true
    } catch (e: Exception) {
        logger.error("Failed to remove container", e)
        false
    }
}