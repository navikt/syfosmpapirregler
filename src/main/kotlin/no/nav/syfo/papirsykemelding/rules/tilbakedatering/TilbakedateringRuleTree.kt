package no.nav.syfo.rules.tilbakedatering

import no.nav.syfo.model.Status
import no.nav.syfo.model.Status.MANUAL_PROCESSING
import no.nav.syfo.model.Status.OK
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.dsl.RuleNode
import no.nav.syfo.papirsykemelding.rules.dsl.tree
import no.nav.syfo.papirsykemelding.rules.tilbakedatering.Rule
import no.nav.syfo.papirsykemelding.rules.tilbakedatering.TilbakedateringRuleHit
import no.nav.syfo.papirsykemelding.rules.tilbakedatering.TilbakedateringRuleHit.INNTIL_30_DAGER
import no.nav.syfo.papirsykemelding.rules.tilbakedatering.TilbakedateringRuleHit.INNTIL_30_DAGER_MED_BEGRUNNELSE
import no.nav.syfo.papirsykemelding.rules.tilbakedatering.TilbakedateringRuleHit.INNTIL_8_DAGER
import no.nav.syfo.papirsykemelding.rules.tilbakedatering.TilbakedateringRuleHit.OVER_30_DAGER
import no.nav.syfo.papirsykemelding.rules.tilbakedatering.arbeidsgiverperiode
import no.nav.syfo.papirsykemelding.rules.tilbakedatering.begrunnelse_min_1_ord
import no.nav.syfo.papirsykemelding.rules.tilbakedatering.begrunnelse_min_3_ord
import no.nav.syfo.papirsykemelding.rules.tilbakedatering.ettersending
import no.nav.syfo.papirsykemelding.rules.tilbakedatering.forlengelse
import no.nav.syfo.papirsykemelding.rules.tilbakedatering.spesialisthelsetjenesten
import no.nav.syfo.papirsykemelding.rules.tilbakedatering.tilbakedatering
import no.nav.syfo.papirsykemelding.rules.tilbakedatering.tilbakedateringInntil30Dager
import no.nav.syfo.papirsykemelding.rules.tilbakedatering.tilbakedateringInntil8Dager
import no.nav.syfo.rules.tilbakedatering.TilbakedateringRules.ARBEIDSGIVERPERIODE
import no.nav.syfo.rules.tilbakedatering.TilbakedateringRules.BEGRUNNELSE_MIN_1_ORD
import no.nav.syfo.rules.tilbakedatering.TilbakedateringRules.BEGRUNNELSE_MIN_3_ORD
import no.nav.syfo.rules.tilbakedatering.TilbakedateringRules.ETTERSENDING
import no.nav.syfo.rules.tilbakedatering.TilbakedateringRules.FORLENGELSE
import no.nav.syfo.rules.tilbakedatering.TilbakedateringRules.SPESIALISTHELSETJENESTEN
import no.nav.syfo.rules.tilbakedatering.TilbakedateringRules.TILBAKEDATERING
import no.nav.syfo.rules.tilbakedatering.TilbakedateringRules.TILBAKEDATERT_INNTIL_30_DAGER
import no.nav.syfo.rules.tilbakedatering.TilbakedateringRules.TILBAKEDATERT_INNTIL_8_DAGER

enum class TilbakedateringRules {
    ARBEIDSGIVERPERIODE,
    BEGRUNNELSE_MIN_1_ORD,
    BEGRUNNELSE_MIN_3_ORD,
    ETTERSENDING,
    FORLENGELSE,
    SPESIALISTHELSETJENESTEN,
    TILBAKEDATERING,
    TILBAKEDATERT_INNTIL_8_DAGER,
    TILBAKEDATERT_INNTIL_30_DAGER,
}

val tilbakedateringRuleTree = tree<TilbakedateringRules, RuleResult>(TILBAKEDATERING) {
    yes(ETTERSENDING) {
        yes(OK)
        no(TILBAKEDATERT_INNTIL_8_DAGER) {
            yes(BEGRUNNELSE_MIN_1_ORD) {
                yes(OK)
                no(FORLENGELSE) {
                    yes(OK)
                    no(SPESIALISTHELSETJENESTEN) {
                        yes(OK)
                        no(MANUAL_PROCESSING, INNTIL_8_DAGER)
                    }
                }
            }
            no(TILBAKEDATERT_INNTIL_30_DAGER) {
                yes(BEGRUNNELSE_MIN_1_ORD) {
                    yes(FORLENGELSE) {
                        yes(OK)
                        no(ARBEIDSGIVERPERIODE) {
                            yes(OK)
                            no(SPESIALISTHELSETJENESTEN) {
                                yes(OK)
                                no(MANUAL_PROCESSING, INNTIL_30_DAGER_MED_BEGRUNNELSE)
                            }
                        }
                    }
                    no(SPESIALISTHELSETJENESTEN) {
                        yes(OK)
                        no(MANUAL_PROCESSING, INNTIL_30_DAGER)
                    }
                }
                no(MANUAL_PROCESSING, OVER_30_DAGER)
            }
        }
    }
    no(OK)
}

internal fun RuleNode<TilbakedateringRules, RuleResult>.yes(status: Status, ruleHit: TilbakedateringRuleHit? = null) {
    yes(RuleResult(status = status, ruleHit = ruleHit?.ruleHit))
}

internal fun RuleNode<TilbakedateringRules, RuleResult>.no(status: Status, ruleHit: TilbakedateringRuleHit? = null) {
    no(RuleResult(status = status, ruleHit = ruleHit?.ruleHit))
}

fun getRule(rules: TilbakedateringRules): Rule<TilbakedateringRules> {
    return when (rules) {
        ARBEIDSGIVERPERIODE -> arbeidsgiverperiode
        BEGRUNNELSE_MIN_1_ORD -> begrunnelse_min_1_ord
        BEGRUNNELSE_MIN_3_ORD -> begrunnelse_min_3_ord
        ETTERSENDING -> ettersending
        FORLENGELSE -> forlengelse
        SPESIALISTHELSETJENESTEN -> spesialisthelsetjenesten
        TILBAKEDATERING -> tilbakedatering
        TILBAKEDATERT_INNTIL_8_DAGER -> tilbakedateringInntil8Dager
        TILBAKEDATERT_INNTIL_30_DAGER -> tilbakedateringInntil30Dager
    }
}
