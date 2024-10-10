package no.nav.syfo.papirsykemelding.rules.arbeidsuforhet

import no.nav.syfo.model.Status
import no.nav.syfo.model.Status.MANUAL_PROCESSING
import no.nav.syfo.model.Status.OK
import no.nav.syfo.model.juridisk.JuridiskHenvisning
import no.nav.syfo.model.juridisk.Lovverk
import no.nav.syfo.papirsykemelding.rules.common.MedJuridisk
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.dsl.RuleNode
import no.nav.syfo.papirsykemelding.rules.dsl.tree

enum class ArbeidsuforhetRules {
    ICPC_2_Z_DIAGNOSE,
    HOVEDDIAGNOSE_MANGLER,
    FRAVAERSGRUNN_MANGLER,
    UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE,
    UGYLDIG_KODEVERK_FOR_BIDIAGNOSE,
}

val arbeidsuforhetRuleTreeNew =
    tree<ArbeidsuforhetRules, RuleResult>(ArbeidsuforhetRules.HOVEDDIAGNOSE_MANGLER) {
        yes(ArbeidsuforhetRules.FRAVAERSGRUNN_MANGLER) {
            yes(MANUAL_PROCESSING, ArbeidsuforhetRuleHit.FRAVAERSGRUNN_MANGLER)
            no(ArbeidsuforhetRules.UGYLDIG_KODEVERK_FOR_BIDIAGNOSE) {
                yes(MANUAL_PROCESSING, ArbeidsuforhetRuleHit.UGYLDIG_KODEVERK_FOR_BIDIAGNOSE)
                no(OK)
            }
        }
        no(ArbeidsuforhetRules.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE) {
            yes(MANUAL_PROCESSING, ArbeidsuforhetRuleHit.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE)
            no(ArbeidsuforhetRules.ICPC_2_Z_DIAGNOSE) {
                yes(MANUAL_PROCESSING, ArbeidsuforhetRuleHit.ICPC_2_Z_DIAGNOSE)
                no(ArbeidsuforhetRules.UGYLDIG_KODEVERK_FOR_BIDIAGNOSE) {
                    yes(MANUAL_PROCESSING, ArbeidsuforhetRuleHit.UGYLDIG_KODEVERK_FOR_BIDIAGNOSE)
                    no(OK)
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
        ArbeidsuforhetRules.HOVEDDIAGNOSE_MANGLER -> manglerHovedDiagnose
        ArbeidsuforhetRules.ICPC_2_Z_DIAGNOSE -> icpc2ZDiagnose
        ArbeidsuforhetRules.FRAVAERSGRUNN_MANGLER -> manglerAnnenFravarsArsak
        ArbeidsuforhetRules.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE -> ugyldigKodeVerkHovedDiagnose
        ArbeidsuforhetRules.UGYLDIG_KODEVERK_FOR_BIDIAGNOSE -> ugyldigKodeVerkBiDiagnose
    }
}
