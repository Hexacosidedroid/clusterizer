package ru.cib.clusterizer.config

import io.etcd.jetcd.Client
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EtcdConfig {

    @Bean
    fun initializeConfig() = Client.builder().endpoints("http://127.0.0.1:2379").build()
}