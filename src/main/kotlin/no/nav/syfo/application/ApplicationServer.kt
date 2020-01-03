package no.nav.syfo.application

import com.auth0.jwk.JwkProvider
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.install
import io.ktor.auth.authenticate
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import no.nav.syfo.Environment
import no.nav.syfo.application.api.registerNaisApi
import no.nav.syfo.application.authentication.setupAuth
import no.nav.syfo.application.metrics.monitorHttpRequests
import no.nav.syfo.papirsykemelding.api.registerPapirsykemeldingsRegler
import no.nav.syfo.papirsykemelding.service.PapirsykemeldingRegelService

@KtorExperimentalAPI
class ApplicationServer(
    private val env: Environment,
    private val jwkProvider: JwkProvider,
    private val papirsykemeldingRegelService: PapirsykemeldingRegelService
) {
    private val applicationState = ApplicationState()
    fun start() {
        val applicationServer = embeddedServer(Netty, env.applicationPort) {
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
                    registerModule(JavaTimeModule())
                    registerKotlinModule()
                }
            }
            intercept(ApplicationCallPipeline.Monitoring, monitorHttpRequests())
        }
        applicationServer.start(false)
        applicationState.ready = true
    }
}
