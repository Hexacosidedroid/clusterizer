package ru.cib.clusterizer.config

import io.etcd.jetcd.Client
import io.etcd.jetcd.KV
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EtcdConfig(
    private val etcdProperties: EtcdProperties
) {

    @Bean
    fun initializeClient(): KV = Client.builder().endpoints(etcdProperties.endpoint).build().kvClient
}