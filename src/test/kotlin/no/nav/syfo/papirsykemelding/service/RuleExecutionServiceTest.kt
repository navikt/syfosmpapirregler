package no.nav.syfo.papirsykemelding.service

import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import no.nav.syfo.client.norskhelsenett.Behandler
import no.nav.syfo.client.norskhelsenett.Godkjenning
import no.nav.syfo.client.norskhelsenett.Kode
import no.nav.syfo.generateSykemelding
import no.nav.syfo.model.Status
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.model.juridisk.JuridiskEnum
import no.nav.syfo.papirsykemelding.model.sortedFOMDate
import no.nav.syfo.papirsykemelding.rules.common.RuleExecution
import no.nav.syfo.papirsykemelding.rules.common.RuleHit
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.common.UtenJuridisk
import no.nav.syfo.papirsykemelding.rules.dsl.TreeOutput
import no.nav.syfo.papirsykemelding.rules.validation.ruleMetadataSykmelding
import no.nav.syfo.toRuleMetadata
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

enum class TestRules {
    RULE2,
}

internal class RuleExecutionServiceTest {
    private val sykmeldnig = mockk<Sykmelding>(relaxed = true)
    private val ruleMetadataSykmelding = mockk<RuleMetadataSykmelding>(relaxed = true)
    private val rulesExecution = mockk<RuleExecution<TestRules>>(relaxed = true)
    private val ruleExecutionService = RuleExecutionService()

    @Test
    internal fun `Should include all rules`() {
        val sykmelding = generateSykemelding()
        val behandler =
            Behandler(
                listOf(
                    Godkjenning(
                        autorisasjon =
                            Kode(
                                aktiv = true,
                                oid = 7704,
                                verdi = "1",
                            ),
                        helsepersonellkategori =
                            Kode(
                                aktiv = true,
                                oid = 0,
                                verdi = "LE",
                            ),
                    ),
                ),
            )
        val result =
            ruleExecutionService.runRules(
                generateSykemelding(),
                ruleMetadataSykmelding(
                        sykmelding
                            .toRuleMetadata()
                            .copy(
                                pasientFodselsdato = LocalDate.now().minusYears(20),
                            ),
                    )
                    .copy(
                        behandlerOgStartdato =
                            BehandlerOgStartdato(
                                behandler,
                                sykmelding.perioder.sortedFOMDate().first(),
                            ),
                    ),
            )
        assertEquals(8, result.size)
    }

    @Test
    internal fun `Run ruleTrees`() {
        every {
            rulesExecution.runRules(
                any(),
                any(),
            )
        } returns
            TreeOutput<TestRules, RuleResult>(
                treeResult =
                    RuleResult(
                        status = Status.OK,
                        JuridiskEnum.INGEN.JuridiskHenvisning,
                        ruleHit = null,
                    ),
            )

        val rule =
            ruleExecutionService
                .runRules(sykmeldnig, ruleMetadataSykmelding, sequenceOf(rulesExecution))
                .first()

        assertEquals(Status.OK, rule.treeResult.status)
        assertEquals(UtenJuridisk, rule.treeResult.juridisk)
    }

    @Test
    internal fun `should not run all rules if first no OK`() {
        val okRule =
            mockk<RuleExecution<TestRules>>().also {
                every { it.runRules(any(), any()) } returns
                    TreeOutput<TestRules, RuleResult>(
                        treeResult =
                            RuleResult(
                                status = Status.OK,
                                juridisk = JuridiskEnum.INGEN.JuridiskHenvisning,
                                ruleHit = null,
                            ),
                    )
            }
        val manuallRuleExecution =
            mockk<RuleExecution<TestRules>>().also {
                every { it.runRules(any(), any()) } returns
                    TreeOutput<TestRules, RuleResult>(
                        treeResult =
                            RuleResult(
                                status = Status.MANUAL_PROCESSING,
                                juridisk = JuridiskEnum.INGEN.JuridiskHenvisning,
                                ruleHit =
                                    RuleHit(
                                        Status.MANUAL_PROCESSING,
                                        TestRules.RULE2.name,
                                        "message",
                                        "message",
                                    ),
                            ),
                    )
            }
        val results =
            ruleExecutionService.runRules(
                sykmeldnig,
                ruleMetadataSykmelding,
                sequenceOf(manuallRuleExecution, okRule),
            )
        assertEquals(1, results.size)
        assertEquals(Status.MANUAL_PROCESSING, results.first().treeResult.status)
    }
}
