package ru.cib.clusterizer.dao.rest

data class DockerConfig (
    val host: String,
    val verify: String?,
    val certPath: String?,
    val url: String?,
    val user: String?,
    val password: String?
)