package ru.cib.clusterizer.dao.rest

data class Image(
    val created: Long?,
    val id: String,
    val parentId: String,
    val repoTags: String,
    val repoDigests: String,
    val size: Long,
    val virtualSize: Long,
    val sharedSize: Long,
    val labels: Map<String, String>,
    val containers: Int
)