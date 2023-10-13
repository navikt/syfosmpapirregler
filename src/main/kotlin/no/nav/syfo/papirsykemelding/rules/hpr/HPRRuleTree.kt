package no.nav.syfo.papirsykemelding.rules.hpr

import no.nav.syfo.model.Status
import no.nav.syfo.model.Status.*
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.dsl.RuleNode
import no.nav.syfo.papirsykemelding.rules.dsl.tree
import no.nav.syfo.papirsykemelding.rules.hpr.HPRRuleHit.*
import no.nav.syfo.papirsykemelding.rules.hpr.HPRRules.*

enum class HPRRules {
    BEHANDLER_GYLIDG_I_HPR,
    BEHANDLER_HAR_AUTORISASJON_I_HPR,
    BEHANDLER_ER_LEGE_I_HPR,
    BEHANDLER_ER_TANNLEGE_I_HPR,
    BEHANDLER_ER_MANUELLTERAPEUT_I_HPR,
    BEHANDLER_ER_FT_MED_TILLEGSKOMPETANSE_I_HPR,
    BEHANDLER_ER_KI_MED_TILLEGSKOMPETANSE_I_HPR,
    SYKEFRAVAR_OVER_12_UKER,
}

val hprRuleTree =
    tree<HPRRules, RuleResult>(BEHANDLER_GYLIDG_I_HPR) {
        no(MANUAL_PROCESSING, BEHANDLER_IKKE_GYLDIG_I_HPR)
        yes(BEHANDLER_HAR_AUTORISASJON_I_HPR) {
            no(MANUAL_PROCESSING, BEHANDLER_MANGLER_AUTORISASJON_I_HPR)
            yes(BEHANDLER_ER_LEGE_I_HPR) {
                yes(OK)
                no(BEHANDLER_ER_TANNLEGE_I_HPR) {
                    yes(OK)
                    no(BEHANDLER_ER_MANUELLTERAPEUT_I_HPR) {
                        yes(SYKEFRAVAR_OVER_12_UKER, checkSykefravarOver12Uker())
                        no(BEHANDLER_ER_FT_MED_TILLEGSKOMPETANSE_I_HPR) {
                            yes(SYKEFRAVAR_OVER_12_UKER, checkSykefravarOver12Uker())
                            no(BEHANDLER_ER_KI_MED_TILLEGSKOMPETANSE_I_HPR) {
                                yes(SYKEFRAVAR_OVER_12_UKER, checkSykefravarOver12Uker())
                                no(MANUAL_PROCESSING, BEHANDLER_IKKE_LE_KI_MT_TL_FT_I_HPR)
                            }
                        }
                    }
                }
            }
        }
    }

private fun checkSykefravarOver12Uker(): RuleNode<HPRRules, RuleResult>.() -> Unit = {
    yes(MANUAL_PROCESSING, BEHANDLER_MT_FT_KI_OVER_12_UKER)
    no(OK)
}

internal fun RuleNode<HPRRules, RuleResult>.yes(status: Status, ruleHit: HPRRuleHit? = null) {
    yes(RuleResult(status, ruleHit?.ruleHit))
}

internal fun RuleNode<HPRRules, RuleResult>.no(status: Status, ruleHit: HPRRuleHit? = null) {
    no(RuleResult(status, ruleHit?.ruleHit))
}

fun getRule(rules: HPRRules): Rule<HPRRules> {
    return when (rules) {
        HPRRules.BEHANDLER_GYLIDG_I_HPR -> behanderGyldigHPR(rules)
        HPRRules.BEHANDLER_HAR_AUTORISASJON_I_HPR -> behandlerHarAutorisasjon(rules)
        HPRRules.BEHANDLER_ER_LEGE_I_HPR -> behandlerErLege(rules)
        HPRRules.BEHANDLER_ER_TANNLEGE_I_HPR -> behandlerErTannlege(rules)
        HPRRules.BEHANDLER_ER_MANUELLTERAPEUT_I_HPR -> behandlerErManuellterapeut(rules)
        HPRRules.BEHANDLER_ER_FT_MED_TILLEGSKOMPETANSE_I_HPR ->
            behandlerErFTMedTilligskompetanseSykmelding(rules)
        HPRRules.BEHANDLER_ER_KI_MED_TILLEGSKOMPETANSE_I_HPR ->
            behandlerErKIMedTilligskompetanseSykmelding(rules)
        HPRRules.SYKEFRAVAR_OVER_12_UKER -> sykefravarOver12Uker(rules)
    }
}
