package no.nav.syfo

import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.server.testing.TestApplicationEngine
import java.nio.file.Paths
import no.nav.syfo.application.authentication.setupAuth

fun TestApplicationEngine.setUpTestApplication() {
    start(true)
    application.install(ContentNegotiation) {
        jackson {
            registerKotlinModule()
            registerModule(JavaTimeModule())
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }
    application.install(StatusPages) {
        exception<Throwable> { cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Unknown error")

            log.error("Caught exception while trying to validate against rules", cause)
            throw cause
        }
    }
}

val testAuthorizedUsers = listOf("smpapirmottak")

fun TestApplicationEngine.setUpAuth(jwkKeysUrl: String = "url", audience: List<String> = testAuthorizedUsers): Environment {
    val env = Environment(
        jwkKeysUrl = jwkKeysUrl,
        appIds = audience,
        helsenettproxyScope = "",
        norskHelsenettEndpointURL = "url",
        aadAccessTokenV2Url = "",
        jwtIssuer = "https://sts.issuer.net/myid",
        clientId = "regel-clientId",
        clientIdV2 = "regel-clientId-v2",
        clientSecretV2 = "",
        jwkKeysUrlV2 = "https://keys.url",
        jwtIssuerV2 = "https://sts.issuer.net/myidV2"
    )

    val path = "src/test/resources/jwkset.json"
    val uri = Paths.get(path).toUri().toURL()
    val jwkProvider = JwkProviderBuilder(uri).build()

    application.setupAuth(env, jwkProvider, jwkProvider)
    return env
}