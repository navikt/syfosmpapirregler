package no.nav.syfo.papirsykemelding.rules.legesuspensjon

import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.util.UUID
import no.nav.syfo.model.Status
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.ruleMetadataSykmelding
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LegeSuspensjonTest {
    private val ruleTree = LegeSuspensjonRulesExecution()
    private val ruleMetadata =
        RuleMetadata(
            signatureDate = LocalDate.now().atStartOfDay(),
            receivedDate = LocalDate.now().atStartOfDay(),
            behandletTidspunkt = LocalDate.now().atStartOfDay(),
            patientPersonNumber = "12345678901",
            rulesetVersion = null,
            legekontorOrgnr = null,
            tssid = null,
            pasientFodselsdato = LocalDate.now().minusYears(31),
        )
    private val sykmeldingRuleMetadata = ruleMetadataSykmelding(ruleMetadata)
    private val sykmelding = mockk<Sykmelding>(relaxed = true)

    @BeforeEach
    fun setup() {
        every { sykmelding.id } returns UUID.randomUUID().toString()
    }

    @Test
    internal fun `Testing legesuspensjon rules and checking the rule outcomes Er ikkje suspendert, Status OK`() {

        val status = ruleTree.runRules(sykmelding, sykmeldingRuleMetadata).first
        status.treeResult.status shouldBeEqualTo Status.OK
        status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
            listOf(
                LegeSuspensjonRules.BEHANDLER_SUSPENDERT to false,
            )

        mapOf(
            "suspendert" to false,
        ) shouldBeEqualTo status.ruleInputs

        status.treeResult.ruleHit shouldBeEqualTo null
    }

    @Test
    internal fun `Testing legesuspensjon rules and checking the rule outcomes Er suspendert, Status MANUAL_PROCESSING`() {
        val status =
            ruleTree
                .runRules(sykmelding, sykmeldingRuleMetadata.copy(doctorSuspensjon = true))
                .first

        status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
        status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
            listOf(
                LegeSuspensjonRules.BEHANDLER_SUSPENDERT to true,
            )
        mapOf(
            "suspendert" to true,
        ) shouldBeEqualTo status.ruleInputs

        status.treeResult.ruleHit shouldBeEqualTo LegeSuspensjonRuleHit.BEHANDLER_SUSPENDERT.ruleHit
    }
}
