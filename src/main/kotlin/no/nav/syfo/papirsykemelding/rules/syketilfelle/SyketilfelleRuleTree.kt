package no.nav.syfo.papirsykemelding.rules.syketilfelle

import no.nav.syfo.model.Status
import no.nav.syfo.model.Status.MANUAL_PROCESSING
import no.nav.syfo.model.Status.OK
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.dsl.RuleNode
import no.nav.syfo.papirsykemelding.rules.dsl.tree


enum class SyketilfelleRules {
    TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING,
    TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING_MED_BEGRUNNELSE,
    TILBAKEDATERT_INNTIL_8_DAGER_UTEN_KONTAKTDATO_OG_BEGRUNNELSE,
    TILBAKEDATERT_FORLENGELSE_OVER_1_MND,
    TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE
}

val syketilfelleRuleTree = tree<SyketilfelleRules, RuleResult>(SyketilfelleRules.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING) {
    yes(MANUAL_PROCESSING, SyketilfelleRuleHit.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING)
    no(SyketilfelleRules.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING_MED_BEGRUNNELSE) {
        yes(MANUAL_PROCESSING, SyketilfelleRuleHit.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING_MED_BEGRUNNELSE)
        no(SyketilfelleRules.TILBAKEDATERT_INNTIL_8_DAGER_UTEN_KONTAKTDATO_OG_BEGRUNNELSE) {
            yes(MANUAL_PROCESSING, SyketilfelleRuleHit.TILBAKEDATERT_INNTIL_8_DAGER_UTEN_KONTAKTDATO_OG_BEGRUNNELSE)
            no(SyketilfelleRules.TILBAKEDATERT_FORLENGELSE_OVER_1_MND) {
                yes(MANUAL_PROCESSING, SyketilfelleRuleHit.TILBAKEDATERT_FORLENGELSE_OVER_1_MND)
                no(SyketilfelleRules.TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE) {
                    yes(MANUAL_PROCESSING, SyketilfelleRuleHit.TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE)
                    no(OK)
                }
            }
        }
    }
}

internal fun RuleNode<SyketilfelleRules, RuleResult>.yes(status: Status, ruleHit: SyketilfelleRuleHit? = null) {
    yes(RuleResult(status = status, ruleHit = ruleHit?.ruleHit))
}

internal fun RuleNode<SyketilfelleRules, RuleResult>.no(status: Status, ruleHit: SyketilfelleRuleHit? = null) {
    no(RuleResult(status = status, ruleHit = ruleHit?.ruleHit))
}

fun getRule(rules: SyketilfelleRules): Rule<SyketilfelleRules> {
    return when (rules) {
        SyketilfelleRules.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING -> tilbakedatermerenn8dagerforstesykmelding
        SyketilfelleRules.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING_MED_BEGRUNNELSE -> tilbakedatermerenn8dagerforstesykmeldingmedbegrunnelse
        SyketilfelleRules.TILBAKEDATERT_INNTIL_8_DAGER_UTEN_KONTAKTDATO_OG_BEGRUNNELSE -> tilbakedateertintall8dagerutenkontakdaoogbegrunnelse
        SyketilfelleRules.TILBAKEDATERT_FORLENGELSE_OVER_1_MND -> tilbakedatertforlengelseover1mnd
        SyketilfelleRules.TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE -> tilbakedertmedbegrunnelseforlengelse

    }
}
