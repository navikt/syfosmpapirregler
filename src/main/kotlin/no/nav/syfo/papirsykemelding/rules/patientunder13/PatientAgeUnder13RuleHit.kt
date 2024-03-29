package no.nav.syfo.papirsykemelding.rules.patientunder13

import no.nav.syfo.model.Status
import no.nav.syfo.papirsykemelding.rules.common.RuleHit

enum class PatientAgeUnder13RuleHit(
    val ruleHit: RuleHit,
) {
    PASIENT_YNGRE_ENN_13(
        ruleHit =
            RuleHit(
                rule = "PASIENT_YNGRE_ENN_13",
                status = Status.MANUAL_PROCESSING,
                messageForSender = "Pasienten er under 13 år. Sykmelding kan ikke benyttes.",
                messageForUser = "Pasienten er under 13 år. Sykmelding kan ikke benyttes.",
            ),
    ),
}
