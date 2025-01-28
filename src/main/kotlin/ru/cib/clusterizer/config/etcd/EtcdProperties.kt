package ru.cib.clusterizer.config.etcd

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "etcd")
data class EtcdProperties(
    val endpoint: String
)
