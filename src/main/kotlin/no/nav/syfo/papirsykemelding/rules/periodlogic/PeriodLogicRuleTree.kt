package no.nav.syfo.rules.periodlogic

import no.nav.syfo.model.Status
import no.nav.syfo.model.Status.MANUAL_PROCESSING
import no.nav.syfo.model.Status.OK
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.dsl.RuleNode
import no.nav.syfo.papirsykemelding.rules.dsl.tree
import no.nav.syfo.papirsykemelding.rules.periodlogic.PeriodLogicRuleHit
import no.nav.syfo.papirsykemelding.rules.periodlogic.Rule
import no.nav.syfo.papirsykemelding.rules.periodlogic.avventendeKombinert
import no.nav.syfo.papirsykemelding.rules.periodlogic.avventendeOver16Dager
import no.nav.syfo.papirsykemelding.rules.periodlogic.behandslingsDatoEtterMottatDato
import no.nav.syfo.papirsykemelding.rules.periodlogic.forMangeBehandlingsDagerPrUke
import no.nav.syfo.papirsykemelding.rules.periodlogic.fraDatoEtterTilDato
import no.nav.syfo.papirsykemelding.rules.periodlogic.fremdatertOver30Dager
import no.nav.syfo.papirsykemelding.rules.periodlogic.gradertOver99Prosent
import no.nav.syfo.papirsykemelding.rules.periodlogic.ikkeDefinertPeriode
import no.nav.syfo.papirsykemelding.rules.periodlogic.inneholderBehandlingsDager
import no.nav.syfo.papirsykemelding.rules.periodlogic.manglendeInnspillArbeidsgiver
import no.nav.syfo.papirsykemelding.rules.periodlogic.oppholdMellomPerioder
import no.nav.syfo.papirsykemelding.rules.periodlogic.overlappendePerioder
import no.nav.syfo.papirsykemelding.rules.periodlogic.periodeMangler
import no.nav.syfo.papirsykemelding.rules.periodlogic.tilbakeDatertOver3Ar
import no.nav.syfo.papirsykemelding.rules.periodlogic.varighetOver1AAr

enum class PeriodLogicRules {
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

val periodLogicRuleTree = tree<PeriodLogicRules, RuleResult>(PeriodLogicRules.PERIODER_MANGLER) {
    yes(MANUAL_PROCESSING, PeriodLogicRuleHit.PERIODER_MANGLER)
    no(PeriodLogicRules.FRADATO_ETTER_TILDATO) {
        yes(MANUAL_PROCESSING, PeriodLogicRuleHit.FRADATO_ETTER_TILDATO)
        no(PeriodLogicRules.OVERLAPPENDE_PERIODER) {
            yes(MANUAL_PROCESSING, PeriodLogicRuleHit.OVERLAPPENDE_PERIODER)
            no(PeriodLogicRules.OPPHOLD_MELLOM_PERIODER) {
                yes(MANUAL_PROCESSING, PeriodLogicRuleHit.OPPHOLD_MELLOM_PERIODER)
                no(PeriodLogicRules.TILBAKEDATERT_MER_ENN_3_AR) {
                    yes(MANUAL_PROCESSING, PeriodLogicRuleHit.TILBAKEDATERT_MER_ENN_3_AR)
                    no(PeriodLogicRules.FREMDATERT) {
                        yes(MANUAL_PROCESSING, PeriodLogicRuleHit.FREMDATERT)
                        no(PeriodLogicRules.TOTAL_VARIGHET_OVER_ETT_AAR) {
                            yes(MANUAL_PROCESSING, PeriodLogicRuleHit.TOTAL_VARIGHET_OVER_ETT_AAR)
                            no(PeriodLogicRules.AVVENTENDE_SYKMELDING_KOMBINERT) {
                                yes(MANUAL_PROCESSING, PeriodLogicRuleHit.AVVENTENDE_SYKMELDING_KOMBINERT)
                                no(PeriodLogicRules.MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER) {
                                    yes(MANUAL_PROCESSING, PeriodLogicRuleHit.MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER)
                                    no(PeriodLogicRules.AVVENTENDE_SYKMELDING_OVER_16_DAGER) {
                                        yes(MANUAL_PROCESSING, PeriodLogicRuleHit.AVVENTENDE_SYKMELDING_OVER_16_DAGER)
                                        no(PeriodLogicRules.FOR_MANGE_BEHANDLINGSDAGER_PER_UKE) {
                                            yes(MANUAL_PROCESSING, PeriodLogicRuleHit.FOR_MANGE_BEHANDLINGSDAGER_PER_UKE)
                                            no(PeriodLogicRules.GRADERT_SYKMELDING_OVER_99_PROSENT) {
                                                yes(MANUAL_PROCESSING, PeriodLogicRuleHit.GRADERT_SYKMELDING_OVER_99_PROSENT)
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

internal fun RuleNode<PeriodLogicRules, RuleResult>.yes(status: Status, ruleHit: PeriodLogicRuleHit? = null) {
    yes(RuleResult(status, ruleHit?.ruleHit))
}

internal fun RuleNode<PeriodLogicRules, RuleResult>.no(status: Status, ruleHit: PeriodLogicRuleHit? = null) {
    no(RuleResult(status, ruleHit?.ruleHit))
}

fun getRule(rules: PeriodLogicRules): Rule<PeriodLogicRules> {
    return when (rules) {
        PeriodLogicRules.PERIODER_MANGLER -> periodeMangler
        PeriodLogicRules.FRADATO_ETTER_TILDATO -> fraDatoEtterTilDato
        PeriodLogicRules.OVERLAPPENDE_PERIODER -> overlappendePerioder
        PeriodLogicRules.OPPHOLD_MELLOM_PERIODER -> oppholdMellomPerioder
        PeriodLogicRules.IKKE_DEFINERT_PERIODE -> ikkeDefinertPeriode
        PeriodLogicRules.FREMDATERT -> fremdatertOver30Dager
        PeriodLogicRules.TOTAL_VARIGHET_OVER_ETT_AAR -> varighetOver1AAr
        PeriodLogicRules.BEHANDLINGSDATO_ETTER_MOTTATTDATO -> behandslingsDatoEtterMottatDato
        PeriodLogicRules.AVVENTENDE_SYKMELDING_KOMBINERT -> avventendeKombinert
        PeriodLogicRules.MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER -> manglendeInnspillArbeidsgiver
        PeriodLogicRules.AVVENTENDE_SYKMELDING_OVER_16_DAGER -> avventendeOver16Dager
        PeriodLogicRules.FOR_MANGE_BEHANDLINGSDAGER_PER_UKE -> forMangeBehandlingsDagerPrUke
        PeriodLogicRules.GRADERT_SYKMELDING_OVER_99_PROSENT -> gradertOver99Prosent
        PeriodLogicRules.SYKMELDING_MED_BEHANDLINGSDAGER -> inneholderBehandlingsDager
        PeriodLogicRules.TILBAKEDATERT_MER_ENN_3_AR -> tilbakeDatertOver3Ar
    }
}
