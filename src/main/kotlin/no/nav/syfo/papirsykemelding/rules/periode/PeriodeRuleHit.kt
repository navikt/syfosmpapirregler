package no.nav.syfo.papirsykemelding.rules.periode

import no.nav.syfo.model.Status
import no.nav.syfo.papirsykemelding.rules.common.RuleHit

enum class PeriodeRuleHit(val ruleHit: RuleHit) {
    TILBAKEDATERT_MER_ENN_3_AR(
        ruleHit =
            RuleHit(
                rule = "TILBAKEDATERT_MER_ENN_3_AR",
                status = Status.MANUAL_PROCESSING,
                messageForSender = "Sykmeldinges fom-dato er mer enn 3 år tilbake i tid.",
                messageForUser = "Startdatoen er mer enn tre år tilbake.",
            ),
    ),
    FREMDATERT(
        ruleHit =
            RuleHit(
                rule = "FREMDATERT",
                status = Status.MANUAL_PROCESSING,
                messageForSender =
                    "Sykmeldingen er fremdatert mer enn 30 dager etter behandletDato",
                messageForUser = "Sykmeldingen er datert mer enn 30 dager fram i tid.",
            ),
    ),
    TOTAL_VARIGHET_OVER_ETT_AAR(
        ruleHit =
            RuleHit(
                rule = "TOTAL_VARIGHET_OVER_ETT_AAR",
                status = Status.MANUAL_PROCESSING,
                messageForSender =
                    "Hvis sykmeldingens sluttdato er mer enn ett år frem i tid, avvises meldingen.",
                messageForUser = "Den kan ikke ha en varighet på over ett år.",
            ),
    ),
}
