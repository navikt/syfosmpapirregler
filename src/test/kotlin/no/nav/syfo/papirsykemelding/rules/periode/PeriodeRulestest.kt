package no.nav.syfo.papirsykemelding.rules.periode

import java.time.LocalDate
import java.time.LocalDateTime
import no.nav.syfo.generatePeriode
import no.nav.syfo.generateSykemelding
import no.nav.syfo.model.Status
import no.nav.syfo.papirsykemelding.model.sortedFOMDate
import no.nav.syfo.papirsykemelding.model.sortedTOMDate
import no.nav.syfo.papirsykemelding.rules.validation.ruleMetadataSykmelding
import no.nav.syfo.toRuleMetadata
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class PeriodeRulestest {
    private val periodeRules = PeriodeRulesExecution()

    @Test
    internal fun `Testing periode regler OK`() {
        val sykmelding = generateSykemelding()
        val ruleMetadata = ruleMetadataSykmelding(sykmelding.toRuleMetadata())
        val status = periodeRules.runRules(sykmelding, ruleMetadata)
        status.treeResult.status shouldBeEqualTo Status.OK
        status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
            listOf(
                PeriodeRules.FREMDATERT to false,
                PeriodeRules.TILBAKEDATERT_MER_ENN_3_AR to false,
                PeriodeRules.TOTAL_VARIGHET_OVER_ETT_AAR to false,
            )
        mapOf(
                "genereringsTidspunkt" to ruleMetadata.ruleMetadata.signatureDate,
                "fom" to sykmelding.perioder.sortedFOMDate().first(),
                "tom" to sykmelding.perioder.sortedTOMDate().last(),
                "fremdatert" to false,
                "tilbakeDatertMerEnn3AAr" to false,
                "varighetOver1AAr" to false,
            )
            .toSortedMap() shouldBeEqualTo status.ruleInputs.toSortedMap()
    }

    @Test
    internal fun `Testing periode regler Fremdater over 30 dager, Status MANUAL_PROCESSING`() {

        val sykmelding =
            generateSykemelding(
                perioder =
                    listOf(
                        generatePeriode(
                            fom = LocalDate.now().plusDays(31),
                            tom = LocalDate.now().plusDays(37),
                        ),
                    ),
                tidspunkt = LocalDateTime.now(),
            )

        val ruleMetadata = sykmelding.toRuleMetadata()

        val status = periodeRules.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata))

        status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
        status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
            listOf(
                PeriodeRules.FREMDATERT to true,
            )

        mapOf(
            "genereringsTidspunkt" to ruleMetadata.signatureDate,
            "fom" to sykmelding.perioder.sortedFOMDate().first(),
            "fremdatert" to true,
        ) shouldBeEqualTo status.ruleInputs

        status.treeResult.ruleHit shouldBeEqualTo PeriodeRuleHit.FREMDATERT.ruleHit
    }

    @Test
    internal fun `Testing periode regler Varighet over 1 år, Status MANUAL_PROCESSING`() {
        val sykmelding =
            generateSykemelding(
                perioder =
                    listOf(
                        generatePeriode(
                            fom = LocalDate.now(),
                            tom = LocalDate.now(),
                        ),
                        generatePeriode(
                            fom = LocalDate.now().plusDays(1),
                            tom = LocalDate.now().plusDays(366),
                        ),
                    ),
                tidspunkt = LocalDateTime.now(),
            )

        val ruleMetadata = sykmelding.toRuleMetadata()

        val status = periodeRules.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata))

        status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
        status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
            listOf(
                PeriodeRules.FREMDATERT to false,
                PeriodeRules.TILBAKEDATERT_MER_ENN_3_AR to false,
                PeriodeRules.TOTAL_VARIGHET_OVER_ETT_AAR to true,
            )

        mapOf(
                "genereringsTidspunkt" to ruleMetadata.signatureDate,
                "fom" to sykmelding.perioder.sortedFOMDate().first(),
                "tom" to sykmelding.perioder.sortedTOMDate().last(),
                "fremdatert" to false,
                "tilbakeDatertMerEnn3AAr" to false,
                "varighetOver1AAr" to true,
            )
            .toSortedMap() shouldBeEqualTo status.ruleInputs.toSortedMap()

        status.treeResult.ruleHit shouldBeEqualTo PeriodeRuleHit.TOTAL_VARIGHET_OVER_ETT_AAR.ruleHit
    }
}
