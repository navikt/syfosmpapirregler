package no.nav.syfo.papirsykemelding.rules.arbeidsuforhet

import no.nav.syfo.model.Status
import no.nav.syfo.model.Status.OK
import no.nav.syfo.model.juridisk.JuridiskHenvisning
import no.nav.syfo.model.juridisk.Lovverk
import no.nav.syfo.papirsykemelding.rules.common.MedJuridisk
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.dsl.RuleNode
import no.nav.syfo.papirsykemelding.rules.dsl.tree

enum class ArbeidsuforhetRules {
    UKJENT_DIAGNOSEKODETYPE,
    ICPC_2_Z_DIAGNOSE,
    HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER,
    UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE,
    UGYLDIG_KODEVERK_FOR_BIDIAGNOSE,
}

val arbeidsuforhetRuleTree =
    tree<ArbeidsuforhetRules, RuleResult>(ArbeidsuforhetRules.UKJENT_DIAGNOSEKODETYPE) {
        yes(Status.MANUAL_PROCESSING, ArbeidsuforhetRuleHit.UKJENT_DIAGNOSEKODETYPE)
        no(ArbeidsuforhetRules.ICPC_2_Z_DIAGNOSE) {
            yes(Status.MANUAL_PROCESSING, ArbeidsuforhetRuleHit.ICPC_2_Z_DIAGNOSE)
            no(ArbeidsuforhetRules.HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER) {
                yes(
                    Status.MANUAL_PROCESSING,
                    ArbeidsuforhetRuleHit.HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER
                )
                no(ArbeidsuforhetRules.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE) {
                    yes(
                        Status.MANUAL_PROCESSING,
                        ArbeidsuforhetRuleHit.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE
                    )
                    no(ArbeidsuforhetRules.UGYLDIG_KODEVERK_FOR_BIDIAGNOSE) {
                        yes(
                            Status.MANUAL_PROCESSING,
                            ArbeidsuforhetRuleHit.UGYLDIG_KODEVERK_FOR_BIDIAGNOSE
                        )
                        no(OK)
                    }
                }
            }
        }
    } to
        MedJuridisk(
            JuridiskHenvisning(
                lovverk = Lovverk.FOLKETRYGDLOVEN,
                paragraf = "8-4",
                ledd = 1,
                punktum = null,
                bokstav = null,
            ),
        )

internal fun RuleNode<ArbeidsuforhetRules, RuleResult>.yes(
    status: Status,
    ruleHit: ArbeidsuforhetRuleHit? = null
) {
    yes(RuleResult(status, ruleHit?.ruleHit))
}

internal fun RuleNode<ArbeidsuforhetRules, RuleResult>.no(
    status: Status,
    ruleHit: ArbeidsuforhetRuleHit? = null
) {
    no(RuleResult(status, ruleHit?.ruleHit))
}

fun getRule(rules: ArbeidsuforhetRules): Rule<ArbeidsuforhetRules> {
    return when (rules) {
        ArbeidsuforhetRules.UKJENT_DIAGNOSEKODETYPE -> ukjentDiagnoseKodeType
        ArbeidsuforhetRules.ICPC_2_Z_DIAGNOSE -> icpc2ZDiagnose
        ArbeidsuforhetRules.HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER ->
            houvedDiagnoseEllerFraversgrunnMangler
        ArbeidsuforhetRules.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE -> ugyldigKodeVerkHouvedDiagnose
        ArbeidsuforhetRules.UGYLDIG_KODEVERK_FOR_BIDIAGNOSE -> ugyldigKodeVerkBiDiagnose
    }
}
