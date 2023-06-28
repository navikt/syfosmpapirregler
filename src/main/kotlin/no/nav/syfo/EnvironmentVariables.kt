package no.nav.syfo

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

data class EnvironmentVariables(
    val applicationPort: Int = getEnvVar("APPLICATION_PORT", "8080").toInt(),
    val legeSuspensjonProxyEndpointURL: String = getEnvVar("LEGE_SUSPENSJON_PROXY_ENDPOINT_URL"),
    val legeSuspensjonProxyScope: String = getEnvVar("LEGE_SUSPENSJON_PROXY_SCOPE"),
    val norskHelsenettEndpointURL: String = getEnvVar("HELSENETT_ENDPOINT_URL"),
    val syketilfelleEndpointURL: String =
        getEnvVar("SYKETILLFELLE_ENDPOINT_URL", "http://flex-syketilfelle.flex"),
    val syketilfelleScope: String = getEnvVar("SYKETILLFELLE_SCOPE"),
    val aadAccessTokenV2Url: String = getEnvVar("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
    val clientIdV2: String = getEnvVar("AZURE_APP_CLIENT_ID"),
    val clientSecretV2: String = getEnvVar("AZURE_APP_CLIENT_SECRET"),
    val helsenettproxyScope: String = getEnvVar("HELSENETT_SCOPE"),
    val jwkKeysUrlV2: String = getEnvVar("AZURE_OPENID_CONFIG_JWKS_URI"),
    val jwtIssuerV2: String = getEnvVar("AZURE_OPENID_CONFIG_ISSUER"),
    val applicationName: String = getEnvVar("NAIS_APP_NAME", "syfosmpapirregler"),
    val etterlevelsesTopic: String = "teamsykmelding.paragraf-i-kode",
    val pdlScope: String = getEnvVar("PDL_SCOPE"),
    val pdlGraphqlPath: String = getEnvVar("PDL_GRAPHQL_PATH"),
    val smregisterEndpointURL: String = getEnvVar("SMREGISTER_URL", "http://syfosmregister"),
    val smregisterAudience: String = getEnvVar("SMREGISTER_AUDIENCE"),
)

fun getEnvVar(varName: String, defaultValue: String? = null) =
    System.getenv(varName)
        ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")

fun getFileAsString(filePath: String) =
    String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8)
