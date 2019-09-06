package no.nav.syfo

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule()).registerModule(KotlinModule())

fun <T> getStringValue(content: T): String {
    return objectMapper.writeValueAsString(content)
}
