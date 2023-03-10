package no.nav.syfo.papirsykemelding.rules.syketilfelle

import io.kotest.core.spec.style.FunSpec
import java.time.LocalDate
import no.nav.syfo.generatePeriode
import no.nav.syfo.generateSykemelding
import no.nav.syfo.model.Status
import no.nav.syfo.ruleMetadataSykmelding
import no.nav.syfo.toRuleMetadata
import org.amshove.kluent.shouldBeEqualTo

class SyketilfelleTest : FunSpec({
    //TODO fix me
    val ruleTree = SyketilfelleRulesExecution()

    context("Testing syketilfelle rules and checking the rule outcomes") {
        test("Alt er ok, Status OK") {
            val sykmelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.of(2019, 1, 10),
                        tom = LocalDate.of(2019, 1, 20)
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
                SyketilfelleRules.OPPHOLD_MELLOM_PERIODER to false,
                SyketilfelleRules.IKKE_DEFINERT_PERIODE to false,
                SyketilfelleRules.TILBAKEDATERT_MER_ENN_3_AR to false,
                SyketilfelleRules.FREMDATERT to false,
                SyketilfelleRules.TOTAL_VARIGHET_OVER_ETT_AAR to false,
                SyketilfelleRules.BEHANDLINGSDATO_ETTER_MOTTATTDATO to false,
                SyketilfelleRules.AVVENTENDE_SYKMELDING_KOMBINERT to false,
                SyketilfelleRules.MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER to false,
                SyketilfelleRules.AVVENTENDE_SYKMELDING_OVER_16_DAGER to false,
                SyketilfelleRules.FOR_MANGE_BEHANDLINGSDAGER_PER_UKE to false,
                SyketilfelleRules.GRADERT_SYKMELDING_OVER_99_PROSENT to false,
                SyketilfelleRules.SYKMELDING_MED_BEHANDLINGSDAGER to false

            )

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
    }
})