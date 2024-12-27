package ru.cib.clusterizer.service

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.PullImageResultCallback
import com.github.dockerjava.api.model.Image
import com.github.dockerjava.api.model.Info
import com.github.dockerjava.api.model.Version
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient
import org.springframework.stereotype.Service
import ru.cib.clusterizer.dao.docker.Registry
import ru.cib.clusterizer.dao.docker.Tls
import java.time.Duration
import java.util.concurrent.TimeUnit

@Service
class DockerApiService {

    fun connect(host: String, registry: Registry?, tls: Tls?): DockerClient {
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
    }

    fun ping(client: DockerClient?) {
        client?.pingCmd()?.exec()
    }

    fun info(client: DockerClient?): Info? = client?.infoCmd()?.exec()

    fun version(client: DockerClient?): Version? = client?.versionCmd()?.exec()

    fun listOfImages(client: DockerClient?): MutableList<Image>? = client?.listImagesCmd()?.exec()

    fun listOfContainers(client: DockerClient?) = client?.listContainersCmd()?.exec()

    fun pullImage(client: DockerClient?, image: ru.cib.clusterizer.dao.rest.Image): String {
        val result = client?.pullImageCmd(image.name)?.exec(PullImageResultCallback())?.awaitCompletion(30, TimeUnit.SECONDS)
        return if (result == true) {
            "Image ${image.name}:${image.tag} downloaded"
        } else {
            "Fail during download"
        }
    }
}