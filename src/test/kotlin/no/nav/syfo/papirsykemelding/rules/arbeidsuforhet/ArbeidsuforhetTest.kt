package no.nav.syfo.papirsykemelding.rules.arbeidsuforhet

import io.kotest.core.spec.style.FunSpec
import java.time.LocalDate
import no.nav.helse.diagnosekoder.Diagnosekoder
import no.nav.syfo.client.norskhelsenett.Behandler
import no.nav.syfo.generateSykemelding
import no.nav.syfo.model.AnnenFraverGrunn
import no.nav.syfo.model.AnnenFraversArsak
import no.nav.syfo.model.Diagnose
import no.nav.syfo.model.Status
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.papirsykemelding.rules.validation.generatePersonNumber
import no.nav.syfo.papirsykemelding.service.BehandlerOgStartdato
import no.nav.syfo.papirsykemelding.service.RuleMetadataSykmelding
import no.nav.syfo.papirsykemelding.service.SykmeldingMetadataInfo
import no.nav.syfo.toDiagnose
import org.amshove.kluent.shouldBeEqualTo

class ArbeidsuforhetTest :
    FunSpec({
        val ruleTree = ArbeidsuforhetRulesExecution()

        test("Ukjent diagnoseKodeType, Status MANUAL_PROSESSING") {
            val person31Years = LocalDate.now().minusYears(31)

            val sykmelding =
                generateSykemelding(
                    diagnose =
                        Diagnose(
                            system = "2.16.578.1.12.4.1.1.9999",
                            kode = "A09",
                            tekst = "Brudd legg/ankel",
                        ),
                )

            val ruleMetadata =
                RuleMetadata(
                    signatureDate = LocalDate.now().atStartOfDay(),
                    receivedDate = LocalDate.now().atStartOfDay(),
                    behandletTidspunkt = LocalDate.now().atStartOfDay(),
                    patientPersonNumber = generatePersonNumber(person31Years, false),
                    rulesetVersion = null,
                    legekontorOrgnr = null,
                    tssid = null,
                    pasientFodselsdato = person31Years,
                )

            val ruleMetadataSykmelding =
                RuleMetadataSykmelding(
                    ruleMetadata = ruleMetadata,
                    sykmeldingMetadataInfo = SykmeldingMetadataInfo(null, emptyList()),
                    doctorSuspensjon = false,
                    behandlerOgStartdato = BehandlerOgStartdato(Behandler(emptyList(), null), null)
                )

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding)

            status.first.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
            status.first.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
                listOf(
                    ArbeidsuforhetRules.UKJENT_DIAGNOSEKODETYPE to true,
                )

            mapOf(
                "ukjentDiagnoseKodeType" to true,
            ) shouldBeEqualTo status.first.ruleInputs

            status.first.treeResult.ruleHit shouldBeEqualTo
                ArbeidsuforhetRuleHit.UKJENT_DIAGNOSEKODETYPE.ruleHit
        }

        test("Diagnosen er icpz 2 z diagnose, Status MANUAL_PROSESSING") {
            val person31Years = LocalDate.now().minusYears(31)

            val sykmelding =
                generateSykemelding(diagnose = Diagnosekoder.icpc2["Z09"]!!.toDiagnose())

            val ruleMetadata =
                RuleMetadata(
                    signatureDate = LocalDate.now().atStartOfDay(),
                    receivedDate = LocalDate.now().atStartOfDay(),
                    behandletTidspunkt = LocalDate.now().atStartOfDay(),
                    patientPersonNumber = generatePersonNumber(person31Years, false),
                    rulesetVersion = null,
                    legekontorOrgnr = null,
                    tssid = null,
                    pasientFodselsdato = person31Years,
                )

            val ruleMetadataSykmelding =
                RuleMetadataSykmelding(
                    ruleMetadata = ruleMetadata,
                    sykmeldingMetadataInfo = SykmeldingMetadataInfo(null, emptyList()),
                    doctorSuspensjon = false,
                    behandlerOgStartdato = BehandlerOgStartdato(Behandler(emptyList(), null), null)
                )

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding)

            status.first.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
            status.first.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
                listOf(
                    ArbeidsuforhetRules.UKJENT_DIAGNOSEKODETYPE to false,
                    ArbeidsuforhetRules.ICPC_2_Z_DIAGNOSE to true,
                )

            mapOf(
                "ukjentDiagnoseKodeType" to false,
                "icpc2ZDiagnose" to true,
            ) shouldBeEqualTo status.first.ruleInputs

            status.first.treeResult.ruleHit shouldBeEqualTo
                ArbeidsuforhetRuleHit.ICPC_2_Z_DIAGNOSE.ruleHit
        }

        test("HouvedDiagnose eller fraversgrunn mangler, Status MANUAL_PROSESSING") {
            val person31Years = LocalDate.now().minusYears(31)

            val sykmelding = generateSykemelding(diagnose = null)

            val ruleMetadata =
                RuleMetadata(
                    signatureDate = LocalDate.now().atStartOfDay(),
                    receivedDate = LocalDate.now().atStartOfDay(),
                    behandletTidspunkt = LocalDate.now().atStartOfDay(),
                    patientPersonNumber = generatePersonNumber(person31Years, false),
                    rulesetVersion = null,
                    legekontorOrgnr = null,
                    tssid = null,
                    pasientFodselsdato = person31Years,
                )

            val ruleMetadataSykmelding =
                RuleMetadataSykmelding(
                    ruleMetadata = ruleMetadata,
                    sykmeldingMetadataInfo = SykmeldingMetadataInfo(null, emptyList()),
                    doctorSuspensjon = false,
                    behandlerOgStartdato = BehandlerOgStartdato(Behandler(emptyList(), null), null)
                )

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding)

            status.first.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
            status.first.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
                listOf(
                    ArbeidsuforhetRules.UKJENT_DIAGNOSEKODETYPE to false,
                    ArbeidsuforhetRules.ICPC_2_Z_DIAGNOSE to false,
                    ArbeidsuforhetRules.HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER to true,
                )

            mapOf(
                "ukjentDiagnoseKodeType" to false,
                "icpc2ZDiagnose" to false,
                "houvedDiagnoseEllerFraversgrunnMangler" to true,
            ) shouldBeEqualTo status.first.ruleInputs

            status.first.treeResult.ruleHit shouldBeEqualTo
                ArbeidsuforhetRuleHit.HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER.ruleHit
        }

        test("Ugyldig KodeVerk for houvedDiagnose, Status MANUAL_PROSESSING") {
            val person31Years = LocalDate.now().minusYears(31)

            val sykmelding =
                generateSykemelding(
                    diagnose =
                        Diagnose(
                            system = "2.16.578.1.12.4.1.1.7110",
                            kode = "Z09",
                            tekst = "Brudd legg/ankel",
                        ),
                )

            val ruleMetadata =
                RuleMetadata(
                    signatureDate = LocalDate.now().atStartOfDay(),
                    receivedDate = LocalDate.now().atStartOfDay(),
                    behandletTidspunkt = LocalDate.now().atStartOfDay(),
                    patientPersonNumber = generatePersonNumber(person31Years, false),
                    rulesetVersion = null,
                    legekontorOrgnr = null,
                    tssid = null,
                    pasientFodselsdato = person31Years,
                )

            val ruleMetadataSykmelding =
                RuleMetadataSykmelding(
                    ruleMetadata = ruleMetadata,
                    sykmeldingMetadataInfo = SykmeldingMetadataInfo(null, emptyList()),
                    doctorSuspensjon = false,
                    behandlerOgStartdato = BehandlerOgStartdato(Behandler(emptyList(), null), null)
                )

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding)

            status.first.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
            status.first.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
                listOf(
                    ArbeidsuforhetRules.UKJENT_DIAGNOSEKODETYPE to false,
                    ArbeidsuforhetRules.ICPC_2_Z_DIAGNOSE to false,
                    ArbeidsuforhetRules.HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER to false,
                    ArbeidsuforhetRules.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE to true,
                )

            mapOf(
                "ukjentDiagnoseKodeType" to false,
                "icpc2ZDiagnose" to false,
                "houvedDiagnoseEllerFraversgrunnMangler" to false,
                "ugyldigKodeVerkHouvedDiagnose" to true,
            ) shouldBeEqualTo status.first.ruleInputs

            status.first.treeResult.ruleHit shouldBeEqualTo
                ArbeidsuforhetRuleHit.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE.ruleHit
        }

        test(
            "Ugyldig kodeverk for hovediagnose, og mangler annenFraværArsak, skal få status MANUAL_PROSESSING"
        ) {
            val person31Years = LocalDate.now().minusYears(31)

            val sykmelding =
                generateSykemelding(
                    diagnose =
                        Diagnose(
                            system = Diagnosekoder.ICD10_CODE,
                            kode = "Aaoheaotneshao",
                            tekst = "Brudd legg/ankel",
                        ),
                    annenFravarArsak =
                        AnnenFraversArsak(
                            grunn = listOf(AnnenFraverGrunn.DONOR),
                            beskrivelse = null,
                        )
                )

            val ruleMetadata =
                RuleMetadata(
                    signatureDate = LocalDate.now().atStartOfDay(),
                    receivedDate = LocalDate.now().atStartOfDay(),
                    behandletTidspunkt = LocalDate.now().atStartOfDay(),
                    patientPersonNumber = generatePersonNumber(person31Years, false),
                    rulesetVersion = null,
                    legekontorOrgnr = null,
                    tssid = null,
                    pasientFodselsdato = person31Years,
                )

            val ruleMetadataSykmelding =
                RuleMetadataSykmelding(
                    ruleMetadata = ruleMetadata,
                    sykmeldingMetadataInfo = SykmeldingMetadataInfo(null, emptyList()),
                    doctorSuspensjon = false,
                    behandlerOgStartdato = BehandlerOgStartdato(Behandler(emptyList(), null), null)
                )

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding)

            status.first.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
            status.first.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
                listOf(
                    ArbeidsuforhetRules.UKJENT_DIAGNOSEKODETYPE to false,
                    ArbeidsuforhetRules.ICPC_2_Z_DIAGNOSE to false,
                    ArbeidsuforhetRules.HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER to false,
                    ArbeidsuforhetRules.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE to true,
                )

            mapOf(
                "ukjentDiagnoseKodeType" to false,
                "icpc2ZDiagnose" to false,
                "houvedDiagnoseEllerFraversgrunnMangler" to false,
                "ugyldigKodeVerkHouvedDiagnose" to true,
            ) shouldBeEqualTo status.first.ruleInputs

            status.first.treeResult.ruleHit shouldBeEqualTo
                ArbeidsuforhetRuleHit.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE.ruleHit
        }

        test("Ugyldig kodeVerk for biDiagnose, Status MANUAL_PROSESSING") {
            val person31Years = LocalDate.now().minusYears(31)

            val sykmelding =
                generateSykemelding(
                    biDiagnose =
                        listOf(
                            Diagnose(
                                system = "2.16.578.1.12.4.1.1.7110",
                                kode = "S09",
                                tekst = "Brudd legg/ankel",
                            ),
                        ),
                )

            val ruleMetadata =
                RuleMetadata(
                    signatureDate = LocalDate.now().atStartOfDay(),
                    receivedDate = LocalDate.now().atStartOfDay(),
                    behandletTidspunkt = LocalDate.now().atStartOfDay(),
                    patientPersonNumber = generatePersonNumber(person31Years, false),
                    rulesetVersion = null,
                    legekontorOrgnr = null,
                    tssid = null,
                    pasientFodselsdato = person31Years,
                )

            val ruleMetadataSykmelding =
                RuleMetadataSykmelding(
                    ruleMetadata = ruleMetadata,
                    sykmeldingMetadataInfo = SykmeldingMetadataInfo(null, emptyList()),
                    doctorSuspensjon = false,
                    behandlerOgStartdato = BehandlerOgStartdato(Behandler(emptyList(), null), null)
                )

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding)

            status.first.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
            status.first.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
                listOf(
                    ArbeidsuforhetRules.UKJENT_DIAGNOSEKODETYPE to false,
                    ArbeidsuforhetRules.ICPC_2_Z_DIAGNOSE to false,
                    ArbeidsuforhetRules.HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER to false,
                    ArbeidsuforhetRules.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE to false,
                    ArbeidsuforhetRules.UGYLDIG_KODEVERK_FOR_BIDIAGNOSE to true,
                )

            mapOf(
                "ukjentDiagnoseKodeType" to false,
                "icpc2ZDiagnose" to false,
                "houvedDiagnoseEllerFraversgrunnMangler" to false,
                "ugyldigKodeVerkHouvedDiagnose" to false,
                "ugyldigKodeVerkBiDiagnose" to true,
            ) shouldBeEqualTo status.first.ruleInputs

            status.first.treeResult.ruleHit shouldBeEqualTo
                ArbeidsuforhetRuleHit.UGYLDIG_KODEVERK_FOR_BIDIAGNOSE.ruleHit
        }
    })
