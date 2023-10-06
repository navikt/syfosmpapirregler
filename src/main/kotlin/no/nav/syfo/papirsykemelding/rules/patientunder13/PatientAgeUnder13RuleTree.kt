package no.nav.syfo.papirsykemelding.rules.patientunder13

import no.nav.syfo.model.Status
import no.nav.syfo.model.Status.OK
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.dsl.RuleNode
import no.nav.syfo.papirsykemelding.rules.dsl.tree

enum class PatientAgeUnder13Rules {
    PASIENT_YNGRE_ENN_13,
}

val patientAgeUnder13RuleTree =
    tree<PatientAgeUnder13Rules, RuleResult>(PatientAgeUnder13Rules.PASIENT_YNGRE_ENN_13) {
        yes(Status.MANUAL_PROCESSING, PatientAgeUnder13RuleHit.PASIENT_YNGRE_ENN_13)
        no(OK)
    }

internal fun RuleNode<PatientAgeUnder13Rules, RuleResult>.yes(
    status: Status,
    ruleHit: PatientAgeUnder13RuleHit? = null
) {
    yes(RuleResult(status, ruleHit?.ruleHit))
}

internal fun RuleNode<PatientAgeUnder13Rules, RuleResult>.no(
    status: Status,
    ruleHit: PatientAgeUnder13RuleHit? = null
) {
    no(RuleResult(status, ruleHit?.ruleHit))
}

fun getRule(rules: PatientAgeUnder13Rules): Rule<PatientAgeUnder13Rules> {
    return when (rules) {
        PatientAgeUnder13Rules.PASIENT_YNGRE_ENN_13 -> pasientUnder13Aar
    }
}
