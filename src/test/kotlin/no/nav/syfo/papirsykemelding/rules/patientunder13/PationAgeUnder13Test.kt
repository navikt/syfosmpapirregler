package no.nav.syfo.papirsykemelding.rules.patientunder13

import io.kotest.core.spec.style.FunSpec
import java.time.LocalDate
import no.nav.syfo.generateSykemelding
import no.nav.syfo.model.Diagnose
import no.nav.syfo.model.Status
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.papirsykemelding.rules.validation.generatePersonNumber
import no.nav.syfo.papirsykemelding.rules.validation.ruleMetadataSykmelding
import org.amshove.kluent.shouldBeEqualTo

class PationAgeUnder13Test :
    FunSpec({
        val ruleTree = PatientAgeUnder13RulesExecution()

        context("Test patient under") {
            test("Alt ok, Status OK") {
                val person14Years = LocalDate.now().minusYears(14)

                val sykmelding =
                    generateSykemelding(
                        diagnose =
                            Diagnose(
                                system = "2.16.578.1.12.4.1.1.7170",
                                kode = "R24",
                                tekst = "Blodig oppspytt/hemoptyse",
                            ),
                    )

                val ruleMetadata =
                    RuleMetadata(
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
                status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
                    listOf(PatientAgeUnder13Rules.PASIENT_YNGRE_ENN_13 to false)

                mapOf(
                    "pasientUnder13Aar" to false,
                ) shouldBeEqualTo status.ruleInputs

                status.treeResult.ruleHit shouldBeEqualTo null
            }

            test("Pasient under 13 Aar, Status INVALID") {
                val person12Years = LocalDate.now().minusYears(12)

                val sykmelding = generateSykemelding()

                val ruleMetadata =
                    RuleMetadata(
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
                status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
                    listOf(
                        PatientAgeUnder13Rules.PASIENT_YNGRE_ENN_13 to true,
                    )

                mapOf(
                    "pasientUnder13Aar" to true,
                ) shouldBeEqualTo status.ruleInputs

                status.treeResult.ruleHit shouldBeEqualTo
                    PatientAgeUnder13RuleHit.PASIENT_YNGRE_ENN_13.ruleHit
            }
        }
    })
