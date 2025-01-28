package ru.cib.clusterizer.config.docker

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.cib.clusterizer.domain.config.ConfigId
import ru.cib.clusterizer.domain.config.DockerConfigRepository
import ru.cib.clusterizer.domain.config.StaticDockerConfigRepository
import ru.cib.clusterizer.domain.docker.DockerApiService
import ru.cib.clusterizer.domain.docker.DockerConfig
import ru.cib.clusterizer.domain.docker.createDockerClient

@Configuration
@EnableConfigurationProperties(DockerConfigurationProperties::class)
class DockerServicesConfig {
    @Bean
    fun apiServices(
        dockerConfigRepositories: List<DockerConfigRepository>
    ): Map<ConfigId, DockerApiService> {
        return dockerConfigRepositories
            .map { runBlocking { it.getAll() } }
            .flatten()
            .associate { it.id to DockerApiService(createDockerClient(it.config)) }
    }

    private val mapper = jacksonObjectMapper()

    @Bean
    fun staticDockerConfigRepository(
        configurationProperties: DockerConfigurationProperties
    ): StaticDockerConfigRepository {
        val configs = configurationProperties.connections.map {
            val config = mapper.writeValueAsString(it.value)
            ConfigId(it.key) to mapper.readValue<DockerConfig>(config)
        }.toMap()

        return StaticDockerConfigRepository(configs)
    }
}