package no.nav.syfo.api

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import no.nav.syfo.service.PapirsykemeldingRegelService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log: Logger = LoggerFactory.getLogger("no.nav.syfo.smpapirregler")

fun Route.registerPapirsykemeldingsRegler(papirsykemeldingRegelService: PapirsykemeldingRegelService) {
    post("/v1/rules/validate") {
            val receivedSykemleding: String = call.receive()
            val result: String = papirsykemeldingRegelService.validateSykemelding(receivedSykemleding)
            call.respond(result)
        }
}
