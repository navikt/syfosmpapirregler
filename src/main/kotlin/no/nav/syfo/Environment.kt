package no.nav.syfo

import java.lang.RuntimeException

data class Environment(
    val applicationPort: Int = getEnvVar("APPLICATION_PORT", "8080").toInt(),
    val jwkKeysUrl: String = getEnvVar("JWKKEYS_URL", "https://login.microsoftonline.com/common/discovery/keys"),
    val clientId: String = getEnvVar("CLIENT_ID"),
    val jwtIssuer: String = getEnvVar("JWT_ISSUER"),
    val diskresjonskodeEndpointUrl: String = getEnvVar("DISKRESJONSKODE_ENDPOINT_URL"),
    val securityTokenServiceURL: String = getEnvVar("SECURITY_TOKEN_SERVICE_URL"),
    val appIds: List<String> = getEnvVar("ALLOWED_APP_IDS")
        .split(",")
        .map { it.trim() }
)

data class VaultCredentials(
    val serviceuserUsername: String,
    val serviceuserPassword: String
)

fun getEnvVar(varName: String, defaultValue: String? = null) =
    System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")
