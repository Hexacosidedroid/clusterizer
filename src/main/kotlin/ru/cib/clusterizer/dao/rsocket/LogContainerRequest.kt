package ru.cib.clusterizer.dao.rsocket

data class LogContainerRequest(
    val id: String,
    val follow: Boolean = true,
    val tail: Int? = null
)
