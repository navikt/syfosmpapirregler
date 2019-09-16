package no.nav.syfo.papirsykemelding.service

import io.prometheus.client.Counter
import no.nav.syfo.application.metrics.RULE_HIT_STATUS_COUNTER
import no.nav.syfo.model.ReceivedSykmelding
import no.nav.syfo.model.RuleInfo
import no.nav.syfo.model.Status
import no.nav.syfo.model.ValidationResult

class PapirsykemeldingRegelService(
    private val ruleHitStatusCounter: Counter = RULE_HIT_STATUS_COUNTER
) {

    fun validateSykemelding(sykemelding: ReceivedSykmelding): ValidationResult {
        return if (sykemelding.sykmelding.perioder.count() > 0) {
            ruleHitStatusCounter.labels(Status.OK.name).inc()
            ValidationResult(Status.OK, emptyList())
        } else {
            ruleHitStatusCounter.labels(Status.INVALID.name).inc()
            ValidationResult(Status.INVALID, listOf(RuleInfo("Ingen perioder",
                "Ingen perioder registrert",
                "Ingen perioder registrert")))
        }
    }
}
