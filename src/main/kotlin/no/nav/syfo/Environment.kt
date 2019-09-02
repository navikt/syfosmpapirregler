package no.nav.syfo

import java.lang.RuntimeException

data class Environment(
    val applicationPort: Int = getEnvVar("APPLICATION_PORT", "8080").toInt(),
    val jwkKeysUrl: String = getEnvVar("JWKKEYS_URL", "https://login.microsoftonline.com/common/discovery/keys"),
    val jwtIssuer: String = getEnvVar("JWT_ISSUER"),
    val clientId: String = getEnvVar("CLIENT_ID"),
    val appIds: List<String> = getEnvVar("ALLOWED_APP_IDS")
        .split(",")
        .map { it.trim() }
)

fun getEnvVar(varName: String, defaultValue: String? = null) =
    defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")
