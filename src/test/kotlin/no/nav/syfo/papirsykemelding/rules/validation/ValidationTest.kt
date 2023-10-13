package no.nav.syfo.papirsykemelding.rules.validation

import io.kotest.core.spec.style.FunSpec
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import no.nav.syfo.client.norskhelsenett.Behandler
import no.nav.syfo.generateSykemelding
import no.nav.syfo.model.Diagnose
import no.nav.syfo.model.Status
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.papirsykemelding.service.BehandlerOgStartdato
import no.nav.syfo.papirsykemelding.service.RuleMetadataSykmelding
import no.nav.syfo.papirsykemelding.service.SykmeldingMetadataInfo
import no.nav.syfo.validering.validatePersonAndDNumber
import org.amshove.kluent.shouldBeEqualTo

val personNumberDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("ddMMyy")

class ValidationTest :
    FunSpec({
        val ruleTree = ValidationRulesExecution()

        context("Testing validation rules and checking the rule outcomes") {
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
                    listOf(
                        ValidationRules.UGYLDIG_ORGNR_LENGDE to false,
                    )

                mapOf(
                    "legekontorOrgnummer" to "",
                    "ugyldingOrgNummerLengde" to false,
                ) shouldBeEqualTo status.ruleInputs

                status.treeResult.ruleHit shouldBeEqualTo null
            }

            test("ugyldig orgnummer legende, Status MANUAL_PROCESSING") {
                val person31Years = LocalDate.now().minusYears(31)

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
                        patientPersonNumber = generatePersonNumber(person31Years, false),
                        rulesetVersion = "2",
                        legekontorOrgnr = "1232344231",
                        tssid = null,
                        pasientFodselsdato = person31Years,
                    )

                val status =
                    ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata)).first

                status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
                status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
                    listOf(
                        ValidationRules.UGYLDIG_ORGNR_LENGDE to true,
                    )

                mapOf(
                    "legekontorOrgnummer" to ruleMetadata.legekontorOrgnr,
                    "ugyldingOrgNummerLengde" to true,
                ) shouldBeEqualTo status.ruleInputs

                status.treeResult.ruleHit shouldBeEqualTo
                    ValidationRuleHit.UGYLDIG_ORGNR_LENGDE.ruleHit
            }
        }
    })

fun ruleMetadataSykmelding(ruleMetadata: RuleMetadata) =
    RuleMetadataSykmelding(
        ruleMetadata = ruleMetadata,
        doctorSuspensjon = false,
        behandlerOgStartdato = BehandlerOgStartdato(Behandler(emptyList(), null), null),
        sykmeldingMetadataInfo = SykmeldingMetadataInfo(null, emptyList()),
    )

fun generatePersonNumber(bornDate: LocalDate, useDNumber: Boolean = false): String {
    val personDate =
        bornDate.format(personNumberDateFormat).let {
            if (useDNumber) "${it[0] + 4}${it.substring(1)}" else it
        }
    return (if (bornDate.year >= 2000) (75011..99999) else (11111..50099))
        .map { "$personDate$it" }
        .first { validatePersonAndDNumber(it) }
}
