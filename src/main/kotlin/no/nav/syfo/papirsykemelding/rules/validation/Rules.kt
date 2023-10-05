package no.nav.syfo.papirsykemelding.rules.validation

import no.nav.syfo.model.Sykmelding
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.papirsykemelding.rules.dsl.RuleResult

typealias Rule<T> = (sykmelding: Sykmelding, ruleMetadata: RuleMetadata) -> RuleResult<T>

typealias ValidationRule = Rule<ValidationRules>

val ugyldingOrgNummerLengde: ValidationRule = { _, ruleMetadata ->
    val legekontorOrgnr = ruleMetadata.legekontorOrgnr

    val ugyldingOrgNummerLengde = legekontorOrgnr != null && legekontorOrgnr.length != 9
    RuleResult(
        ruleInputs =
            mapOf(
                "legekontorOrgnummer" to (legekontorOrgnr ?: ""),
                "ugyldingOrgNummerLengde" to ugyldingOrgNummerLengde
            ),
        rule = ValidationRules.UGYLDIG_ORGNR_LENGDE,
        ruleResult = ugyldingOrgNummerLengde,
    )
}
