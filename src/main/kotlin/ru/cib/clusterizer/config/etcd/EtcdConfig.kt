package ru.cib.clusterizer.config.etcd

import io.etcd.jetcd.Client
import io.etcd.jetcd.KV
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(value = ["etcd"], matchIfMissing = false)
class EtcdConfig(
    private val etcdProperties: EtcdProperties
) {

    @Bean
    fun kvClient(): KV = Client.builder().endpoints(etcdProperties.endpoint).build().kvClient
}