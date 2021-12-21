package no.nav.syfo.papirsykemelding.rules

import no.nav.syfo.model.Status
import no.nav.syfo.rules.Description
import no.nav.syfo.rules.Rule
import no.nav.syfo.rules.RuleData

enum class LegesuspensjonRuleChain(
    override val ruleId: Int?,
    override val status: Status,
    override val messageForUser: String,
    override val messageForSender: String,
    override val predicate: (RuleData<Boolean>) -> Boolean
) : Rule<RuleData<Boolean>> {
    @Description("Behandler er suspendert av NAV på konsultasjonstidspunkt")
    BEHANDLER_SUSPENDERT(
        1414,
        Status.MANUAL_PROCESSING,
        "Den som sykmeldte deg har mistet retten til å skrive sykmeldinger.",
        "Behandler er suspendert av NAV på konsultasjonstidspunkt", { (_, suspended) ->
            suspended
        }
    )
}
