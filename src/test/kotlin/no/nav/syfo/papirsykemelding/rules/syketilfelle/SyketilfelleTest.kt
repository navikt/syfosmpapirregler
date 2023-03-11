package no.nav.syfo.papirsykemelding.rules.syketilfelle

import io.kotest.core.spec.style.FunSpec
import no.nav.syfo.generatePeriode
import no.nav.syfo.generateSykemelding
import no.nav.syfo.model.Status
import no.nav.syfo.ruleMetadataSykmelding
import no.nav.syfo.toRuleMetadata
import org.amshove.kluent.shouldBeEqualTo
import java.time.LocalDate

class SyketilfelleTest : FunSpec({
    val ruleTree = SyketilfelleRulesExecution()

    context("Testing syketilfelle rules and checking the rule outcomes") {
        test("Noene of the rules hits, Status OK") {
            val sykmelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now(),
                        tom = LocalDate.now().plusDays(7)
                    )
                ),
                tidspunkt = LocalDate.now().atStartOfDay()
            )

            val ruleMetadata = sykmelding.toRuleMetadata()

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata)).first

            status.treeResult.status shouldBeEqualTo Status.OK
            status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo listOf(
                SyketilfelleRules.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING to false,
                SyketilfelleRules.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING_MED_BEGRUNNELSE to false,
                SyketilfelleRules.TILBAKEDATERT_INNTIL_8_DAGER_UTEN_KONTAKTDATO_OG_BEGRUNNELSE to false,
                SyketilfelleRules.TILBAKEDATERT_FORLENGELSE_OVER_1_MND to false,
                SyketilfelleRules.TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE to false
            )

            // TODO fix me
            mapOf(
                "perioder" to sykmelding.perioder,
                "periodeRanges" to sykmelding.perioder
                    .sortedBy { it.fom }
                    .map { it.fom to it.tom },
                "tilbakeDatertMerEnn3AAr" to false,
                "fremdatert" to false,
                "varighetOver1AAr" to false,
                "behandslingsDatoEtterMottatDato" to false,
                "avventendeKombinert" to false,
                "manglendeInnspillArbeidsgiver" to false,
                "avventendeOver16Dager" to false,
                "forMangeBehandlingsDagerPrUke" to false,
                "gradertOver99Prosent" to false,
                "inneholderBehandlingsDager" to false
            ) shouldBeEqualTo status.ruleInputs

            status.treeResult.ruleHit shouldBeEqualTo null
        }

        // TODO add in rest of tests
    }
})
