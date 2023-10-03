package no.nav.syfo.papirsykemelding.rules.validation

import no.nav.syfo.model.Status
import no.nav.syfo.papirsykemelding.rules.common.RuleHit

enum class ValidationRuleHit(
    val ruleHit: RuleHit,
) {
    UGYLDIG_ORGNR_LENGDE(
        ruleHit =
            RuleHit(
                rule = "UGYLDIG_ORGNR_LENGDE",
                status = Status.MANUAL_PROCESSING,
                messageForSender = "Den må ha riktig organisasjonsnummer.Dette skal være 9 sifre.",
                messageForUser = "Den må ha riktig organisasjonsnummer.Dette skal være 9 sifre.",
            ),
    ),
}
