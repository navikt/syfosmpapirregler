package no.nav.syfo.papirsykemelding.rules.legesuspensjon

import no.nav.syfo.model.Status
import no.nav.syfo.model.Status.OK
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.dsl.RuleNode
import no.nav.syfo.papirsykemelding.rules.dsl.tree
import no.nav.syfo.rules.legesuspensjon.LegeSuspensjonRuleHit

enum class LegeSuspensjonRules {
    BEHANDLER_SUSPENDERT,
}

val legeSuspensjonRuleTree =
    tree<LegeSuspensjonRules, RuleResult>(LegeSuspensjonRules.BEHANDLER_SUSPENDERT) {
        yes(Status.MANUAL_PROCESSING, LegeSuspensjonRuleHit.BEHANDLER_SUSPENDERT)
        no(OK)
    }

internal fun RuleNode<LegeSuspensjonRules, RuleResult>.yes(
    status: Status,
    ruleHit: LegeSuspensjonRuleHit? = null
) {
    yes(RuleResult(status, ruleHit?.ruleHit))
}

internal fun RuleNode<LegeSuspensjonRules, RuleResult>.no(
    status: Status,
    ruleHit: LegeSuspensjonRuleHit? = null
) {
    no(RuleResult(status, ruleHit?.ruleHit))
}

fun getRule(rules: LegeSuspensjonRules): Rule<LegeSuspensjonRules> {
    return when (rules) {
        LegeSuspensjonRules.BEHANDLER_SUSPENDERT -> behandlerSuspendert
    }
}
