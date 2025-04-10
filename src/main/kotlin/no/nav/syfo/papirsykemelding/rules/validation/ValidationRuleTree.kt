package no.nav.syfo.papirsykemelding.rules.validation

import no.nav.syfo.model.Status
import no.nav.syfo.model.Status.OK
import no.nav.syfo.model.juridisk.JuridiskEnum
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.dsl.RuleNode
import no.nav.syfo.papirsykemelding.rules.dsl.tree

enum class ValidationRules {
    UGYLDIG_ORGNR_LENGDE,
}

val validationRuleTree =
    tree<ValidationRules, RuleResult>(ValidationRules.UGYLDIG_ORGNR_LENGDE) {
        yes(
            Status.MANUAL_PROCESSING,
            ValidationRuleHit.UGYLDIG_ORGNR_LENGDE,
        )
        no(OK)
    }

internal fun RuleNode<ValidationRules, RuleResult>.yes(
    status: Status,
    ruleHit: ValidationRuleHit? = null
) {
    yes(RuleResult(status, JuridiskEnum.INGEN.JuridiskHenvisning, ruleHit?.ruleHit))
}

internal fun RuleNode<ValidationRules, RuleResult>.no(
    status: Status,
    ruleHit: ValidationRuleHit? = null
) {
    no(RuleResult(status, JuridiskEnum.INGEN.JuridiskHenvisning, ruleHit?.ruleHit))
}

fun getRule(rules: ValidationRules): Rule<ValidationRules> {
    return when (rules) {
        ValidationRules.UGYLDIG_ORGNR_LENGDE -> ugyldingOrgNummerLengde
    }
}
