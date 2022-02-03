package no.nav.syfo

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

data class Environment(
    val applicationPort: Int = getEnvVar("APPLICATION_PORT", "8080").toInt(),
    val securityTokenServiceURL: String = getEnvVar("SECURITY_TOKEN_SERVICE_URL", "http://security-token-service.default/rest/v1/sts/token"),
    val legeSuspensjonEndpointURL: String = getEnvVar("LEGE_SUSPENSJON_ENDPOINT_URL", "http://btsys.default"),
    val norskHelsenettEndpointURL: String = getEnvVar("HELSENETT_ENDPOINT_URL"),
    val syketilfelleEndpointURL: String = getEnvVar("SYKETILLFELLE_ENDPOINT_URL"),
    val syketilfelleScope: String = getEnvVar("SYKETILLFELLE_SCOPE"),
    val aadAccessTokenV2Url: String = getEnvVar("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
    val clientIdV2: String = getEnvVar("AZURE_APP_CLIENT_ID"),
    val clientSecretV2: String = getEnvVar("AZURE_APP_CLIENT_SECRET"),
    val helsenettproxyScope: String = getEnvVar("HELSENETT_SCOPE"),
    val jwkKeysUrlV2: String = getEnvVar("AZURE_OPENID_CONFIG_JWKS_URI"),
    val jwtIssuerV2: String = getEnvVar("AZURE_OPENID_CONFIG_ISSUER"),
    val applicationName: String = getEnvVar("NAIS_APP_NAME", "syfosmpapirregler"),
    val etterlevelsesTopic: String = "teamsykmelding.paragraf-i-kode",
    val versjonAvKode: String = getEnvVar("NAIS_APP_IMAGE")
)

data class VaultCredentials(
    val serviceuserUsername: String = getFileAsString("/secrets/serviceuser/username"),
    val serviceuserPassword: String = getFileAsString("/secrets/serviceuser/password")
)

fun getEnvVar(varName: String, defaultValue: String? = null) =
    System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")

fun getFileAsString(filePath: String) = String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8)
