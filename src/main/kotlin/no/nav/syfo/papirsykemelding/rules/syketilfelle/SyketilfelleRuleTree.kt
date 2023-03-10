package no.nav.syfo.papirsykemelding.rules.syketilfelle

import no.nav.syfo.model.Status
import no.nav.syfo.model.Status.INVALID
import no.nav.syfo.model.Status.MANUAL_PROCESSING
import no.nav.syfo.model.Status.OK
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.dsl.RuleNode
import no.nav.syfo.papirsykemelding.rules.dsl.tree

enum class SyketilfelleRules {
    PERIODER_MANGLER,
    FRADATO_ETTER_TILDATO,
    OVERLAPPENDE_PERIODER,
    OPPHOLD_MELLOM_PERIODER,
    IKKE_DEFINERT_PERIODE,
    TILBAKEDATERT_MER_ENN_3_AR,
    FREMDATERT,
    TOTAL_VARIGHET_OVER_ETT_AAR,
    BEHANDLINGSDATO_ETTER_MOTTATTDATO,
    AVVENTENDE_SYKMELDING_KOMBINERT,
    MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER,
    AVVENTENDE_SYKMELDING_OVER_16_DAGER,
    FOR_MANGE_BEHANDLINGSDAGER_PER_UKE,
    GRADERT_SYKMELDING_OVER_99_PROSENT,
    SYKMELDING_MED_BEHANDLINGSDAGER
}

val syketilfelleRuleTree = tree<SyketilfelleRules, RuleResult>(SyketilfelleRules.PERIODER_MANGLER) {
    yes(INVALID, SyketilfelleRuleHit.PERIODER_MANGLER)
    no(SyketilfelleRules.FRADATO_ETTER_TILDATO) {
        yes(INVALID, SyketilfelleRuleHit.FRADATO_ETTER_TILDATO)
        no(SyketilfelleRules.OVERLAPPENDE_PERIODER) {
            yes(INVALID, SyketilfelleRuleHit.OVERLAPPENDE_PERIODER)
            no(SyketilfelleRules.OPPHOLD_MELLOM_PERIODER) {
                yes(INVALID, SyketilfelleRuleHit.OPPHOLD_MELLOM_PERIODER)
                no(SyketilfelleRules.IKKE_DEFINERT_PERIODE) {
                    yes(INVALID, SyketilfelleRuleHit.IKKE_DEFINERT_PERIODE)
                    no(SyketilfelleRules.TILBAKEDATERT_MER_ENN_3_AR) {
                        yes(INVALID, SyketilfelleRuleHit.TILBAKEDATERT_MER_ENN_3_AR)
                        no(SyketilfelleRules.FREMDATERT) {
                            yes(INVALID, SyketilfelleRuleHit.FREMDATERT)
                            no(SyketilfelleRules.TOTAL_VARIGHET_OVER_ETT_AAR) {
                                yes(INVALID, SyketilfelleRuleHit.TOTAL_VARIGHET_OVER_ETT_AAR)
                                no(SyketilfelleRules.BEHANDLINGSDATO_ETTER_MOTTATTDATO) {
                                    yes(INVALID, SyketilfelleRuleHit.BEHANDLINGSDATO_ETTER_MOTTATTDATO)
                                    no(SyketilfelleRules.AVVENTENDE_SYKMELDING_KOMBINERT) {
                                        yes(INVALID, SyketilfelleRuleHit.AVVENTENDE_SYKMELDING_KOMBINERT)
                                        no(SyketilfelleRules.MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER) {
                                            yes(INVALID, SyketilfelleRuleHit.MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER)
                                            no(SyketilfelleRules.AVVENTENDE_SYKMELDING_OVER_16_DAGER) {
                                                yes(INVALID, SyketilfelleRuleHit.AVVENTENDE_SYKMELDING_OVER_16_DAGER)
                                                no(SyketilfelleRules.FOR_MANGE_BEHANDLINGSDAGER_PER_UKE) {
                                                    yes(INVALID, SyketilfelleRuleHit.FOR_MANGE_BEHANDLINGSDAGER_PER_UKE)
                                                    no(SyketilfelleRules.GRADERT_SYKMELDING_OVER_99_PROSENT) {
                                                        yes(INVALID, SyketilfelleRuleHit.GRADERT_SYKMELDING_OVER_99_PROSENT)
                                                        no(SyketilfelleRules.SYKMELDING_MED_BEHANDLINGSDAGER) {
                                                            yes(
                                                                MANUAL_PROCESSING,
                                                                SyketilfelleRuleHit.SYKMELDING_MED_BEHANDLINGSDAGER
                                                            )
                                                            no(OK)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun RuleNode<SyketilfelleRules, RuleResult>.yes(status: Status, ruleHit: SyketilfelleRuleHit? = null) {
    yes(RuleResult(status, ruleHit?.ruleHit))
}

internal fun RuleNode<SyketilfelleRules, RuleResult>.no(status: Status, ruleHit: SyketilfelleRuleHit? = null) {
    no(RuleResult(status, ruleHit?.ruleHit))
}

fun getRule(rules: SyketilfelleRules): Rule<SyketilfelleRules> {
    return when (rules) {
        SyketilfelleRules.PERIODER_MANGLER -> periodeMangler
        SyketilfelleRules.FRADATO_ETTER_TILDATO -> fraDatoEtterTilDato
        SyketilfelleRules.OVERLAPPENDE_PERIODER -> overlappendePerioder
        SyketilfelleRules.OPPHOLD_MELLOM_PERIODER -> oppholdMellomPerioder
        SyketilfelleRules.IKKE_DEFINERT_PERIODE -> ikkeDefinertPeriode
        SyketilfelleRules.FREMDATERT -> fremdatertOver30Dager
        SyketilfelleRules.TOTAL_VARIGHET_OVER_ETT_AAR -> varighetOver1AAr
        SyketilfelleRules.BEHANDLINGSDATO_ETTER_MOTTATTDATO -> behandslingsDatoEtterMottatDato
        SyketilfelleRules.AVVENTENDE_SYKMELDING_KOMBINERT -> avventendeKombinert
        SyketilfelleRules.MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER -> manglendeInnspillArbeidsgiver
        SyketilfelleRules.AVVENTENDE_SYKMELDING_OVER_16_DAGER -> avventendeOver16Dager
        SyketilfelleRules.FOR_MANGE_BEHANDLINGSDAGER_PER_UKE -> forMangeBehandlingsDagerPrUke
        SyketilfelleRules.GRADERT_SYKMELDING_OVER_99_PROSENT -> gradertOver99Prosent
        SyketilfelleRules.SYKMELDING_MED_BEHANDLINGSDAGER -> inneholderBehandlingsDager
        SyketilfelleRules.TILBAKEDATERT_MER_ENN_3_AR -> tilbakeDatertOver3Ar
    }
}
