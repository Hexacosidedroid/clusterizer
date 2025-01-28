package ru.cib.clusterizer.domain.docker

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient
import java.nio.file.Path
import java.time.Duration


/*Methods for work with server*/
fun createDockerClient(config: DockerConfig): DockerClient = runCatching {
    val dockerConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().apply {
        when (config.target) {
            is DockerConfig.Target.LocalSocket -> {
                withDockerHost("unix://${config.target.path}")
            }

            is DockerConfig.Target.Remote -> {
                withDockerHost(config.target.host)
            }
        }

        config.tls?.let {
            withDockerTlsVerify(true)
            it.certPath?.toString()?.let(::withDockerCertPath)
        }

        config.registry?.let {
            withRegistryUrl(it.url)
            it.credentials?.let { credentials ->
                withRegistryUsername(credentials.username)
                withRegistryPassword(credentials.password)
            }
        }
    }.build()
    val httpClient = ZerodepDockerHttpClient.Builder().apply {
        dockerHost(dockerConfig.dockerHost)
        sslConfig(dockerConfig.sslConfig)
        maxConnections(100)
        connectionTimeout(Duration.ofSeconds(30))
        responseTimeout(Duration.ofSeconds(45))
    }.build()
    return DockerClientImpl.getInstance(dockerConfig, httpClient)
}.onFailure {
    dockerApiLogger.error("<7e35386f> Failed to create docker instant with config $config", it)
}.getOrThrow()

data class DockerConfig(
    val target: Target,
    val registry: Registry? = null,
    val tls: Tls? = null
) {
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    sealed interface Target {
        @JsonTypeName("local")
        data class LocalSocket(
            val path: String = "/var/run/docker.sock",
        ) : Target

        @JsonTypeName("remote")
        data class Remote(
            val host: String,
        ) : Target
    }

    data class Registry(
        val url: String,
        val credentials: Credentials?,
    ) {
        data class Credentials(
            val username: String,
            val password: String
        )
    }

    data class Tls(
        val certPath: Path? = null,
    )
}
