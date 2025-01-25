package ru.cib.clusterizer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import ru.cib.clusterizer.config.EtcdProperties

@SpringBootApplication
@EnableConfigurationProperties(EtcdProperties::class)
class ClusterizerApplication

fun main(args: Array<String>) {
    runApplication<ClusterizerApplication>(*args)
}
