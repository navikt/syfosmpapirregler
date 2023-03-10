package no.nav.syfo.papirsykemelding.rules.validation

import io.kotest.core.spec.style.FunSpec
import no.nav.syfo.generateBehandler
import no.nav.syfo.generateMedisinskVurdering
import no.nav.syfo.model.Diagnose
import no.nav.syfo.model.Status
import org.amshove.kluent.shouldBeEqualTo
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import no.nav.syfo.client.norskhelsenett.Behandler
import no.nav.syfo.generateSykemelding
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.papirsykemelding.service.BehandlerOgStartdato
import no.nav.syfo.papirsykemelding.service.RuleMetadataSykmelding
import no.nav.syfo.validering.validatePersonAndDNumber

val personNumberDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("ddMMyy")

class ValidationTest : FunSpec({
 //TODO fix me
    val ruleTree = ValidationRulesExecution()

    context("Testing validation rules and checking the rule outcomes") {
        test("Alt ok, Status OK") {
            val person14Years = LocalDate.now().minusYears(14)

            val sykmelding = generateSykemelding(
                medisinskVurdering = generateMedisinskVurdering(
                    hovedDiagnose = Diagnose(
                        system = "2.16.578.1.12.4.1.1.7170",
                        kode = "R24",
                        tekst = "Blodig oppspytt/hemoptyse"
                    )
                )
            )

            val ruleMetadata = RuleMetadata(
                signatureDate = LocalDate.now().atStartOfDay(),
                receivedDate = LocalDate.now().atStartOfDay(),
                behandletTidspunkt = LocalDate.now().atStartOfDay(),
                patientPersonNumber = generatePersonNumber(person14Years, false),
                rulesetVersion = null,
                legekontorOrgnr = null,
                tssid = null,
                pasientFodselsdato = person14Years
            )

            val ruleMetadataSykmelding = ruleMetadataSykmelding(ruleMetadata)

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding).first

            status.treeResult.status shouldBeEqualTo Status.OK
            status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo listOf(
                ValidationRules.PASIENT_YNGRE_ENN_13 to false,
                ValidationRules.UGYLDIG_REGELSETTVERSJON to false,
                ValidationRules.UGYLDIG_ORGNR_LENGDE to false,
                ValidationRules.BEHANDLER_FNR_ER_SAMME_SOM_PASIENT_FNR to false
            )

            mapOf(
                "pasientUnder13Aar" to false,
                "ugyldigRegelsettversjon" to false,
                "manglendeDynamiskesporsmaalversjon2uke39" to false,
                "ugyldingOrgNummerLengde" to false,
                "avsenderSammeSomPasient" to false,
                "behandlerSammeSomPasient" to false,

            ) shouldBeEqualTo status.ruleInputs

            status.treeResult.ruleHit shouldBeEqualTo null
        }
        test("Pasient under 13 Aar, Status INVALID") {
            val person12Years = LocalDate.now().minusYears(12)

            val sykmelding = generateSykemelding()

            val ruleMetadata = RuleMetadata(
                signatureDate = LocalDate.now().atStartOfDay(),
                receivedDate = LocalDate.now().atStartOfDay(),
                behandletTidspunkt = LocalDate.now().atStartOfDay(),
                patientPersonNumber = generatePersonNumber(person12Years, false),
                rulesetVersion = null,
                legekontorOrgnr = null,
                tssid = null,
                pasientFodselsdato = person12Years
            )
            val ruleMetadataSykmelding = ruleMetadataSykmelding(ruleMetadata)

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding).first

            status.treeResult.status shouldBeEqualTo Status.INVALID
            status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo listOf(
                ValidationRules.PASIENT_YNGRE_ENN_13 to true
            )

            mapOf(
                "pasientUnder13Aar" to true,

            ) shouldBeEqualTo status.ruleInputs

            status.treeResult.ruleHit shouldBeEqualTo ValidationRuleHit.PASIENT_YNGRE_ENN_13.ruleHit
        }

        test("Ugyldig regelsettversjon, Status INVALID") {
            val person31Years = LocalDate.now().minusYears(31)

            val sykmelding = generateSykemelding(
                medisinskVurdering = generateMedisinskVurdering(
                    hovedDiagnose = Diagnose(
                        system = "2.16.578.1.12.4.1.1.7170",
                        kode = "R24",
                        tekst = "Blodig oppspytt/hemoptyse"
                    )
                )
            )

            val ruleMetadata = RuleMetadata(
                signatureDate = LocalDate.now().atStartOfDay(),
                receivedDate = LocalDate.now().atStartOfDay(),
                behandletTidspunkt = LocalDate.now().atStartOfDay(),
                patientPersonNumber = generatePersonNumber(person31Years, false),
                rulesetVersion = "9999",
                legekontorOrgnr = null,
                tssid = null,
                pasientFodselsdato = person31Years
            )
            val ruleMetadataSykmelding = ruleMetadataSykmelding(ruleMetadata)

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding).first

            status.treeResult.status shouldBeEqualTo Status.INVALID
            status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo listOf(
                ValidationRules.PASIENT_YNGRE_ENN_13 to false,
                ValidationRules.UGYLDIG_REGELSETTVERSJON to true
            )

            mapOf(
                "pasientUnder13Aar" to false,
                "ugyldigRegelsettversjon" to true
            ) shouldBeEqualTo status.ruleInputs

            status.treeResult.ruleHit shouldBeEqualTo ValidationRuleHit.UGYLDIG_REGELSETTVERSJON.ruleHit
        }

        test("Ugyldig orgnummer lengede, Status INVALID") {
            val person31Years = LocalDate.now().minusYears(31)

            val sykmelding = generateSykemelding(
                medisinskVurdering = generateMedisinskVurdering(
                    hovedDiagnose = Diagnose(
                        system = "2.16.578.1.12.4.1.1.7170",
                        kode = "R24",
                        tekst = "Blodig oppspytt/hemoptyse"
                    )
                )
            )

            val ruleMetadata = RuleMetadata(
                signatureDate = LocalDate.now().atStartOfDay(),
                receivedDate = LocalDate.now().atStartOfDay(),
                behandletTidspunkt = LocalDate.now().atStartOfDay(),
                patientPersonNumber = generatePersonNumber(person31Years, false),
                rulesetVersion = "2",
                legekontorOrgnr = "1232344",
                tssid = null,
                pasientFodselsdato = person31Years
            )

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata)).first

            status.treeResult.status shouldBeEqualTo Status.INVALID
            status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo listOf(
                ValidationRules.PASIENT_YNGRE_ENN_13 to false,
                ValidationRules.UGYLDIG_REGELSETTVERSJON to false,
                ValidationRules.UGYLDIG_ORGNR_LENGDE to true
            )

            mapOf(
                "pasientUnder13Aar" to false,
                "ugyldigRegelsettversjon" to false,
                "manglendeDynamiskesporsmaalversjon2uke39" to false,
                "ugyldingOrgNummerLengde" to true
            ) shouldBeEqualTo status.ruleInputs

            status.treeResult.ruleHit shouldBeEqualTo ValidationRuleHit.UGYLDIG_ORGNR_LENGDE.ruleHit
        }

        test("Behandler samme som pasient, Status INVALID") {
            val person31Years = LocalDate.now().minusYears(31)

            val patientPersonNumber = generatePersonNumber(person31Years, false)

            val sykmelding = generateSykemelding(
                behandler = generateBehandler(
                    "Per", "", "Hansen", "134", "113", patientPersonNumber
                ),
                medisinskVurdering = generateMedisinskVurdering(
                    hovedDiagnose = Diagnose(
                        system = "2.16.578.1.12.4.1.1.7170",
                        kode = "R24",
                        tekst = "Blodig oppspytt/hemoptyse"
                    )
                )
            )

            val ruleMetadata = RuleMetadata(
                signatureDate = LocalDate.now().atStartOfDay(),
                receivedDate = LocalDate.now().atStartOfDay(),
                behandletTidspunkt = LocalDate.now().atStartOfDay(),
                patientPersonNumber = patientPersonNumber,
                rulesetVersion = null,
                legekontorOrgnr = null,
                tssid = null,
                pasientFodselsdato = person31Years
            )

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata)).first

            status.treeResult.status shouldBeEqualTo Status.INVALID
            status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo listOf(
                ValidationRules.PASIENT_YNGRE_ENN_13 to false,
                ValidationRules.UGYLDIG_REGELSETTVERSJON to false,
                ValidationRules.UGYLDIG_ORGNR_LENGDE to false,
                ValidationRules.BEHANDLER_FNR_ER_SAMME_SOM_PASIENT_FNR to true
            )

            mapOf(
                "pasientUnder13Aar" to false,
                "ugyldigRegelsettversjon" to false,
                "manglendeDynamiskesporsmaalversjon2uke39" to false,
                "ugyldingOrgNummerLengde" to false,
                "avsenderSammeSomPasient" to false,
                "behandlerSammeSomPasient" to true
            ) shouldBeEqualTo status.ruleInputs

            status.treeResult.ruleHit shouldBeEqualTo ValidationRuleHit.BEHANDLER_FNR_ER_SAMME_SOM_PASIENT_FNR.ruleHit
        }
    }
})

fun ruleMetadataSykmelding(ruleMetadata: RuleMetadata) = RuleMetadataSykmelding(
    ruleMetadata = ruleMetadata,
    erNyttSyketilfelle = false,
    doctorSuspensjon = false,
    behandlerOgStartdato = BehandlerOgStartdato(Behandler(emptyList(), null), null)
)

fun generatePersonNumber(bornDate: LocalDate, useDNumber: Boolean = false): String {
    val personDate = bornDate.format(personNumberDateFormat).let {
        if (useDNumber) "${it[0] + 4}${it.substring(1)}" else it
    }
    return (if (bornDate.year >= 2000) (75011..99999) else (11111..50099))
        .map { "$personDate$it" }
        .first {
            validatePersonAndDNumber(it)
        }
}
