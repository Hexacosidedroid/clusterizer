package ru.cib.clusterizer.dao.rest

data class DockerConfigRequest (
    val host: String,
    val verify: String?,
    val certPath: String?,
    val url: String?,
    val user: String?,
    val password: String?
)