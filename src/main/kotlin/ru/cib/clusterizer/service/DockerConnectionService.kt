package ru.cib.clusterizer.service

import io.etcd.jetcd.ByteSequence
import org.springframework.stereotype.Service
import ru.cib.clusterizer.config.EtcdConfig
import ru.cib.clusterizer.dao.rest.DockerConfigRequest

@Service
class DockerConnectionService(
    private val etcdConfig: EtcdConfig
) {
    private fun getClientFromEtcd(host: String): DockerConfigRequest? {
        val value = etcdConfig.initializeClient().get(
            ByteSequence.from(host.toByteArray())
        ).get().kvs.firstOrNull()?.value
        return value ?.let { DockerConfigRequest.fromJson(String(value.bytes)) }
    }

    fun saveConnection(configRequest: DockerConfigRequest) {
        etcdConfig.initializeClient().put(
            ByteSequence.from(configRequest.host?.toByteArray()),
            ByteSequence.from(configRequest.toJson().toByteArray())
        )
    }

    fun getAllConnections(): Map<String, DockerConfigRequest> {
        val connections = mutableMapOf<String, DockerConfigRequest>()
        val kvs = etcdConfig.initializeClient().get(ByteSequence.from("".toByteArray())).get().kvs
        kvs.forEach { kv ->
            val host = String(kv.key.bytes)
            val client = DockerConfigRequest.fromJson(String(kv.value.bytes))
            connections[host] = client
        }
        return connections
    }

    fun getConnection(host: String): DockerConfigRequest? {
        return getClientFromEtcd(host)
    }
}