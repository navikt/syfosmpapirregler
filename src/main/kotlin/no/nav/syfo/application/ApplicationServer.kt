package no.nav.syfo.application

import com.auth0.jwk.JwkProvider
import io.ktor.auth.authenticate
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.syfo.Environment
import no.nav.syfo.api.registerNaisApi
import no.nav.syfo.api.registerPapirsykemeldingsRegler
import no.nav.syfo.authentication.setupAuth
import no.nav.syfo.service.PapirsykemeldingRegelService

class ApplicationServer(private val env: Environment, private val jwkProvider: JwkProvider) {
    private val applicationState = ApplicationState()
    fun start() {
        val applicationServer = embeddedServer(Netty, env.applicationPort) {
            setupAuth(env, jwkProvider)
            routing {
                registerNaisApi(applicationState)
                route("/api") {
                    authenticate {
                        registerPapirsykemeldingsRegler(PapirsykemeldingRegelService())
                    }
                }
            }
        }
        applicationServer.start(false)
        applicationState.running = true
    }
}
