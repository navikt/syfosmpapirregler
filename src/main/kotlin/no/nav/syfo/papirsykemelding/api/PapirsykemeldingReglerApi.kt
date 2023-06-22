package no.nav.syfo.papirsykemelding.api

import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.coroutines.DelicateCoroutinesApi
import no.nav.syfo.model.ReceivedSykmelding
import no.nav.syfo.model.ValidationResult
import no.nav.syfo.papirsykemelding.service.PapirsykemeldingRegelService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log: Logger = LoggerFactory.getLogger("no.nav.syfo.smpapirregler")

@DelicateCoroutinesApi
fun Route.registerPapirsykemeldingsRegler(
    papirsykemeldingRegelService: PapirsykemeldingRegelService
) {
    post("/rules/validate") {
        log.info("Got an request to validate papirregler")
        val receivedSykemleding: ReceivedSykmelding = call.receive()
        val result: ValidationResult =
            papirsykemeldingRegelService.validateSykemelding(receivedSykemleding)
        call.respond(result)
    }
}
