package no.nav.syfo.papirsykemelding.rules.validation

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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

val personNumberDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("ddMMyy")

internal class ValidationTest {

    private val ruleTree = ValidationRulesExecution()

    @Test
    internal fun `Testing validation rules and checking the rule outcomes alt ok, Status OK`() {

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

        assertEquals(Status.OK, status.treeResult.status)
        assertEquals(
            listOf(
                ValidationRules.UGYLDIG_ORGNR_LENGDE to false,
            ),
            status.rulePath.map { it.rule to it.ruleResult },
        )
        assertEquals(
            mapOf(
                "legekontorOrgnummer" to "",
                "ugyldingOrgNummerLengde" to false,
            ),
            status.ruleInputs,
        )
        assertEquals(null, status.treeResult.ruleHit)
    }

    @Test
    internal fun `Testing validation rules and checking the rule outcomes ugyldig orgnummer legende, Status MANUAL_PROCESSING`() {

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

        val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata)).first

        assertEquals(Status.MANUAL_PROCESSING, status.treeResult.status)
        assertEquals(
            listOf(
                ValidationRules.UGYLDIG_ORGNR_LENGDE to true,
            ),
            status.rulePath.map { it.rule to it.ruleResult },
        )
        assertEquals(
            mapOf(
                "legekontorOrgnummer" to ruleMetadata.legekontorOrgnr,
                "ugyldingOrgNummerLengde" to true,
            ),
            status.ruleInputs,
        )
        assertEquals(ValidationRuleHit.UGYLDIG_ORGNR_LENGDE.ruleHit, status.treeResult.ruleHit)
    }
}

fun ruleMetadataSykmelding(ruleMetadata: RuleMetadata) =
    RuleMetadataSykmelding(
        ruleMetadata = ruleMetadata,
        doctorSuspensjon = false,
        behandlerOgStartdato = BehandlerOgStartdato(Behandler(emptyList(), null), null),
        sykmeldingMetadataInfo =
            SykmeldingMetadataInfo(
                ettersending = null,
                forlengelse = null,
                dagerForArbeidsgiverperiodeCheck = emptyList(),
                startDato = LocalDate.now()
            ),
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
