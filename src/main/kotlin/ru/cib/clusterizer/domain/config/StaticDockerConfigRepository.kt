package ru.cib.clusterizer.domain.config

import ru.cib.clusterizer.domain.docker.DockerConfig

class StaticDockerConfigRepository(
    val configs: Map<ConfigId, DockerConfig>
) : DockerConfigRepository {
    override suspend fun getAll(): List<DockerConfigEntity> {
        return configs.map {
            DockerConfigEntity(
                id = it.key,
                readOnly = true,
                config = it.value
            )
        }
    }

    override suspend fun findById(configId: ConfigId): DockerConfigEntity? {
        return configs[configId]?.let {
            DockerConfigEntity(
                id = configId,
                readOnly = true,
                config = it
            )
        }
    }

    override suspend fun save(config: DockerConfigEntity) {
        throw IllegalStateException()
    }

    override suspend fun delete(configId: ConfigId) {
        throw IllegalStateException()
    }
}