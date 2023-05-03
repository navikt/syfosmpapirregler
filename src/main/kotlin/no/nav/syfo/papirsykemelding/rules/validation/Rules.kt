package no.nav.syfo.papirsykemelding.rules.validation

import no.nav.syfo.model.Sykmelding
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.papirsykemelding.model.sortedTOMDate
import no.nav.syfo.papirsykemelding.rules.dsl.RuleResult
import no.nav.syfo.sm.Diagnosekoder
import no.nav.syfo.sm.isICPC2

typealias Rule<T> = (sykmelding: Sykmelding, ruleMetadata: RuleMetadata) -> RuleResult<T>
typealias ValidationRule = Rule<ValidationRules>

val pasientUnder13Aar: ValidationRule = { sykmelding, ruleMetadata ->

    val sisteTomDato = sykmelding.perioder.sortedTOMDate().last()
    val pasientFodselsdato = ruleMetadata.pasientFodselsdato

    val pasientUnder13Aar = sisteTomDato < pasientFodselsdato.plusYears(13)

    RuleResult(
        ruleInputs = mapOf("pasientUnder13Aar" to pasientUnder13Aar),
        rule = ValidationRules.PASIENT_YNGRE_ENN_13,
        ruleResult = pasientUnder13Aar,
    )
}

val pasienteldreenn70Aar: ValidationRule = { sykmelding, ruleMetadata ->
    val sisteTomDato = sykmelding.perioder.sortedTOMDate().last()
    val pasientFodselsdato = ruleMetadata.pasientFodselsdato

    val pasientOver70Aar = sisteTomDato > pasientFodselsdato.plusYears(70)

    RuleResult(
        ruleInputs = mapOf("pasientOver70Aar" to pasientOver70Aar),
        rule = ValidationRules.PASIENT_ELDRE_ENN_70,
        ruleResult = pasientOver70Aar,
    )
}
val ukjentdiagnosekodetype: ValidationRule = { sykmelding, _ ->
    val hoveddiagnose = sykmelding.medisinskVurdering.hovedDiagnose

    RuleResult(
        ruleInputs = mapOf("hoveddiagnose" to (hoveddiagnose ?: "")),
        rule = ValidationRules.UKJENT_DIAGNOSEKODETYPE,
        ruleResult = hoveddiagnose != null && hoveddiagnose.system !in Diagnosekoder,
    )
}

val icpc2zdiagnose: ValidationRule = { sykmelding, _ ->
    val hoveddiagnose = sykmelding.medisinskVurdering.hovedDiagnose

    RuleResult(
        ruleInputs = mapOf("hoveddiagnose" to (hoveddiagnose ?: "")),
        rule = ValidationRules.ICPC_2_Z_DIAGNOSE,
        ruleResult = hoveddiagnose != null && hoveddiagnose.isICPC2() && hoveddiagnose.kode.startsWith("Z"),
    )
}

val houveddiagnsoeellerfravaergrunnmangler: ValidationRule = { sykmelding, _ ->
    val annenFraversArsak = sykmelding.medisinskVurdering.annenFraversArsak
    val hoveddiagnose = sykmelding.medisinskVurdering.hovedDiagnose

    RuleResult(
        ruleInputs = mapOf(
            "hoveddiagnose" to (hoveddiagnose ?: ""),
            "annenFraversArsak" to (annenFraversArsak ?: ""),
        ),
        rule = ValidationRules.HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER,
        ruleResult = annenFraversArsak == null && hoveddiagnose == null,
    )
}

val ugyldigkodeverkforhouveddiagnose: ValidationRule = { sykmelding, _ ->
    val hoveddiagnose = sykmelding.medisinskVurdering.hovedDiagnose

    RuleResult(
        ruleInputs = mapOf("hoveddiagnose" to (hoveddiagnose ?: "")),
        rule = ValidationRules.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE,
        ruleResult = if (hoveddiagnose == null) {
            false
        } else {
            hoveddiagnose.system !in arrayOf(
                Diagnosekoder.ICPC2_CODE,
                Diagnosekoder.ICD10_CODE,
            ) || !hoveddiagnose.let { diagnose ->
                if (diagnose.isICPC2()) {
                    Diagnosekoder.icpc2.containsKey(diagnose.kode)
                } else {
                    Diagnosekoder.icd10.containsKey(diagnose.kode)
                }
            }
        },
    )
}

val ugyldigkodeverkforbidiagnose: ValidationRule = { sykmelding, _ ->
    val biDiagnoser = sykmelding.medisinskVurdering.biDiagnoser

    RuleResult(
        ruleInputs = mapOf("biDiagnoser" to biDiagnoser),
        rule = ValidationRules.UGYLDIG_KODEVERK_FOR_BIDIAGNOSE,
        ruleResult = !biDiagnoser.all { diagnose ->
            if (diagnose.isICPC2()) {
                Diagnosekoder.icpc2.containsKey(diagnose.kode)
            } else {
                Diagnosekoder.icd10.containsKey(diagnose.kode)
            }
        },
    )
}

val ugyldingOrgNummerLengde: ValidationRule = { _, ruleMetadata ->
    val legekontorOrgnr = ruleMetadata.legekontorOrgnr

    val ugyldingOrgNummerLengde = legekontorOrgnr != null && legekontorOrgnr.length != 9

    RuleResult(
        ruleInputs = mapOf("ugyldingOrgNummerLengde" to ugyldingOrgNummerLengde),
        rule = ValidationRules.UGYLDIG_ORGNR_LENGDE,
        ruleResult = ugyldingOrgNummerLengde,
    )
}
