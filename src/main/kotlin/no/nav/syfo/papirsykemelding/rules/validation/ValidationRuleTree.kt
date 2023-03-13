package no.nav.syfo.papirsykemelding.rules.validation

import no.nav.syfo.model.Status
import no.nav.syfo.model.Status.OK
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.dsl.RuleNode
import no.nav.syfo.papirsykemelding.rules.dsl.tree

enum class ValidationRules {
    PASIENT_YNGRE_ENN_13,
    PASIENT_ELDRE_ENN_70,
    UKJENT_DIAGNOSEKODETYPE,
    ICPC_2_Z_DIAGNOSE,
    HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER,
    UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE,
    UGYLDIG_KODEVERK_FOR_BIDIAGNOSE,
    UGYLDIG_ORGNR_LENGDE
}

val validationRuleTree = tree<ValidationRules, RuleResult>(ValidationRules.PASIENT_YNGRE_ENN_13) {
    yes(Status.MANUAL_PROCESSING, ValidationRuleHit.PASIENT_YNGRE_ENN_13)
    no(ValidationRules.PASIENT_ELDRE_ENN_70) {
        yes(Status.MANUAL_PROCESSING, ValidationRuleHit.PASIENT_ELDRE_ENN_70)
        no(ValidationRules.UKJENT_DIAGNOSEKODETYPE) {
            yes(Status.MANUAL_PROCESSING, ValidationRuleHit.UKJENT_DIAGNOSEKODETYPE)
            no(ValidationRules.ICPC_2_Z_DIAGNOSE) {
                yes(Status.MANUAL_PROCESSING, ValidationRuleHit.ICPC_2_Z_DIAGNOSE)
                no(ValidationRules.HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER) {
                    yes(Status.MANUAL_PROCESSING, ValidationRuleHit.HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER)
                    no(ValidationRules.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE) {
                        yes(Status.MANUAL_PROCESSING, ValidationRuleHit.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE)
                        no(ValidationRules.UGYLDIG_KODEVERK_FOR_BIDIAGNOSE) {
                            yes(Status.MANUAL_PROCESSING, ValidationRuleHit.UGYLDIG_KODEVERK_FOR_BIDIAGNOSE)
                            no(ValidationRules.UGYLDIG_ORGNR_LENGDE) {
                                yes(Status.MANUAL_PROCESSING, ValidationRuleHit.UGYLDIG_ORGNR_LENGDE)
                                no(OK)
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun RuleNode<ValidationRules, RuleResult>.yes(status: Status, ruleHit: ValidationRuleHit? = null) {
    yes(RuleResult(status, ruleHit?.ruleHit))
}

internal fun RuleNode<ValidationRules, RuleResult>.no(status: Status, ruleHit: ValidationRuleHit? = null) {
    no(RuleResult(status, ruleHit?.ruleHit))
}

fun getRule(rules: ValidationRules): Rule<ValidationRules> {
    return when (rules) {
        ValidationRules.PASIENT_YNGRE_ENN_13 -> pasientUnder13Aar
        ValidationRules.PASIENT_ELDRE_ENN_70 -> pasienteldreenn70Aar
        ValidationRules.UKJENT_DIAGNOSEKODETYPE -> ukjentdiagnosekodetype
        ValidationRules.ICPC_2_Z_DIAGNOSE -> icpc2zdiagnose
        ValidationRules.HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER -> houveddiagnsoeellerfravaergrunnmangler
        ValidationRules.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE -> ugyldigkodeverkforhouveddiagnose
        ValidationRules.UGYLDIG_KODEVERK_FOR_BIDIAGNOSE -> ugyldigkodeverkforbidiagnose
        ValidationRules.UGYLDIG_ORGNR_LENGDE -> ugyldingOrgNummerLengde
    }
}
