package ru.cib.clusterizer.config

import io.etcd.jetcd.Client
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EtcdConfig(
    private val etcdProperties: EtcdProperties
) {

    @Bean
    fun initializeClient(): Client = Client.builder().endpoints(etcdProperties.endpoint).build()
}