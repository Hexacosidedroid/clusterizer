package ru.cib.clusterizer.domain.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.etcd.jetcd.ByteSequence
import io.etcd.jetcd.KV
import io.etcd.jetcd.options.GetOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import ru.cib.clusterizer.domain.docker.DockerConfig

@Service
@ConditionalOnProperty(value = ["etcd"], matchIfMissing = false)
class EtcdDockerConfigRepository(
    private val kvClient: KV
) : DockerConfigRepository {
    companion object {
        private val logger = LoggerFactory.getLogger(EtcdDockerConfigRepository::class.java)
        private val mapper = jacksonObjectMapper()
    }

    override suspend fun getAll(): List<DockerConfigEntity> = withContext(Dispatchers.IO) {
        logger.debug("<410b142e> Fetching all docker configs")
        kvClient.get(ByteSequence.from("".encodeToByteArray()), GetOption.builder().isPrefix(true).build())
            .get().kvs.map {
                DockerConfigEntity(
                    id = ConfigId(it.key.bytes.decodeToString()),
                    readOnly = false,
                    config = mapper.readValue<DockerConfig>(it.value.bytes)
                )
            }
    }

    override suspend fun findById(configId: ConfigId): DockerConfigEntity? = withContext(Dispatchers.IO) {
        logger.debug("<32d65bb9> Finding docker config with id $configId")
        kvClient
            .get(ByteSequence.from(configId.value.encodeToByteArray()))
            .get()
            .kvs
            .firstOrNull()
            ?.value
            ?.let {
                DockerConfigEntity(
                    id = configId,
                    readOnly = false,
                    config = mapper.readValue<DockerConfig>(it.bytes)
                )
            }
    }

    override suspend fun save(config: DockerConfigEntity): Unit = withContext(Dispatchers.IO) {
        logger.debug("<503935a6> Saving docker config {}", config)
        kvClient.put(
            ByteSequence.from(config.id.value.encodeToByteArray()),
            ByteSequence.from(mapper.writeValueAsBytes(config.config))
        ).get()
    }

    override suspend fun delete(configId: ConfigId): Unit = withContext(Dispatchers.IO) {
        logger.debug("<40b448> Deleting docker config with id $configId")
        kvClient.delete(ByteSequence.from(configId.value.encodeToByteArray())).get()
    }
}