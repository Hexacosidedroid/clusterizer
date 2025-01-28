package ru.cib.clusterizer.domain.config

import ru.cib.clusterizer.domain.docker.DockerConfig

interface DockerConfigRepository {
    suspend fun getAll(): List<DockerConfigEntity>
    suspend fun findById(configId: ConfigId): DockerConfigEntity?
    suspend fun save(config: DockerConfigEntity)
    suspend fun delete(configId: ConfigId)
}

@JvmInline
value class ConfigId(val value: String)

data class DockerConfigEntity(
    val id: ConfigId,
    val readOnly: Boolean,
    val config: DockerConfig
)