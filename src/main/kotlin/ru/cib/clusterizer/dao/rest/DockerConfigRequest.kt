package ru.cib.clusterizer.dao.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

data class DockerConfigRequest (
    val host: String?,
    val verify: String?,
    val certPath: String?,
    val url: String?,
    val user: String?,
    val password: String?
) {
    fun toJson(): String = jacksonObjectMapper().writeValueAsString(this)

    companion object {
        fun fromJson(json: String): DockerConfigRequest = jacksonObjectMapper().readValue(json)
    }
}