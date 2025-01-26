package ru.cib.clusterizer.service

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback.Adapter
import com.github.dockerjava.api.command.AsyncDockerCmd
import com.github.dockerjava.api.model.*
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.cib.clusterizer.dao.docker.DockerLogRecord
import ru.cib.clusterizer.dao.docker.Registry
import ru.cib.clusterizer.dao.docker.Tls
import ru.cib.clusterizer.dao.rest.ImageRequest
import java.io.InputStream
import java.nio.channels.ClosedByInterruptException
import java.time.Duration

private val logger = LoggerFactory.getLogger(DockerApiService::class.java)

private suspend fun <T> AsyncDockerCmd<*, T>.asFlow(
    logMarker: String,
    onNext: (T) -> Unit = {}
): Flow<T> = callbackFlow {
    val callback = object : Adapter<T>() {

        override fun onNext(item: T) {
            logger.info("$logMarker: OnNext: $item")
            onNext(item)
            trySend(item).isSuccess
        }

        override fun onError(throwable: Throwable) {
            when (throwable) {
                is CancellationException,
                is ClosedByInterruptException -> {
                    close()
                }

                else -> {
                    logger.error("$logMarker: OnError: ", throwable)
                    close(throwable)
                }
            }
        }

        override fun onComplete() {
            logger.info("$logMarker: completed")
            close()
        }
    }

    exec(callback)

    awaitClose {
        try {
            logger.info("$logMarker: await close")
            callback.close()
        } catch (e: Exception) {
            logger.error("$logMarker: Error while closing callback: ", e)
        }
    }
}

@Service
class DockerApiService {

    /*Methods for work with server*/
    fun connect(host: String?, registry: Registry?, tls: Tls?): DockerClient {
        try {
            val dockerConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().apply {
                host?.let(::withDockerHost)
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
            throw e
        }
    }

    fun ping(client: DockerClient) = try {
        client.pingCmd()?.exec()
        true
    } catch (e: Exception) {
        logger.error("Error server is unreachable ", e)
        false
    }

    fun info(client: DockerClient) = try {
        client.infoCmd()?.exec()
    } catch (e: Exception) {
        logger.error("Error server is unreachable ", e)
        throw e
    }

    fun version(client: DockerClient) = try {
        client.versionCmd()?.exec()
    } catch (e: Exception) {
        logger.error("Error server is unreachable ", e)
        throw e
    }

    /*Methods for work with images on host*/

    suspend fun pullImage(client: DockerClient, request: ImageRequest): Flow<PullResponseItem> = runCatching {
        val pullCmd = client.pullImageCmd("${request.name}:${request.tag}")
        pullCmd.asFlow("Pull Image CMD") { item ->
            logger.info(item.status)
        }.flowOn(Dispatchers.IO)
    }.onFailure {
        logger.error("Failed to pull image ${request.name}:${request.tag} ", it)
    }.getOrThrow()


    suspend fun pushImage(client: DockerClient, request: ImageRequest): Flow<PushResponseItem> = runCatching {
        val pushCmd = client.pushImageCmd("${request.name}:${request.tag}")
        pushCmd.asFlow<PushResponseItem>("Push Image CMD") { item ->
            logger.info(item.status)
        }.flowOn(Dispatchers.IO)
    }.onFailure {
        logger.error("Failed to push image ${request.name}:${request.tag} ", it)
    }.getOrThrow()

    fun createImage(client: DockerClient, repo: String, inputStream: InputStream) = try {
        client.createImageCmd(repo, inputStream).exec()
    } catch (e: Exception) {
        logger.error("Failed to create image ", e)
        throw e
    }

    fun loadImage(client: DockerClient, inputStream: InputStream) = try {
        client.loadImageCmd(inputStream)
    } catch (e: Exception) {
        logger.error("Failed to load image ", e)
        throw e
    }

    fun searchImages(client: DockerClient, term: String) = try {
        client.searchImagesCmd(term)?.exec()
    } catch (e: Exception) {
        logger.error("Failed to search images for request term $term ", e)
        throw e
    }

    fun removeImage(client: DockerClient, id: String) = try {
        client.removeImageCmd(id)?.exec()
        true
    } catch (e: Exception) {
        logger.error("Failed to remove image by id $id ", e)
        false
    }

    fun listOfImages(client: DockerClient): MutableList<Image>? = try {
        client.listImagesCmd()?.exec()
    } catch (e: Exception) {
        logger.error("Failed to load list of images ", e)
        throw e
    }

    fun inspectImage(client: DockerClient, id: String) = try {
        client.inspectImageCmd(id).exec()
    } catch (e: Exception) {
        logger.error("Failed to inspect image $id ", e)
        throw e
    }

    fun saveImage(client: DockerClient, request: ImageRequest) = try {
        client.saveImageCmd(request.name).withTag(request.tag).exec()
    } catch (e: Exception) {
        logger.error("Failed to save image ${request.name}:${request.tag} ", e)
        throw e
    }

    /*Methods for work with containers*/

    fun listOfContainers(client: DockerClient, all: Boolean) = try {
        client.listContainersCmd().withShowAll(all).exec()
    } catch (e: Exception) {
        logger.error("Failed to load list of containers ", e)
        throw e
    }

    fun createContainer(client: DockerClient, request: ImageRequest) = try {
        client.createContainerCmd("${request.name}:${request.tag}").exec()
    } catch (e: Exception) {
        logger.error("Failed to create container", e)
        throw e
    }

    fun startContainer(client: DockerClient, id: String) = try {
        client.startContainerCmd(id).exec()
        true
    } catch (e: Exception) {
        logger.error("Failed to start container $id", e)
        false
    }

    fun execCreate(client: DockerClient, id: String) = try {
        client.execCreateCmd(id).exec()
    } catch (e: Exception) {
        logger.error("Failed to exec create $id", e)
        throw e
    }

    fun resizeExec(client: DockerClient, id: String) = try {
        client.resizeExecCmd(id).exec() //TODO add size
        true
    } catch (e: Exception) {
        logger.error("Failed to resize container $id", e)
        throw e
    }

    fun inspectContainer(client: DockerClient, id: String) = try {
        client.inspectContainerCmd(id).exec()
    } catch (e: Exception) {
        logger.error("Failed to inspect container $id", e)
        throw e
    }

    fun removeContainer(client: DockerClient, id: String, force: Boolean) = try {
        client.removeContainerCmd(id).withForce(force).exec()
        true
    } catch (e: Exception) {
        logger.error("Failed to remove container $id", e)
        false
    }

    suspend fun waitContainer(client: DockerClient, id: String): Flow<WaitResponse> = runCatching {
        val waitCmd = client.waitContainerCmd(id)
        waitCmd.asFlow<WaitResponse>("Wait container") { item ->
            logger.info("${item.statusCode}")
        }.flowOn(Dispatchers.IO)
    }.onFailure {
        logger.error("Failed to wait container $id: ", it)
    }.getOrThrow()

    suspend fun logContainer(client: DockerClient, id: String, follow: Boolean, tail: Int?) = coroutineScope {
        runCatching {
            val logCmd = client.logContainerCmd(id).apply {
                withStdOut(true)
                withStdErr(true)
                withFollowStream(follow)
                tail?.let(::withTail)
            }

            logCmd.asFlow<Frame>("Log container $id")
                .flowOn(Dispatchers.IO)
                .map {
                    DockerLogRecord(
                        type = it.streamType,
                        payload = it.payload.decodeToString()
                    )
                }
                .onEach { logger.info("Log Record from $id: $it") }
        }.onFailure { logger.error("Failed to log container $id: ", it) }.getOrThrow()
    }

    fun diffContainer(client: DockerClient, id: String) = try {
        client.containerDiffCmd(id).exec()
    } catch (e: Exception) {
        logger.error("Failed to make diff for container $id", e)
    }

    fun stopContainer(client: DockerClient, id: String) = try {
        client.stopContainerCmd(id).exec()
        true
    } catch (e: Exception) {
        logger.error("Failed to stop container $id", e)
        false
    }

    fun killContainer(client: DockerClient, id: String) = try {
        client.killContainerCmd(id).exec()
        true
    } catch (e: Exception) {
        logger.error("Failed to kill container $id", e)
        false
    }

    fun updateContainer(client: DockerClient, id: String) = try {
        client.createContainerCmd(id).exec()
    } catch (e: Exception) {
        logger.error("Failed to update container $id", e)
        throw e
    }

    fun renameContainer(client: DockerClient, id: String, name: String) = try {
        client.renameContainerCmd(id).withName(name).exec()
        true
    } catch (e: Exception) {
        logger.error("Failed to rename container $id", e)
        false
    }

    fun restartContainer(client: DockerClient, id: String) = try {
        client.restartContainerCmd(id).exec()
        true
    } catch (e: Exception) {
        logger.error("Failed to restart container $id", e)
        false
    }

    fun resizeContainer(client: DockerClient, id: String) = try {
        client.resizeContainerCmd(id).exec()
        true
    } catch (e: Exception) {
        logger.error("Failed to resize container $id", e)
        false
    }

    fun topContainer(client: DockerClient, id: String) = try {
        client.topContainerCmd(id).exec()
    } catch (e: Exception) {
        logger.error("Failed to execute top in container $id", e)
        throw e
    }

    fun pauseContainer(client: DockerClient, id: String) = try {
        client.pauseContainerCmd(id).exec()
        true
    } catch (e: Exception) {
        logger.error("Failed to pause container $id", e)
        false
    }

    fun unpauseContainer(client: DockerClient, id: String) = try {
        client.unpauseContainerCmd(id).exec()
        true
    } catch (e: Exception) {
        logger.error("Failed to unpause container $id", e)
        false
    }

    suspend fun events(client: DockerClient) = coroutineScope {
        runCatching {
            val eventsCmd = client.eventsCmd()
            eventsCmd.asFlow<Event>("Host events") { item -> logger.info("$item") }
                .flowOn(Dispatchers.IO)
        }.onFailure { logger.error("Failed to fetch events: ", it) }.getOrThrow()
    }

    /*Swarm methods*/
    fun inspectSwarm(client: DockerClient) = try {
        client.inspectSwarmCmd().exec()
    } catch (e: Exception) {
        logger.error("Failed to inspect swarm", e)
        throw e
    }

//    fun joinSwarm(client: DockerClient)
}