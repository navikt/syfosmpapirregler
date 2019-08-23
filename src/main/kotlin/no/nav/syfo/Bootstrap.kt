package no.nav.syfo

import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.syfo.api.registerNaisApi

data class ApplicationState(val running: Boolean = true, val initialized: Boolean = false)

fun main() {
    val env = Environment()
    val applicationState = ApplicationState()
    val applicationServer = embeddedServer(Netty, env.applicationPort) {
        routing { registerNaisApi(readynessCheck = { true }, livenessCheck = { applicationState.running } ) }
    }
    applicationServer.start(true)
}
