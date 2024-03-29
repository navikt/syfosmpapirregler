package no.nav.syfo.papirsykemelding.rules.legesuspensjon

import no.nav.syfo.model.Status
import no.nav.syfo.papirsykemelding.rules.common.RuleHit

enum class LegeSuspensjonRuleHit(
    val ruleHit: RuleHit,
) {
    BEHANDLER_SUSPENDERT(
        ruleHit =
            RuleHit(
                rule = "BEHANDLER_SUSPENDERT",
                status = Status.MANUAL_PROCESSING,
                messageForSender =
                    "Behandler er suspendert av NAV på konsultasjonstidspunkt. Pasienten har fått beskjed.",
                messageForUser =
                    "Den som sykmeldte deg har mistet retten til å skrive sykmeldinger.",
            ),
    ),
}
