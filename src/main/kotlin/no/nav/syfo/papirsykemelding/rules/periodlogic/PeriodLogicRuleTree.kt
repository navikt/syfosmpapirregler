package no.nav.syfo.papirsykemelding.rules.periodlogic

import no.nav.syfo.model.Status
import no.nav.syfo.model.Status.MANUAL_PROCESSING
import no.nav.syfo.model.Status.OK
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.common.UtenJuridisk
import no.nav.syfo.papirsykemelding.rules.dsl.RuleNode
import no.nav.syfo.papirsykemelding.rules.dsl.tree

enum class PeriodLogicRules {
    PERIODER_MANGLER,
    FRADATO_ETTER_TILDATO,
    OVERLAPPENDE_PERIODER,
    OPPHOLD_MELLOM_PERIODER,
    AVVENTENDE_SYKMELDING_KOMBINERT,
    MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER,
    AVVENTENDE_SYKMELDING_OVER_16_DAGER,
    FOR_MANGE_BEHANDLINGSDAGER_PER_UKE,
    GRADERT_SYKMELDING_OVER_99_PROSENT,
    GRADERT_SYKMELDING_0_PROSENT,
}

val periodLogicRuleTree =
    tree<PeriodLogicRules, RuleResult>(PeriodLogicRules.PERIODER_MANGLER) {
        yes(MANUAL_PROCESSING, PeriodLogicRuleHit.PERIODER_MANGLER)
        no(PeriodLogicRules.FRADATO_ETTER_TILDATO) {
            yes(MANUAL_PROCESSING, PeriodLogicRuleHit.FRADATO_ETTER_TILDATO)
            no(PeriodLogicRules.OVERLAPPENDE_PERIODER) {
                yes(MANUAL_PROCESSING, PeriodLogicRuleHit.OVERLAPPENDE_PERIODER)
                no(PeriodLogicRules.OPPHOLD_MELLOM_PERIODER) {
                    yes(MANUAL_PROCESSING, PeriodLogicRuleHit.OPPHOLD_MELLOM_PERIODER)
                    no(PeriodLogicRules.AVVENTENDE_SYKMELDING_KOMBINERT) {
                        yes(MANUAL_PROCESSING, PeriodLogicRuleHit.AVVENTENDE_SYKMELDING_KOMBINERT)
                        no(PeriodLogicRules.MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER) {
                            yes(
                                MANUAL_PROCESSING,
                                PeriodLogicRuleHit.MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER
                            )
                            no(PeriodLogicRules.AVVENTENDE_SYKMELDING_OVER_16_DAGER) {
                                yes(
                                    MANUAL_PROCESSING,
                                    PeriodLogicRuleHit.AVVENTENDE_SYKMELDING_OVER_16_DAGER
                                )
                                no(PeriodLogicRules.FOR_MANGE_BEHANDLINGSDAGER_PER_UKE) {
                                    yes(
                                        MANUAL_PROCESSING,
                                        PeriodLogicRuleHit.FOR_MANGE_BEHANDLINGSDAGER_PER_UKE
                                    )
                                    no(PeriodLogicRules.GRADERT_SYKMELDING_OVER_99_PROSENT) {
                                        yes(
                                            MANUAL_PROCESSING,
                                            PeriodLogicRuleHit.GRADERT_SYKMELDING_OVER_99_PROSENT
                                        )
                                        no(PeriodLogicRules.GRADERT_SYKMELDING_0_PROSENT) {
                                            yes(
                                                MANUAL_PROCESSING,
                                                PeriodLogicRuleHit.GRADERT_SYKMELDING_O_PROSENT
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
    } to UtenJuridisk

internal fun RuleNode<PeriodLogicRules, RuleResult>.yes(
    status: Status,
    ruleHit: PeriodLogicRuleHit? = null
) {
    yes(RuleResult(status, ruleHit?.ruleHit))
}

internal fun RuleNode<PeriodLogicRules, RuleResult>.no(
    status: Status,
    ruleHit: PeriodLogicRuleHit? = null
) {
    no(RuleResult(status, ruleHit?.ruleHit))
}

fun getRule(rules: PeriodLogicRules): Rule<PeriodLogicRules> {
    return when (rules) {
        PeriodLogicRules.PERIODER_MANGLER -> periodeMangler
        PeriodLogicRules.FRADATO_ETTER_TILDATO -> fraDatoEtterTilDato
        PeriodLogicRules.OVERLAPPENDE_PERIODER -> overlappendePerioder
        PeriodLogicRules.OPPHOLD_MELLOM_PERIODER -> oppholdMellomPerioder
        PeriodLogicRules.AVVENTENDE_SYKMELDING_KOMBINERT -> avventendeKombinert
        PeriodLogicRules.MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER -> manglendeInnspillArbeidsgiver
        PeriodLogicRules.AVVENTENDE_SYKMELDING_OVER_16_DAGER -> avventendeOver16Dager
        PeriodLogicRules.FOR_MANGE_BEHANDLINGSDAGER_PER_UKE -> forMangeBehandlingsDagerPrUke
        PeriodLogicRules.GRADERT_SYKMELDING_OVER_99_PROSENT -> gradertOver99Prosent
        PeriodLogicRules.GRADERT_SYKMELDING_0_PROSENT -> gradert0Prosent
    }
}
