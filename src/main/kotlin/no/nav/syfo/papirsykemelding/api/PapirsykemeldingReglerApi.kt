package no.nav.syfo.papirsykemelding.api

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.util.KtorExperimentalAPI
import no.nav.syfo.model.ReceivedSykmelding
import no.nav.syfo.model.ValidationResult
import no.nav.syfo.papirsykemelding.service.PapirsykemeldingRegelService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log: Logger = LoggerFactory.getLogger("no.nav.syfo.smpapirregler")

@KtorExperimentalAPI
fun Route.registerPapirsykemeldingsRegler(papirsykemeldingRegelService: PapirsykemeldingRegelService) {
    post("/v1/rules/validate") {
        log.info("Got an request to validate papirregler")
        val receivedSykemleding: ReceivedSykmelding = call.receive()
        val result: ValidationResult = papirsykemeldingRegelService.validateSykemelding(receivedSykemleding)
        call.respond(result)
    }
}
