package ru.cib.clusterizer.service

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback.Adapter
import com.github.dockerjava.api.command.PullImageCmd
import com.github.dockerjava.api.command.PushImageCmd
import com.github.dockerjava.api.command.WaitContainerCmd
import com.github.dockerjava.api.model.Image
import com.github.dockerjava.api.model.PullResponseItem
import com.github.dockerjava.api.model.PushResponseItem
import com.github.dockerjava.api.model.WaitResponse
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.cib.clusterizer.dao.docker.Registry
import ru.cib.clusterizer.dao.docker.Tls
import ru.cib.clusterizer.dao.rest.ImageRequest
import java.io.InputStream
import java.time.Duration

private val logger = LoggerFactory.getLogger(DockerApiService::class.java)

private suspend fun <T, R> T.execWithCoroutine(
    exec: T.(Adapter<R>) -> Unit,
    onNext: (R) -> Unit = {},
    log: String
) = suspendCancellableCoroutine { cont ->
    val callback = object : Adapter<R>() {
        override fun onNext(item: R) {
            logger.info("$log: $item")
            onNext(item)
            cont.resumeWith(Result.success(item))
        }

        override fun onError(throwable: Throwable) {
            logger.error("Error $log $throwable")
            if (!cont.isCompleted) {
                cont.resumeWith(Result.failure(throwable))
            }
        }

        override fun onComplete() {
            logger.info("Image $log completed")
            if (!cont.isCompleted) {
                cont.resumeWith(Result.success(Unit))
            }
        }
    }
    exec(callback)
    cont.invokeOnCancellation {
        try {
            callback.close()
        } catch (e: Exception) {
            logger.error("Error closing callback: $e")
        }
    }
}

@Service
class DockerApiService {

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
            throw e
        }
    }

    fun ping(client: DockerClient?) = try {
        client?.pingCmd()?.exec()
        true
    } catch (e: Exception) {
        logger.error("Error server is unreachable ", e)
        false
    }

    fun info(client: DockerClient?) = try {
        client?.infoCmd()?.exec()
    } catch (e: Exception) {
        logger.error("Error server is unreachable ", e)
        throw e
    }

    fun version(client: DockerClient?) = try {
        client?.versionCmd()?.exec()
    } catch (e: Exception) {
        logger.error("Error server is unreachable ", e)
        throw e
    }

    /*Methods for work with images on host*/

    suspend fun pullImage(client: DockerClient?, request: ImageRequest) = try {
        withContext(Dispatchers.IO) {
            val pullCmd = client?.pullImageCmd("${request.name}:${request.tag}")
            pullCmd.let {
                it?.execWithCoroutine<PullImageCmd, PullResponseItem>(
                    exec = { callback -> this.exec(callback) },
                    onNext = { item -> logger.info(item.status) },
                    log = "pull image"
                )
            }
        }
        true
    } catch (e: Exception) {
        logger.error("Failed to pull image ${request.name}:${request.tag} ", e)
        false
    }

    suspend fun pushImage(client: DockerClient?, request: ImageRequest) = try {
        withContext(Dispatchers.IO) {
            val pushCmd = client?.pushImageCmd("${request.name}:${request.tag}")
            pushCmd.let {
                it?.execWithCoroutine<PushImageCmd, PushResponseItem>(
                    exec = { callback -> this.exec(callback) },
                    onNext = { item -> logger.info(item.status) },
                    log = "push image"
                )
            }
        }
        true
    } catch (e: Exception) {
        logger.error("Failed to push image ${request.name}:${request.tag} ", e)
        false
    }

    fun createImage(client: DockerClient?, repo: String, inputStream: InputStream) = try {
        client?.createImageCmd(repo, inputStream)?.exec()
    } catch (e: Exception) {
        logger.error("Failed to create image ", e)
        throw e
    }

    fun loadImage(client: DockerClient?, inputStream: InputStream) = try {
        client?.loadImageCmd(inputStream)
    } catch (e: Exception) {
        logger.error("Failed to load image ", e)
        throw e
    }

    fun searchImages(client: DockerClient?, term: String) = try {
        client?.searchImagesCmd(term)?.exec()
    } catch (e: Exception) {
        logger.error("Failed to search images for request term $term ", e)
        throw e
    }

    fun removeImage(client: DockerClient?, id: String) = try {
        client?.removeImageCmd(id)?.exec()
        true
    } catch (e: Exception) {
        logger.error("Failed to remove image by id $id ", e)
        false
    }

    fun listOfImages(client: DockerClient?): MutableList<Image>? = try {
        client?.listImagesCmd()?.exec()
    } catch (e: Exception) {
        logger.error("Failed to load list of images ", e)
        throw e
    }

    fun inspectImage(client: DockerClient?, id: String) = try {
        client?.inspectImageCmd(id)?.exec()
    } catch (e: Exception) {
        logger.error("Failed to inspect image $id ", e)
        throw e
    }

    fun saveImage(client: DockerClient?, request: ImageRequest) = try {
        client?.saveImageCmd(request.name)?.withTag(request.tag)?.exec()
    } catch (e: Exception) {
        logger.error("Failed to save image ${request.name}:${request.tag} ", e)
        throw e
    }

    /*Methods for work with containers*/

    fun listOfContainers(client: DockerClient?, all: Boolean) = try {
        client?.listContainersCmd()?.withShowAll(all)?.exec()
    } catch (e: Exception) {
        logger.error("Failed to load list of containers ", e)
        throw e
    }

    fun createContainer(client: DockerClient?, request: ImageRequest) = try {
        client?.createContainerCmd("${request.name}:${request.tag}")?.exec()
    } catch (e: Exception) {
        logger.error("Failed to create container", e)
        throw e
    }

    fun startContainer(client: DockerClient?, id: String) = try {
        client?.startContainerCmd(id)?.exec()
        true
    } catch (e: Exception) {
        logger.error("Failed to start container $id", e)
        false
    }

    fun execCreate(client: DockerClient?, id: String) = try {
        client?.execCreateCmd(id)?.exec()
    } catch (e: Exception) {
        logger.error("Failed to exec create $id", e)
        throw e
    }

    fun resizeExec(client: DockerClient?, id: String) = try {
        client?.resizeExecCmd(id)?.exec() //TODO add size
        true
    } catch (e: Exception) {
        logger.error("Failed to resize container $id", e)
        throw e
    }

    fun inspectContainer(client: DockerClient?, id: String) = try {
        client?.inspectContainerCmd(id)?.exec()
    } catch (e: Exception) {
        logger.error("Failed to inspect container $id", e)
        throw e
    }

    fun removeContainer(client: DockerClient?, id: String, force: Boolean) = try {
        client?.removeContainerCmd(id)?.withForce(force)?.exec()
        true
    } catch (e: Exception) {
        logger.error("Failed to remove container $id", e)
        false
    }

    suspend fun waitContainer(client: DockerClient?, id: String): Flow<WaitResponse> = flow {
        try {
            withContext(Dispatchers.IO) {
                val waitCmd = client?.waitContainerCmd(id)
                waitCmd?.let {
                    val response = it.execWithCoroutine<WaitContainerCmd, WaitResponse>(
                        exec = { callback -> this.exec(callback) },
                        onNext = {  },
                        log = "wait container"
                    )
                    emit(response)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to wait container $id", e)
            throw e
        }
    }

    fun diffContainer(client: DockerClient?, id: String) = try {
        client?.containerDiffCmd(id)?.exec()
    } catch (e: Exception) {
        logger.error("Failed to make diff for container $id", e)
    }

    fun stopContainer(client: DockerClient?, id: String) = try {
        client?.stopContainerCmd(id)?.exec()
        true
    } catch (e: Exception) {
        logger.error("Failed to stop container $id", e)
        false
    }

    fun killContainer(client: DockerClient?, id: String) = try {
        client?.killContainerCmd(id)?.exec()
        true
    } catch (e: Exception) {
        logger.error("Failed to kill container $id", e)
        false
    }

    fun updateContainer(client: DockerClient?, id: String) = try {
        client?.createContainerCmd(id)?.exec()
    } catch (e: Exception) {
        logger.error("Failed to update container $id", e)
        throw e
    }

    fun renameContainer(client: DockerClient?, id: String, name: String) = try {
        client?.renameContainerCmd(id)?.withName(name)?.exec()
        true
    } catch (e: Exception) {
        logger.error("Failed to rename container $id", e)
        false
    }

    fun restartContainer(client: DockerClient?, id: String) = try {
        client?.restartContainerCmd(id)?.exec()
        true
    } catch (e: Exception) {
        logger.error("Failed to restart container $id", e)
        false
    }

    fun resizeContainer(client: DockerClient?, id: String) = try {
        client?.resizeContainerCmd(id)?.exec()
        true
    } catch (e: Exception) {
        logger.error("Failed to resize container $id", e)
        false
    }

    fun topContainer(client: DockerClient?, id: String) = try {
        client?.topContainerCmd(id)?.exec()
    } catch (e: Exception) {
        logger.error("Failed to execute top in container $id", e)
        throw e
    }

    fun pauseContainer(client: DockerClient?, id: String) = try {
        client?.pauseContainerCmd(id)?.exec()
        true
    } catch (e: Exception) {
        logger.error("Failed to pause container $id", e)
        false
    }

    fun unpauseContainer(client: DockerClient?, id: String) = try {
        client?.unpauseContainerCmd(id)?.exec()
        true
    } catch (e: Exception) {
        logger.error("Failed to unpause container $id", e)
        false
    }

    fun events(client: DockerClient?) = try {
        val result = client?.eventsCmd()?.start()
    } catch (e: Exception) {
        logger.error("Failed to get events from server", e)
        throw e
    }

    /*Swarm methods*/
    fun inspectSwarm(client: DockerClient?) = try {
        client?.inspectSwarmCmd()?.exec()
    } catch (e: Exception) {
        logger.error("Failed to inspect swarm", e)
        throw e
    }

//    fun joinSwarm(client: DockerClient?)
}