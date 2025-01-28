package ru.cib.clusterizer.config.docker

import org.springframework.boot.context.properties.ConfigurationProperties
import ru.cib.clusterizer.domain.docker.DockerConfig

@ConfigurationProperties(prefix = "docker")
data class DockerConfigurationProperties(
    val connections: Map<String, Any>
)