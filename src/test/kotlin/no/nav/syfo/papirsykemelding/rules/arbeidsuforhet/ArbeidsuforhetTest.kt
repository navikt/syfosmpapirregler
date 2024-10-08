package no.nav.syfo.papirsykemelding.rules.arbeidsuforhet

import io.kotest.core.spec.style.FunSpec
import java.time.LocalDate
import no.nav.helse.diagnosekoder.Diagnosekoder
import no.nav.syfo.client.norskhelsenett.Behandler
import no.nav.syfo.generateMedisinskVurdering
import no.nav.syfo.generateSykemelding
import no.nav.syfo.generateSykmelding
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

        test("Ugyldig diagnoseKodeType, Status MANUAL_PROSESSING") {
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
                    ArbeidsuforhetRules.HOVEDDIAGNOSE_MANGLER to false,
                    ArbeidsuforhetRules.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE to true,
                )

            mapOf(
                "hovedDiagnose" to sykmelding.medisinskVurdering.hovedDiagnose,
                "ugyldigKodeverkHovedDiagnose" to true,
            ) shouldBeEqualTo status.first.ruleInputs

            status.first.treeResult.ruleHit shouldBeEqualTo
                ArbeidsuforhetRuleHit.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE.ruleHit
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
                    ArbeidsuforhetRules.HOVEDDIAGNOSE_MANGLER to false,
                    ArbeidsuforhetRules.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE to false,
                    ArbeidsuforhetRules.ICPC_2_Z_DIAGNOSE to true,
                )

            mapOf(
                "hovedDiagnose" to sykmelding.medisinskVurdering.hovedDiagnose,
                "ugyldigKodeverkHovedDiagnose" to false,
                "icpc2ZDiagnose" to true,
            ) shouldBeEqualTo status.first.ruleInputs

            status.first.treeResult.ruleHit shouldBeEqualTo
                ArbeidsuforhetRuleHit.ICPC_2_Z_DIAGNOSE.ruleHit
        }

        test("HovedDiagnose og fraversgrunn mangler, Status MANUAL_PROSESSING") {
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
                    ArbeidsuforhetRules.HOVEDDIAGNOSE_MANGLER to true,
                    ArbeidsuforhetRules.FRAVAERSGRUNN_MANGLER to true,
                )

            mapOf(
                "hovedDiagnose" to EmptyObject,
                "annenFraversArsak" to EmptyObject,
            ) shouldBeEqualTo status.first.ruleInputs

            status.first.treeResult.ruleHit shouldBeEqualTo
                ArbeidsuforhetRuleHit.FRAVAERSGRUNN_MANGLER.ruleHit
        }

        test("Ugyldig KodeVerk for hovedDiagnose, Status MANUAL_PROSESSING") {
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
                    ArbeidsuforhetRules.HOVEDDIAGNOSE_MANGLER to false,
                    ArbeidsuforhetRules.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE to true
                )

            mapOf(
                "hovedDiagnose" to sykmelding.medisinskVurdering.hovedDiagnose,
                "ugyldigKodeverkHovedDiagnose" to true,
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
                    ArbeidsuforhetRules.HOVEDDIAGNOSE_MANGLER to false,
                    ArbeidsuforhetRules.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE to false,
                    ArbeidsuforhetRules.ICPC_2_Z_DIAGNOSE to false,
                    ArbeidsuforhetRules.UGYLDIG_KODEVERK_FOR_BIDIAGNOSE to true,
                )

            mapOf(
                "hovedDiagnose" to sykmelding.medisinskVurdering.hovedDiagnose,
                "ugyldigKodeverkHovedDiagnose" to false,
                "icpc2ZDiagnose" to false,
                "ugyldigKodeVerkBiDiagnose" to sykmelding.medisinskVurdering.biDiagnoser,
            ) shouldBeEqualTo status.first.ruleInputs

            status.first.treeResult.ruleHit shouldBeEqualTo
                ArbeidsuforhetRuleHit.UGYLDIG_KODEVERK_FOR_BIDIAGNOSE.ruleHit
        }

        context("Test diagnoser") {
            val ruleTree = ArbeidsuforhetRulesExecution()
            val person31Years = LocalDate.now().minusYears(31)
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
                    sykmeldingMetadataInfo =
                    SykmeldingMetadataInfo(null, emptyList()),
                    doctorSuspensjon = false,
                    behandlerOgStartdato =
                    BehandlerOgStartdato(Behandler(emptyList(), null), null),
                )

            test("All OK") {
                val sykmelding =
                    generateSykmelding(
                        medisinskVurdering =
                        generateMedisinskVurdering(
                            diagnose =
                            Diagnose(
                                system = "2.16.578.1.12.4.1.1.7170",
                                kode = "R24",
                                tekst = "Blodig oppspytt/hemoptyse",
                            ),
                            annenFraversArsak = null,
                            biDiagnose =
                            listOf(
                                Diagnose(
                                    system = "2.16.578.1.12.4.1.1.7170",
                                    kode = "R24",
                                    tekst = "Blodig oppspytt/hemoptyse",
                                )
                            )
                        ),
                    )

                val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding)

                status.first.treeResult.status shouldBeEqualTo Status.OK
                status.first.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
                    listOf(
                        ArbeidsuforhetRules.HOVEDDIAGNOSE_MANGLER to false,
                        ArbeidsuforhetRules.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE to false,
                        ArbeidsuforhetRules.ICPC_2_Z_DIAGNOSE to false,
                        ArbeidsuforhetRules.UGYLDIG_KODEVERK_FOR_BIDIAGNOSE to false,
                    )

                mapOf(
                    "hovedDiagnose" to sykmelding.medisinskVurdering.hovedDiagnose,
                    "ugyldigKodeverkHovedDiagnose" to false,
                    "icpc2ZDiagnose" to false,
                    "ugyldigKodeVerkBiDiagnose" to sykmelding.medisinskVurdering.biDiagnoser,
                ) shouldBeEqualTo status.first.ruleInputs

                status.first.treeResult.ruleHit shouldBeEqualTo null
            }
        }
    })
