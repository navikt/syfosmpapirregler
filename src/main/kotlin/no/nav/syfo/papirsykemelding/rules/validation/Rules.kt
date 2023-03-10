package no.nav.syfo.papirsykemelding.rules.validation

import no.nav.syfo.model.Sykmelding
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.papirsykemelding.model.sortedTOMDate
import no.nav.syfo.papirsykemelding.rules.dsl.RuleResult

typealias Rule<T> = (sykmelding: Sykmelding, ruleMetadata: RuleMetadata) -> RuleResult<T>
typealias ValidationRule = Rule<ValidationRules>

val pasientUnder13Aar: ValidationRule = { sykmelding, ruleMetadata ->

    val sisteTomDato = sykmelding.perioder.sortedTOMDate().last()
    val pasientFodselsdato = ruleMetadata.pasientFodselsdato

    val pasientUnder13Aar = sisteTomDato < pasientFodselsdato.plusYears(13)

    RuleResult(
        ruleInputs = mapOf("pasientUnder13Aar" to pasientUnder13Aar),
        rule = ValidationRules.PASIENT_YNGRE_ENN_13,
        ruleResult = pasientUnder13Aar
    )
}

val ugyldigRegelsettversjon: ValidationRule = { _, ruleMetadata ->
    val rulesetVersion = ruleMetadata.rulesetVersion

    val ugyldigRegelsettversjon = rulesetVersion !in arrayOf(null, "", "1", "2", "3")

    RuleResult(
        ruleInputs = mapOf("ugyldigRegelsettversjon" to ugyldigRegelsettversjon),
        rule = ValidationRules.UGYLDIG_REGELSETTVERSJON,
        ruleResult = ugyldigRegelsettversjon
    )
}

val ugyldingOrgNummerLengde: ValidationRule = { _, ruleMetadata ->
    val legekontorOrgnr = ruleMetadata.legekontorOrgnr

    val ugyldingOrgNummerLengde = legekontorOrgnr != null && legekontorOrgnr.length != 9

    RuleResult(
        ruleInputs = mapOf("ugyldingOrgNummerLengde" to ugyldingOrgNummerLengde),
        rule = ValidationRules.UGYLDIG_ORGNR_LENGDE,
        ruleResult = ugyldingOrgNummerLengde
    )
}

val behandlerSammeSomPasient: ValidationRule = { sykmelding, ruleMetadata ->
    val behandlerFnr = sykmelding.behandler.fnr
    val pasientFodselsNummer = ruleMetadata.patientPersonNumber

    val behandlerSammeSomPasient = behandlerFnr == pasientFodselsNummer

    RuleResult(
        ruleInputs = mapOf("behandlerSammeSomPasient" to behandlerSammeSomPasient),
        rule = ValidationRules.BEHANDLER_FNR_ER_SAMME_SOM_PASIENT_FNR,
        ruleResult = behandlerSammeSomPasient
    )
}
