package ru.cib.clusterizer.domain.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback.Adapter
import com.github.dockerjava.api.command.AsyncDockerCmd
import com.github.dockerjava.api.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import ru.cib.clusterizer.dao.rest.ImageRequest
import java.io.InputStream
import java.nio.channels.ClosedByInterruptException

val dockerApiLogger = LoggerFactory.getLogger(DockerApiService::class.java)

class DockerApiService(
    val client: DockerClient
) {

    fun ping() = try {
        client.pingCmd().exec()
        true
    } catch (e: Exception) {
        dockerApiLogger.error("Error server is unreachable ", e)
        false
    }

    fun info() = try {
        client.infoCmd()?.exec()
    } catch (e: Exception) {
        dockerApiLogger.error("Error server is unreachable ", e)
        throw e
    }

    fun version() = try {
        client.versionCmd()?.exec()
    } catch (e: Exception) {
        dockerApiLogger.error("Error server is unreachable ", e)
        throw e
    }

    /*Methods for work with images on host*/

    suspend fun pullImage(request: ImageRequest): Flow<PullResponseItem> = runCatching {
        val pullCmd = client.pullImageCmd("${request.name}:${request.tag}")
        pullCmd.asFlow("Pull Image CMD") { item ->
            dockerApiLogger.info(item.status)
        }.flowOn(Dispatchers.IO)
    }.onFailure {
        dockerApiLogger.error("Failed to pull image ${request.name}:${request.tag} ", it)
    }.getOrThrow()


    suspend fun pushImage(request: ImageRequest): Flow<PushResponseItem> = runCatching {
        val pushCmd = client.pushImageCmd("${request.name}:${request.tag}")
        pushCmd.asFlow<PushResponseItem>("Push Image CMD") { item ->
            dockerApiLogger.info(item.status)
        }.flowOn(Dispatchers.IO)
    }.onFailure {
        dockerApiLogger.error("Failed to push image ${request.name}:${request.tag} ", it)
    }.getOrThrow()

    fun createImage(repo: String, inputStream: InputStream) = try {
        client.createImageCmd(repo, inputStream).exec()
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to create image ", e)
        throw e
    }

    fun loadImage(inputStream: InputStream) = try {
        client.loadImageCmd(inputStream)
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to load image ", e)
        throw e
    }

    fun searchImages(term: String) = try {
        client.searchImagesCmd(term)?.exec()
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to search images for request term $term ", e)
        throw e
    }

    fun removeImage(id: String) = try {
        client.removeImageCmd(id)?.exec()
        true
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to remove image by id $id ", e)
        false
    }

    fun listOfImages(): MutableList<Image>? = try {
        client.listImagesCmd()?.exec()
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to load list of images ", e)
        throw e
    }

    fun inspectImage(id: String) = try {
        client.inspectImageCmd(id).exec()
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to inspect image $id ", e)
        throw e
    }

    fun saveImage(request: ImageRequest) = try {
        client.saveImageCmd(request.name).withTag(request.tag).exec()
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to save image ${request.name}:${request.tag} ", e)
        throw e
    }

    /*Methods for work with containers*/

    fun listOfContainers(all: Boolean) = try {
        client.listContainersCmd().withShowAll(all).exec()
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to load list of containers ", e)
        throw e
    }

    fun createContainer(request: ImageRequest) = try {
        client.createContainerCmd("${request.name}:${request.tag}").exec()
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to create container", e)
        throw e
    }

    fun startContainer(id: String) = try {
        client.startContainerCmd(id).exec()
        true
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to start container $id", e)
        false
    }

    fun execCreate(id: String) = try {
        client.execCreateCmd(id).exec()
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to exec create $id", e)
        throw e
    }

    fun resizeExec(id: String) = try {
        client.resizeExecCmd(id).exec() //TODO add size
        true
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to resize container $id", e)
        throw e
    }

    fun inspectContainer(id: String) = try {
        client.inspectContainerCmd(id).exec()
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to inspect container $id", e)
        throw e
    }

    fun removeContainer(id: String, force: Boolean) = try {
        client.removeContainerCmd(id).withForce(force).exec()
        true
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to remove container $id", e)
        false
    }

    suspend fun waitContainer(id: String): Flow<WaitResponse> = runCatching {
        val waitCmd = client.waitContainerCmd(id)
        waitCmd.asFlow<WaitResponse>("Wait container") { item ->
            dockerApiLogger.info("${item.statusCode}")
        }.flowOn(Dispatchers.IO)
    }.onFailure {
        dockerApiLogger.error("Failed to wait container $id: ", it)
    }.getOrThrow()

    suspend fun logContainer(id: String, follow: Boolean, tail: Int?) = coroutineScope {
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
                .onEach { dockerApiLogger.info("Log Record from $id: $it") }
        }.onFailure { dockerApiLogger.error("Failed to log container $id: ", it) }.getOrThrow()
    }

    fun diffContainer(id: String) = try {
        client.containerDiffCmd(id).exec()
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to make diff for container $id", e)
    }

    fun stopContainer(id: String) = try {
        client.stopContainerCmd(id).exec()
        true
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to stop container $id", e)
        false
    }

    fun killContainer(id: String) = try {
        client.killContainerCmd(id).exec()
        true
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to kill container $id", e)
        false
    }

    fun updateContainer(id: String) = try {
        client.createContainerCmd(id).exec()
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to update container $id", e)
        throw e
    }

    fun renameContainer(id: String, name: String) = try {
        client.renameContainerCmd(id).withName(name).exec()
        true
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to rename container $id", e)
        false
    }

    fun restartContainer(id: String) = try {
        client.restartContainerCmd(id).exec()
        true
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to restart container $id", e)
        false
    }

    fun resizeContainer(id: String) = try {
        client.resizeContainerCmd(id).exec()
        true
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to resize container $id", e)
        false
    }

    fun topContainer(id: String) = try {
        client.topContainerCmd(id).exec()
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to execute top in container $id", e)
        throw e
    }

    fun pauseContainer(id: String) = try {
        client.pauseContainerCmd(id).exec()
        true
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to pause container $id", e)
        false
    }

    fun unpauseContainer(id: String) = try {
        client.unpauseContainerCmd(id).exec()
        true
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to unpause container $id", e)
        false
    }

    suspend fun events() = coroutineScope {
        runCatching {
            val eventsCmd = client.eventsCmd()
            eventsCmd.asFlow<Event>("Host events") { item -> dockerApiLogger.info("$item") }
                .flowOn(Dispatchers.IO)
        }.onFailure { dockerApiLogger.error("Failed to fetch events: ", it) }.getOrThrow()
    }

    /*Swarm methods*/
    fun inspectSwarm() = try {
        client.inspectSwarmCmd().exec()
    } catch (e: Exception) {
        dockerApiLogger.error("Failed to inspect swarm", e)
        throw e
    }

//    fun joinSwarm()
}

data class DockerLogRecord(
    val type: StreamType,
    val payload: String,
)

private suspend fun <T> AsyncDockerCmd<*, T>.asFlow(
    logMarker: String,
    onNext: (T) -> Unit = {}
): Flow<T> = callbackFlow {
    val callback = object : Adapter<T>() {

        override fun onNext(item: T) {
            dockerApiLogger.info("$logMarker: OnNext: $item")
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
                    dockerApiLogger.error("$logMarker: OnError: ", throwable)
                    close(throwable)
                }
            }
        }

        override fun onComplete() {
            dockerApiLogger.info("$logMarker: completed")
            close()
        }
    }

    exec(callback)

    awaitClose {
        try {
            dockerApiLogger.info("$logMarker: await close")
            callback.close()
        } catch (e: Exception) {
            dockerApiLogger.error("$logMarker: Error while closing callback: ", e)
        }
    }
}
