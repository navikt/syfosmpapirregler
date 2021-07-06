package no.nav.syfo.application

import com.auth0.jwk.JwkProvider
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.authenticate
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import no.nav.syfo.Environment
import no.nav.syfo.application.api.registerNaisApi
import no.nav.syfo.application.authentication.setupAuth
import no.nav.syfo.application.metrics.monitorHttpRequests
import no.nav.syfo.log
import no.nav.syfo.papirsykemelding.api.registerPapirsykemeldingsRegler
import no.nav.syfo.papirsykemelding.service.PapirsykemeldingRegelService

@KtorExperimentalAPI
fun createApplicationEngine(
    papirsykemeldingRegelService: PapirsykemeldingRegelService,
    jwkProvider: JwkProvider,
    env: Environment,
    applicationState: ApplicationState
): ApplicationEngine =
    embeddedServer(Netty, env.applicationPort) {
        setupAuth(env, jwkProvider)
        routing {
            registerNaisApi(applicationState)
            route("/api") {
                authenticate {
                    registerPapirsykemeldingsRegler(papirsykemeldingRegelService)
                }
            }
        }
        install(ContentNegotiation) {
            jackson {
                registerKotlinModule()
                registerModule(JavaTimeModule())
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
        install(StatusPages) {
            exception<Throwable> { cause ->
                call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Unknown error")

                log.error("Caught exception while trying to validate against rules", cause)
                throw cause
            }
        }
        intercept(ApplicationCallPipeline.Monitoring, monitorHttpRequests())
    }
