package ru.cib.clusterizer.service

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import org.springframework.stereotype.Service
import ru.cib.clusterizer.dao.docker.Registry
import ru.cib.clusterizer.dao.docker.Tls

@Service
class DockerApiService {

    fun connectToDocker(host: String, registry: Registry, tls: Tls): DockerClient {
        val dockerConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().apply {
            withDockerHost(host)
            withDockerTlsVerify(tls.verify)
            withDockerCertPath(tls.certPath)
            withRegistryUrl(registry.url)
            withRegistryUsername(registry.user)
            withRegistryPassword(registry.password)
        }.build()
        return DockerClientBuilder.getInstance(dockerConfig).build()
    }

    fun testDockerConnection(client: DockerClient) {
        client.pingCmd().exec()
    }
}