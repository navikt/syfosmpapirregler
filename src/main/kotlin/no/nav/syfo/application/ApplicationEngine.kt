package no.nav.syfo.application

import com.auth0.jwk.JwkProvider
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.DelicateCoroutinesApi
import no.nav.syfo.Environment
import no.nav.syfo.application.api.registerNaisApi
import no.nav.syfo.application.authentication.setupAuth
import no.nav.syfo.application.metrics.monitorHttpRequests
import no.nav.syfo.log
import no.nav.syfo.papirsykemelding.api.registerPapirsykemeldingsRegler
import no.nav.syfo.papirsykemelding.service.PapirsykemeldingRegelService

@DelicateCoroutinesApi
fun createApplicationEngine(
    papirsykemeldingRegelService: PapirsykemeldingRegelService,
    env: Environment,
    applicationState: ApplicationState,
    jwkProviderAadV2: JwkProvider,
): ApplicationEngine =
    embeddedServer(Netty, env.applicationPort) {
        setupAuth(
            environment = env,
            jwkProviderAadV2 = jwkProviderAadV2,
        )
        routing {
            registerNaisApi(applicationState)
            route("/api/v2") {
                authenticate("servicebrukerAADv2") {
                    registerPapirsykemeldingsRegler(papirsykemeldingRegelService)
                }
            }
        }
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
        install(StatusPages) {
            exception<Throwable> { call, cause ->
                call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Unknown error")

                log.error("Caught exception while trying to validate against rules", cause)
                throw cause
            }
        }
        intercept(ApplicationCallPipeline.Monitoring, monitorHttpRequests())
    }
