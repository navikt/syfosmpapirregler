package no.nav.syfo

import java.lang.RuntimeException

data class Environment(
    val applicationPort: Int = getEnvVar("APPLICATION_PORT", "8080").toInt()
)

fun getEnvVar(varName: String, defaultValue: String? = null) =
    System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missiong required variable \"$varName\"")