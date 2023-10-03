package no.nav.syfo.papirsykemelding.service

import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import no.nav.syfo.client.norskhelsenett.Behandler
import no.nav.syfo.client.norskhelsenett.Godkjenning
import no.nav.syfo.client.norskhelsenett.Kode
import no.nav.syfo.generateSykemelding
import no.nav.syfo.model.Status
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.papirsykemelding.model.sortedFOMDate
import no.nav.syfo.papirsykemelding.rules.common.RuleExecution
import no.nav.syfo.papirsykemelding.rules.common.RuleHit
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.common.UtenJuridisk
import no.nav.syfo.papirsykemelding.rules.dsl.TreeOutput
import no.nav.syfo.papirsykemelding.rules.validation.ruleMetadataSykmelding
import no.nav.syfo.toRuleMetadata
import org.amshove.kluent.shouldBeEqualTo

enum class TestRules {
    RULE2,
}

class RuleExecutionServiceTest :
    FunSpec({
        val sykmeldnig = mockk<Sykmelding>(relaxed = true)
        val ruleMetadataSykmelding = mockk<RuleMetadataSykmelding>(relaxed = true)
        val rulesExecution = mockk<RuleExecution<TestRules>>(relaxed = true)
        val ruleExecutionService = RuleExecutionService()
        test("Should include all rules") {
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

            result.size shouldBeEqualTo 10
        }
        test("Run ruleTrees") {
            every {
                rulesExecution.runRules(
                    any(),
                    any(),
                )
            } returns
                (TreeOutput<TestRules, RuleResult>(
                    treeResult =
                        RuleResult(
                            status = Status.OK,
                            ruleHit = null,
                        ),
                ) to UtenJuridisk)

            val (rule, juridisk) =
                ruleExecutionService
                    .runRules(sykmeldnig, ruleMetadataSykmelding, sequenceOf(rulesExecution))
                    .first()
            rule.treeResult.status shouldBeEqualTo Status.OK
            juridisk shouldBeEqualTo UtenJuridisk
        }

        test("should not run all rules if first no OK") {
            val okRule =
                mockk<RuleExecution<TestRules>>().also {
                    every { it.runRules(any(), any()) } returns
                        (TreeOutput<TestRules, RuleResult>(
                            treeResult =
                                RuleResult(
                                    status = Status.OK,
                                    ruleHit = null,
                                ),
                        ) to UtenJuridisk)
                }
            val manuallRuleExecution =
                mockk<RuleExecution<TestRules>>().also {
                    every { it.runRules(any(), any()) } returns
                        (TreeOutput<TestRules, RuleResult>(
                            treeResult =
                                RuleResult(
                                    status = Status.MANUAL_PROCESSING,
                                    ruleHit =
                                        RuleHit(
                                            Status.MANUAL_PROCESSING,
                                            TestRules.RULE2.name,
                                            "message",
                                            "message"
                                        ),
                                ),
                        ) to UtenJuridisk)
                }
            val results =
                ruleExecutionService.runRules(
                    sykmeldnig,
                    ruleMetadataSykmelding,
                    sequenceOf(manuallRuleExecution, okRule)
                )
            results.size shouldBeEqualTo 1
            results.first().first.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
        }
    })
