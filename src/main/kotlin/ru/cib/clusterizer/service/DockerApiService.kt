package ru.cib.clusterizer.service

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import org.springframework.stereotype.Service
import ru.cib.clusterizer.dao.docker.Registry
import ru.cib.clusterizer.dao.docker.Tls
import java.time.Duration

@Service
class DockerApiService {

    fun connectToDocker(host: String, registry: Registry?, tls: Tls?): DockerClient {
        val dockerConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().apply {
            withDockerHost(host)
            withDockerTlsVerify(tls?.verify)
            withDockerCertPath(tls?.certPath)
            withRegistryUrl(registry?.url)
            withRegistryUsername(registry?.user)
            withRegistryPassword(registry?.password)
        }.build()
        val httpClient = ApacheDockerHttpClient.Builder().apply {
            dockerHost(dockerConfig.dockerHost)
            sslConfig(dockerConfig.sslConfig)
            maxConnections(100)
            connectionTimeout(Duration.ofSeconds(30))
            responseTimeout(Duration.ofSeconds(45))
        }.build()
        return DockerClientImpl.getInstance(dockerConfig, httpClient)
    }

    fun pingDocker(client: DockerClient) {
        client.pingCmd().exec()
    }

    fun listOfDockerImages(client: DockerClient) {
        val listOfImages = client.listImagesCmd().exec()
//        val list = listOfImages.map { rawImage ->
//            Image(
//                created = rawImage.created
//
//            )
//        }.toMutableList()
    }
}