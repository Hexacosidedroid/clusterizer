package ru.cib.clusterizer.dao.rest

data class ImageRequest(
    val repository: String?,
    val name: String,
    val tag: String
)