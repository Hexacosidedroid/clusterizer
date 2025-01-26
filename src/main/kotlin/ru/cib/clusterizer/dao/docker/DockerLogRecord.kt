package ru.cib.clusterizer.dao.docker

import com.github.dockerjava.api.model.StreamType

data class DockerLogRecord(
    val type: StreamType,
    val payload: String,
)