package no.nav.syfo.papirsykemelding.rules.gradert

import io.kotest.core.spec.style.FunSpec
import no.nav.syfo.client.norskhelsenett.Behandler
import no.nav.syfo.generateGradert
import no.nav.syfo.generatePeriode
import no.nav.syfo.generateSykemelding
import no.nav.syfo.model.Status
import no.nav.syfo.papirsykemelding.service.BehandlerOgStartdato
import no.nav.syfo.papirsykemelding.service.RuleMetadataSykmelding
import no.nav.syfo.toRuleMetadata
import org.amshove.kluent.shouldBeEqualTo
import java.time.LocalDate

class GradertTest : FunSpec({

    val ruleTree = GradertRulesExecution()

    context("Testing gradert rules and checking the rule outcomes") {
        test("Sick leave is 21 procent, should be OK") {
            val sykmelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now(),
                        tom = LocalDate.now(),
                        gradert = generateGradert(grad = 21),
                    ),
                ),
            )

            val ruleMetadata = sykmelding.toRuleMetadata()

            val ruleMetadataSykmelding = RuleMetadataSykmelding(
                ruleMetadata = ruleMetadata,
                erNyttSyketilfelle = false,
                doctorSuspensjon = false,
                behandlerOgStartdato = BehandlerOgStartdato(Behandler(emptyList(), null), null),
            )

            val result = ruleTree.runRules(sykmelding, ruleMetadataSykmelding)

            result.first.treeResult.status shouldBeEqualTo Status.OK
        }

        test("Sick leave is 19 procent, should be MANUAL_PROCESSING") {
            val sykmelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now(),
                        tom = LocalDate.now(),
                        gradert = generateGradert(grad = 19),
                    ),
                ),
            )

            val ruleMetadata = sykmelding.toRuleMetadata()

            val ruleMetadataSykmelding = RuleMetadataSykmelding(
                ruleMetadata = ruleMetadata,
                erNyttSyketilfelle = false,
                doctorSuspensjon = false,
                behandlerOgStartdato = BehandlerOgStartdato(Behandler(emptyList(), null), null),
            )

            val result = ruleTree.runRules(sykmelding, ruleMetadataSykmelding)

            result.first.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
        }
    }
})
