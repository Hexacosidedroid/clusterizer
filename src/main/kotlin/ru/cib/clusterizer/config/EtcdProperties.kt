package ru.cib.clusterizer.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "etcd")
data class EtcdProperties(
    val endpoint: String
)
