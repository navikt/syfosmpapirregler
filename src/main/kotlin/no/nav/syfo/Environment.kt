package no.nav.syfo

import java.lang.RuntimeException

data class Environment(
    val applicationPort: Int = getEnvVar("APPLICATION_PORT", "8080").toInt()
)

fun getEnvVar(varName: String, defaultValue: String? = null) =
    defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")