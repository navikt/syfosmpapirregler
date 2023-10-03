package no.nav.syfo.papirsykemelding.rules.patientunder13

import no.nav.syfo.model.Sykmelding
import no.nav.syfo.papirsykemelding.model.sortedTOMDate
import no.nav.syfo.papirsykemelding.rules.dsl.RuleResult
import no.nav.syfo.papirsykemelding.service.RuleMetadataSykmelding

typealias Rule<T> =
    (sykmelding: Sykmelding, ruleMetadataSykmelding: RuleMetadataSykmelding) -> RuleResult<T>

typealias PatientAgeOver70Rule = Rule<PatientAgeUnder13Rules>

val pasientUnder13Aar: PatientAgeOver70Rule = { sykmelding, ruleMetadata ->
    val sisteTomDato = sykmelding.perioder.sortedTOMDate().last()
    val pasientFodselsdato = ruleMetadata.ruleMetadata.pasientFodselsdato

    val pasientUnder13Aar = sisteTomDato < pasientFodselsdato.plusYears(13)

    RuleResult(
        ruleInputs = mapOf("pasientUnder13Aar" to pasientUnder13Aar),
        rule = PatientAgeUnder13Rules.PASIENT_YNGRE_ENN_13,
        ruleResult = pasientUnder13Aar,
    )
}
