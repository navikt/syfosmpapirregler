package no.nav.syfo.papirsykemelding.rules.periode

import no.nav.syfo.model.Status
import no.nav.syfo.model.Status.MANUAL_PROCESSING
import no.nav.syfo.model.Status.OK
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.dsl.RuleNode
import no.nav.syfo.papirsykemelding.rules.dsl.tree

enum class PeriodeRules {
    FREMDATERT,
    TILBAKEDATERT_MER_ENN_3_AR,
    TOTAL_VARIGHET_OVER_ETT_AAR,
}

val periodeRuleTree =
    tree<PeriodeRules, RuleResult>(PeriodeRules.FREMDATERT) {
        yes(MANUAL_PROCESSING, PeriodeRuleHit.FREMDATERT)
        no(PeriodeRules.TILBAKEDATERT_MER_ENN_3_AR) {
            yes(MANUAL_PROCESSING, PeriodeRuleHit.TILBAKEDATERT_MER_ENN_3_AR)
            no(PeriodeRules.TOTAL_VARIGHET_OVER_ETT_AAR) {
                yes(MANUAL_PROCESSING, PeriodeRuleHit.TOTAL_VARIGHET_OVER_ETT_AAR)
                no(OK)
            }
        }
    }

internal fun RuleNode<PeriodeRules, RuleResult>.yes(
    status: Status,
    ruleHit: PeriodeRuleHit? = null
) {
    yes(RuleResult(status, ruleHit?.ruleHit))
}

internal fun RuleNode<PeriodeRules, RuleResult>.no(
    status: Status,
    ruleHit: PeriodeRuleHit? = null
) {
    no(RuleResult(status, ruleHit?.ruleHit))
}

fun getRule(rules: PeriodeRules): Rule<PeriodeRules> {
    return when (rules) {
        PeriodeRules.FREMDATERT -> fremdatertOver30Dager
        PeriodeRules.TILBAKEDATERT_MER_ENN_3_AR -> tilbakeDatertOver3Ar
        PeriodeRules.TOTAL_VARIGHET_OVER_ETT_AAR -> varighetOver1AAr
    }
}
