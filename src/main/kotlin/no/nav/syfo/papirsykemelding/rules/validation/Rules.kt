package no.nav.syfo.papirsykemelding.rules.validation

import no.nav.syfo.model.Sykmelding
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.papirsykemelding.model.sortedTOMDate
import no.nav.syfo.papirsykemelding.rules.dsl.RuleResult
import no.nav.syfo.sm.Diagnosekoder
import no.nav.syfo.sm.isICPC2

typealias Rule<T> = (sykmelding: Sykmelding, ruleMetadata: RuleMetadata) -> RuleResult<T>

typealias ValidationRule = Rule<ValidationRules>

val ugyldingOrgNummerLengde: ValidationRule = { _, ruleMetadata ->
    val legekontorOrgnr = ruleMetadata.legekontorOrgnr

    val ugyldingOrgNummerLengde = legekontorOrgnr != null && legekontorOrgnr.length != 9

    RuleResult(
        ruleInputs = mapOf("ugyldingOrgNummerLengde" to ugyldingOrgNummerLengde),
        rule = ValidationRules.UGYLDIG_ORGNR_LENGDE,
        ruleResult = ugyldingOrgNummerLengde,
    )
}
