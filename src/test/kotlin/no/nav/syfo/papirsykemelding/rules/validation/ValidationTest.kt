package no.nav.syfo.papirsykemelding.rules.validation

import io.kotest.core.spec.style.FunSpec
import no.nav.syfo.client.norskhelsenett.Behandler
import no.nav.syfo.generateSykemelding
import no.nav.syfo.model.Diagnose
import no.nav.syfo.model.Status
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.papirsykemelding.service.BehandlerOgStartdato
import no.nav.syfo.papirsykemelding.service.RuleMetadataSykmelding
import no.nav.syfo.validering.validatePersonAndDNumber
import org.amshove.kluent.shouldBeEqualTo
import java.time.LocalDate
import java.time.format.DateTimeFormatter

val personNumberDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("ddMMyy")

class ValidationTest : FunSpec({
    val ruleTree = ValidationRulesExecution()

    context("Testing validation rules and checking the rule outcomes") {
        test("Alt ok, Status OK") {
            val person14Years = LocalDate.now().minusYears(14)

            val sykmelding = generateSykemelding(
                diagnose = Diagnose(
                    system = "2.16.578.1.12.4.1.1.7170",
                    kode = "R24",
                    tekst = "Blodig oppspytt/hemoptyse",
                ),
            )

            val ruleMetadata = RuleMetadata(
                signatureDate = LocalDate.now().atStartOfDay(),
                receivedDate = LocalDate.now().atStartOfDay(),
                behandletTidspunkt = LocalDate.now().atStartOfDay(),
                patientPersonNumber = generatePersonNumber(person14Years, false),
                rulesetVersion = null,
                legekontorOrgnr = null,
                tssid = null,
                pasientFodselsdato = person14Years,
            )

            val ruleMetadataSykmelding = ruleMetadataSykmelding(ruleMetadata)

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding).first

            status.treeResult.status shouldBeEqualTo Status.OK
            status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo listOf(
                ValidationRules.PASIENT_YNGRE_ENN_13 to false,
                ValidationRules.PASIENT_ELDRE_ENN_70 to false,
                ValidationRules.UKJENT_DIAGNOSEKODETYPE to false,
                ValidationRules.ICPC_2_Z_DIAGNOSE to false,
                ValidationRules.HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER to false,
                ValidationRules.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE to false,
                ValidationRules.UGYLDIG_KODEVERK_FOR_BIDIAGNOSE to false,
                ValidationRules.UGYLDIG_ORGNR_LENGDE to false,
            )

            mapOf(
                "pasientUnder13Aar" to false,
                "pasientOver70Aar" to false,
                "hoveddiagnose" to sykmelding.medisinskVurdering.hovedDiagnose,
                "annenFraversArsak" to "",
                "biDiagnoser" to emptyList<Diagnose>(),
                "ugyldingOrgNummerLengde" to false,
            ) shouldBeEqualTo status.ruleInputs

            status.treeResult.ruleHit shouldBeEqualTo null
        }
        test("Pasient under 13 Aar, Status MANUAL_PROCESSING") {
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
                pasientFodselsdato = person12Years,
            )
            val ruleMetadataSykmelding = ruleMetadataSykmelding(ruleMetadata)

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding).first

            status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
            status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo listOf(
                ValidationRules.PASIENT_YNGRE_ENN_13 to true,
            )

            mapOf(
                "pasientUnder13Aar" to true,
            ) shouldBeEqualTo status.ruleInputs

            status.treeResult.ruleHit shouldBeEqualTo ValidationRuleHit.PASIENT_YNGRE_ENN_13.ruleHit
        }

        test("pasient eldre enn 70 aar, Status MANUAL_PROCESSING") {
            val person71Years = LocalDate.now().minusYears(71)

            val sykmelding = generateSykemelding(
                diagnose = Diagnose(
                    system = "2.16.578.1.12.4.1.1.7170",
                    kode = "R24",
                    tekst = "Blodig oppspytt/hemoptyse",
                ),
            )

            val ruleMetadata = RuleMetadata(
                signatureDate = LocalDate.now().atStartOfDay(),
                receivedDate = LocalDate.now().atStartOfDay(),
                behandletTidspunkt = LocalDate.now().atStartOfDay(),
                patientPersonNumber = generatePersonNumber(person71Years, false),
                rulesetVersion = "2",
                legekontorOrgnr = "1232344",
                tssid = null,
                pasientFodselsdato = person71Years,
            )

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata)).first

            status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
            status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo listOf(
                ValidationRules.PASIENT_YNGRE_ENN_13 to false,
                ValidationRules.PASIENT_ELDRE_ENN_70 to true,
            )

            mapOf(
                "pasientUnder13Aar" to false,
                "pasientOver70Aar" to true,
            ) shouldBeEqualTo status.ruleInputs

            status.treeResult.ruleHit shouldBeEqualTo ValidationRuleHit.PASIENT_ELDRE_ENN_70.ruleHit
        }

        test("ukjent diagnose kode type, Status MANUAL_PROCESSING") {
            val person31Years = LocalDate.now().minusYears(31)

            val sykmelding = generateSykemelding(
                diagnose = Diagnose(
                    system = "2.16.578.1.12.4.1.1.9999",
                    kode = "A09",
                    tekst = "Svetteproblemer",
                ),
            )

            val ruleMetadata = RuleMetadata(
                signatureDate = LocalDate.now().atStartOfDay(),
                receivedDate = LocalDate.now().atStartOfDay(),
                behandletTidspunkt = LocalDate.now().atStartOfDay(),
                patientPersonNumber = generatePersonNumber(person31Years, false),
                rulesetVersion = "2",
                legekontorOrgnr = "1232344",
                tssid = null,
                pasientFodselsdato = person31Years,
            )

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata)).first

            status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
            status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo listOf(
                ValidationRules.PASIENT_YNGRE_ENN_13 to false,
                ValidationRules.PASIENT_ELDRE_ENN_70 to false,
                ValidationRules.UKJENT_DIAGNOSEKODETYPE to true,
            )

            mapOf(
                "pasientUnder13Aar" to false,
                "pasientOver70Aar" to false,
                "hoveddiagnose" to sykmelding.medisinskVurdering.hovedDiagnose,
            ) shouldBeEqualTo status.ruleInputs

            status.treeResult.ruleHit shouldBeEqualTo ValidationRuleHit.UKJENT_DIAGNOSEKODETYPE.ruleHit
        }

        test("icpc 2 z diagnose, Status MANUAL_PROCESSING") {
            val person31Years = LocalDate.now().minusYears(31)

            val sykmelding = generateSykemelding(
                diagnose = Diagnose(
                    system = "2.16.578.1.12.4.1.1.7170",
                    kode = "Z09",
                    tekst = "Problem jus/poli",
                ),
            )

            val ruleMetadata = RuleMetadata(
                signatureDate = LocalDate.now().atStartOfDay(),
                receivedDate = LocalDate.now().atStartOfDay(),
                behandletTidspunkt = LocalDate.now().atStartOfDay(),
                patientPersonNumber = generatePersonNumber(person31Years, false),
                rulesetVersion = "2",
                legekontorOrgnr = "1232344",
                tssid = null,
                pasientFodselsdato = person31Years,
            )

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata)).first

            status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
            status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo listOf(
                ValidationRules.PASIENT_YNGRE_ENN_13 to false,
                ValidationRules.PASIENT_ELDRE_ENN_70 to false,
                ValidationRules.UKJENT_DIAGNOSEKODETYPE to false,
                ValidationRules.ICPC_2_Z_DIAGNOSE to true,
            )

            mapOf(
                "pasientUnder13Aar" to false,
                "pasientOver70Aar" to false,
                "hoveddiagnose" to sykmelding.medisinskVurdering.hovedDiagnose,
            ) shouldBeEqualTo status.ruleInputs

            status.treeResult.ruleHit shouldBeEqualTo ValidationRuleHit.ICPC_2_Z_DIAGNOSE.ruleHit
        }

        test("houveddiagnse eller fraversgrunn mangler, Status MANUAL_PROCESSING") {
            val person31Years = LocalDate.now().minusYears(31)

            val sykmelding = generateSykemelding(
                diagnose = null,
            )

            val ruleMetadata = RuleMetadata(
                signatureDate = LocalDate.now().atStartOfDay(),
                receivedDate = LocalDate.now().atStartOfDay(),
                behandletTidspunkt = LocalDate.now().atStartOfDay(),
                patientPersonNumber = generatePersonNumber(person31Years, false),
                rulesetVersion = "2",
                legekontorOrgnr = "1232344",
                tssid = null,
                pasientFodselsdato = person31Years,
            )

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata)).first

            status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
            status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo listOf(
                ValidationRules.PASIENT_YNGRE_ENN_13 to false,
                ValidationRules.PASIENT_ELDRE_ENN_70 to false,
                ValidationRules.UKJENT_DIAGNOSEKODETYPE to false,
                ValidationRules.ICPC_2_Z_DIAGNOSE to false,
                ValidationRules.HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER to true,
            )

            mapOf(
                "pasientUnder13Aar" to false,
                "pasientOver70Aar" to false,
                "hoveddiagnose" to "",
                "annenFraversArsak" to "",
            ) shouldBeEqualTo status.ruleInputs

            status.treeResult.ruleHit shouldBeEqualTo ValidationRuleHit.HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER.ruleHit
        }

        test("ugyldigkodeverk for houveddiagnose, Status MANUAL_PROCESSING") {
            val person31Years = LocalDate.now().minusYears(31)

            val sykmelding = generateSykemelding(
                diagnose = Diagnose(
                    system = "2.16.578.1.12.4.1.1.7110",
                    kode = "R24",
                    tekst = "Blodig oppspytt/hemoptyse",
                ),
            )

            val ruleMetadata = RuleMetadata(
                signatureDate = LocalDate.now().atStartOfDay(),
                receivedDate = LocalDate.now().atStartOfDay(),
                behandletTidspunkt = LocalDate.now().atStartOfDay(),
                patientPersonNumber = generatePersonNumber(person31Years, false),
                rulesetVersion = "2",
                legekontorOrgnr = "1232344",
                tssid = null,
                pasientFodselsdato = person31Years,
            )

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata)).first

            status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
            status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo listOf(
                ValidationRules.PASIENT_YNGRE_ENN_13 to false,
                ValidationRules.PASIENT_ELDRE_ENN_70 to false,
                ValidationRules.UKJENT_DIAGNOSEKODETYPE to false,
                ValidationRules.ICPC_2_Z_DIAGNOSE to false,
                ValidationRules.HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER to false,
                ValidationRules.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE to true,
            )

            mapOf(
                "pasientUnder13Aar" to false,
                "pasientOver70Aar" to false,
                "hoveddiagnose" to sykmelding.medisinskVurdering.hovedDiagnose,
                "annenFraversArsak" to "",
            ) shouldBeEqualTo status.ruleInputs

            status.treeResult.ruleHit shouldBeEqualTo ValidationRuleHit.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE.ruleHit
        }

        test("ugyldigkodeverk for bidiagnose, Status MANUAL_PROCESSING") {
            val person31Years = LocalDate.now().minusYears(31)

            val sykmelding = generateSykemelding(
                diagnose = Diagnose(
                    system = "2.16.578.1.12.4.1.1.7170",
                    kode = "R24",
                    tekst = "Blodig oppspytt/hemoptyse",
                ),
                biDiagnose = listOf(
                    Diagnose(
                        system = "2.16.578.1.12.4.1.1.7110",
                        kode = "R24",
                        tekst = "Blodig oppspytt/hemoptyse",
                    ),
                ),
            )

            val ruleMetadata = RuleMetadata(
                signatureDate = LocalDate.now().atStartOfDay(),
                receivedDate = LocalDate.now().atStartOfDay(),
                behandletTidspunkt = LocalDate.now().atStartOfDay(),
                patientPersonNumber = generatePersonNumber(person31Years, false),
                rulesetVersion = "2",
                legekontorOrgnr = "1232344",
                tssid = null,
                pasientFodselsdato = person31Years,
            )

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata)).first

            status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
            status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo listOf(
                ValidationRules.PASIENT_YNGRE_ENN_13 to false,
                ValidationRules.PASIENT_ELDRE_ENN_70 to false,
                ValidationRules.UKJENT_DIAGNOSEKODETYPE to false,
                ValidationRules.ICPC_2_Z_DIAGNOSE to false,
                ValidationRules.HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER to false,
                ValidationRules.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE to false,
                ValidationRules.UGYLDIG_KODEVERK_FOR_BIDIAGNOSE to true,
            )

            mapOf(
                "pasientUnder13Aar" to false,
                "pasientOver70Aar" to false,
                "hoveddiagnose" to sykmelding.medisinskVurdering.hovedDiagnose,
                "annenFraversArsak" to "",
                "biDiagnoser" to sykmelding.medisinskVurdering.biDiagnoser,
            ) shouldBeEqualTo status.ruleInputs

            status.treeResult.ruleHit shouldBeEqualTo ValidationRuleHit.UGYLDIG_KODEVERK_FOR_BIDIAGNOSE.ruleHit
        }

        test("ugyldig orgnummer legende, Status MANUAL_PROCESSING") {
            val person31Years = LocalDate.now().minusYears(31)

            val sykmelding = generateSykemelding(
                diagnose = Diagnose(
                    system = "2.16.578.1.12.4.1.1.7170",
                    kode = "R24",
                    tekst = "Blodig oppspytt/hemoptyse",
                ),
            )

            val ruleMetadata = RuleMetadata(
                signatureDate = LocalDate.now().atStartOfDay(),
                receivedDate = LocalDate.now().atStartOfDay(),
                behandletTidspunkt = LocalDate.now().atStartOfDay(),
                patientPersonNumber = generatePersonNumber(person31Years, false),
                rulesetVersion = "2",
                legekontorOrgnr = "1232344231",
                tssid = null,
                pasientFodselsdato = person31Years,
            )

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata)).first

            status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
            status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo listOf(
                ValidationRules.PASIENT_YNGRE_ENN_13 to false,
                ValidationRules.PASIENT_ELDRE_ENN_70 to false,
                ValidationRules.UKJENT_DIAGNOSEKODETYPE to false,
                ValidationRules.ICPC_2_Z_DIAGNOSE to false,
                ValidationRules.HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER to false,
                ValidationRules.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE to false,
                ValidationRules.UGYLDIG_KODEVERK_FOR_BIDIAGNOSE to false,
                ValidationRules.UGYLDIG_ORGNR_LENGDE to true,
            )

            mapOf(
                "pasientUnder13Aar" to false,
                "pasientOver70Aar" to false,
                "hoveddiagnose" to sykmelding.medisinskVurdering.hovedDiagnose,
                "annenFraversArsak" to "",
                "biDiagnoser" to emptyList<Diagnose>(),
                "ugyldingOrgNummerLengde" to true,
            ) shouldBeEqualTo status.ruleInputs

            status.treeResult.ruleHit shouldBeEqualTo ValidationRuleHit.UGYLDIG_ORGNR_LENGDE.ruleHit
        }
    }
})

fun ruleMetadataSykmelding(ruleMetadata: RuleMetadata) = RuleMetadataSykmelding(
    ruleMetadata = ruleMetadata,
    erNyttSyketilfelle = false,
    doctorSuspensjon = false,
    behandlerOgStartdato = BehandlerOgStartdato(Behandler(emptyList(), null), null),
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
