package no.nav.syfo

import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.testing.*
import java.nio.file.Paths

fun ApplicationTestBuilder.setUpTestApplication() {
    application {
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
        install(StatusPages) {
            exception<Throwable> { call, cause ->
                call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Unknown error")

                logger.error("Caught exception while trying to validate against rules", cause)
                throw cause
            }
        }
    }
}

fun ApplicationTestBuilder.setUpAuth(jwkKeysUrl: String = "url"): EnvironmentVariables {
    val env =
        EnvironmentVariables(
            helsenettproxyScope = "",
            norskHelsenettEndpointURL = "url",
            aadAccessTokenV2Url = "",
            clientIdV2 = "regel-clientId-v2",
            clientSecretV2 = "",
            jwkKeysUrlV2 = jwkKeysUrl,
            jwtIssuerV2 = "https://sts.issuer.net/myidV2",
            pdlScope = "pdl",
            pdlGraphqlPath = "https://pdl",
            legeSuspensjonProxyEndpointURL = "url",
            legeSuspensjonProxyScope = "scope",
            smregisterAudience = "smregister-audience",
        )

    val path = "src/test/resources/jwkset.json"
    val uri = Paths.get(path).toUri().toURL()
    val jwkProvider = JwkProviderBuilder(uri).build()
    application { setupAuth(env, jwkProvider) }
    return env
}
