package ru.cib.clusterizer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ClusterizerApplication

fun main(args: Array<String>) {
    runApplication<ClusterizerApplication>(*args)
}
