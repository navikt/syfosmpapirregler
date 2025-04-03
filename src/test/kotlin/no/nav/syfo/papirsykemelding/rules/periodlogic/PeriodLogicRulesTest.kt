package no.nav.syfo.papirsykemelding.rules.periodlogic

import java.time.LocalDate
import no.nav.syfo.generateGradert
import no.nav.syfo.generatePeriode
import no.nav.syfo.generateSykemelding
import no.nav.syfo.model.AktivitetIkkeMulig
import no.nav.syfo.model.Status
import no.nav.syfo.ruleMetadataSykmelding
import no.nav.syfo.toRuleMetadata
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class PeriodLogicRulesTest {
    private val ruleTree = PeriodLogicRulesExecution()

    @Test
    internal fun `Alt er ok, Status OK`() {
        val sykmelding =
            generateSykemelding(
                perioder =
                    listOf(
                        generatePeriode(
                            fom = LocalDate.now(),
                            tom = LocalDate.now().plusDays(10),
                            aktivitetIkkeMulig =
                                AktivitetIkkeMulig(
                                    medisinskArsak = null,
                                    arbeidsrelatertArsak = null
                                )
                        ),
                    ),
                tidspunkt = LocalDate.now().atStartOfDay(),
            )

        val ruleMetadata = sykmelding.toRuleMetadata()

        val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata))

        status.treeResult.status shouldBeEqualTo Status.OK
        status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
            listOf(
                PeriodLogicRules.PERIODER_MANGLER to false,
                PeriodLogicRules.FRADATO_ETTER_TILDATO to false,
                PeriodLogicRules.OVERLAPPENDE_PERIODER to false,
                PeriodLogicRules.OPPHOLD_MELLOM_PERIODER to false,
                PeriodLogicRules.IKKE_DEFINERT_PERIODE to false,
                PeriodLogicRules.AVVENTENDE_SYKMELDING_KOMBINERT to false,
                PeriodLogicRules.MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER to false,
                PeriodLogicRules.AVVENTENDE_SYKMELDING_OVER_16_DAGER to false,
                PeriodLogicRules.FOR_MANGE_BEHANDLINGSDAGER_PER_UKE to false,
                PeriodLogicRules.GRADERT_SYKMELDING_OVER_99_PROSENT to false,
                PeriodLogicRules.GRADERT_SYKMELDING_0_PROSENT to false,
                PeriodLogicRules.SYKMELDING_MED_BEHANDLINGSDAGER to false,
            )

        mapOf(
            "perioder" to sykmelding.perioder,
            "periodeRanges" to sykmelding.perioder.sortedBy { it.fom }.map { it.fom to it.tom },
            "avventendeKombinert" to false,
            "manglendeInnspillArbeidsgiver" to false,
            "avventendeOver16Dager" to false,
            "forMangeBehandlingsDagerPrUke" to false,
            "gradertePerioder" to sykmelding.perioder.mapNotNull { it.gradert },
            "inneholderBehandlingsDager" to false,
        ) shouldBeEqualTo status.ruleInputs

        status.treeResult.ruleHit shouldBeEqualTo null
    }

    @Test
    internal fun `Periode mangler, Status MANUAL_PROCESSING`() {
        val sykmelding =
            generateSykemelding(
                perioder = listOf(),
            )
        val ruleMetadata = sykmelding.toRuleMetadata()

        val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata))

        status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
        status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
            listOf(
                PeriodLogicRules.PERIODER_MANGLER to true,
            )

        mapOf(
            "perioder" to sykmelding.perioder,
        ) shouldBeEqualTo status.ruleInputs

        status.treeResult.ruleHit shouldBeEqualTo PeriodLogicRuleHit.PERIODER_MANGLER.ruleHit
    }

    @Test
    internal fun `Fra dato er etter til dato, Status MANUAL_PROCESSING`() {
        val sykmelding =
            generateSykemelding(
                perioder =
                    listOf(
                        generatePeriode(
                            fom = LocalDate.of(2018, 1, 9),
                            tom = LocalDate.of(2018, 1, 7),
                        ),
                    ),
            )

        val ruleMetadata = sykmelding.toRuleMetadata()

        val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata))

        status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
        status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
            listOf(
                PeriodLogicRules.PERIODER_MANGLER to false,
                PeriodLogicRules.FRADATO_ETTER_TILDATO to true,
            )

        mapOf(
            "perioder" to sykmelding.perioder,
        ) shouldBeEqualTo status.ruleInputs

        status.treeResult.ruleHit shouldBeEqualTo PeriodLogicRuleHit.FRADATO_ETTER_TILDATO.ruleHit
    }

    @Test
    internal fun `Overlapp i perioder, Status MANUAL_PROCESSING`() {
        val sykmelding =
            generateSykemelding(
                perioder =
                    listOf(
                        generatePeriode(
                            fom = LocalDate.of(2018, 1, 7),
                            tom = LocalDate.of(2018, 1, 9),
                        ),
                        generatePeriode(
                            fom = LocalDate.of(2018, 1, 8),
                            tom = LocalDate.of(2018, 1, 12),
                        ),
                    ),
            )

        val ruleMetadata = sykmelding.toRuleMetadata()

        val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata))

        status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
        status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
            listOf(
                PeriodLogicRules.PERIODER_MANGLER to false,
                PeriodLogicRules.FRADATO_ETTER_TILDATO to false,
                PeriodLogicRules.OVERLAPPENDE_PERIODER to true,
            )

        mapOf(
            "perioder" to sykmelding.perioder,
        ) shouldBeEqualTo status.ruleInputs

        status.treeResult.ruleHit shouldBeEqualTo PeriodLogicRuleHit.OVERLAPPENDE_PERIODER.ruleHit
    }

    @Test
    internal fun `Opphold mellom perioder, Status MANUAL_PROCESSING`() {
        val sykmelding =
            generateSykemelding(
                perioder =
                    listOf(
                        generatePeriode(
                            fom = LocalDate.of(2018, 1, 1),
                            tom = LocalDate.of(2018, 1, 3),
                        ),
                        generatePeriode(
                            fom = LocalDate.of(2018, 1, 4),
                            tom = LocalDate.of(2018, 1, 9),
                        ),
                        generatePeriode(
                            fom = LocalDate.of(2018, 1, 10),
                            tom = LocalDate.of(2018, 1, 11),
                        ),
                        generatePeriode(
                            fom = LocalDate.of(2018, 1, 15),
                            tom = LocalDate.of(2018, 1, 20),
                        ),
                    ),
            )

        val ruleMetadata = sykmelding.toRuleMetadata()

        val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata))

        status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
        status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
            listOf(
                PeriodLogicRules.PERIODER_MANGLER to false,
                PeriodLogicRules.FRADATO_ETTER_TILDATO to false,
                PeriodLogicRules.OVERLAPPENDE_PERIODER to false,
                PeriodLogicRules.OPPHOLD_MELLOM_PERIODER to true,
            )

        mapOf(
            "perioder" to sykmelding.perioder,
            "periodeRanges" to sykmelding.perioder.sortedBy { it.fom }.map { it.fom to it.tom },
        ) shouldBeEqualTo status.ruleInputs

        status.treeResult.ruleHit shouldBeEqualTo PeriodLogicRuleHit.OPPHOLD_MELLOM_PERIODER.ruleHit
    }

    @Test
    internal fun `Avvendte kombinert med annen type periode, Status MANUAL_PROCESSING`() {
        val sykmelding =
            generateSykemelding(
                perioder =
                    listOf(
                        generatePeriode(
                            fom = LocalDate.now(),
                            tom = LocalDate.now().plusDays(5),
                            avventendeInnspillTilArbeidsgiver = "Bør gå minst mulig på jobb",
                        ),
                        generatePeriode(
                            fom = LocalDate.now().plusDays(6),
                            tom = LocalDate.now().plusDays(10),
                            aktivitetIkkeMulig =
                                AktivitetIkkeMulig(
                                    medisinskArsak = null,
                                    arbeidsrelatertArsak = null,
                                )
                        ),
                    ),
            )

        val ruleMetadata = sykmelding.toRuleMetadata()

        val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata))

        status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
        status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
            listOf(
                PeriodLogicRules.PERIODER_MANGLER to false,
                PeriodLogicRules.FRADATO_ETTER_TILDATO to false,
                PeriodLogicRules.OVERLAPPENDE_PERIODER to false,
                PeriodLogicRules.OPPHOLD_MELLOM_PERIODER to false,
                PeriodLogicRules.IKKE_DEFINERT_PERIODE to false,
                PeriodLogicRules.AVVENTENDE_SYKMELDING_KOMBINERT to true,
            )

        // Expected: <[(PERIODER_MANGLER, false), (FRADATO_ETTER_TILDATO, false),
        // (OVERLAPPENDE_PERIODER, false), (OPPHOLD_MELLOM_PERIODER, false), (IKKE_DEFINERT_PERIODE,
        // false), (AVVENTENDE_SYKMELDING_KOMBINERT, true)]>
        // but was:  <[(PERIODER_MANGLER, false), (FRADATO_ETTER_TILDATO, false),
        // (OVERLAPPENDE_PERIODER, false), (OPPHOLD_MELLOM_PERIODER, false), (IKKE_DEFINERT_PERIODE,
        // true)]>

        mapOf(
            "perioder" to sykmelding.perioder,
            "periodeRanges" to sykmelding.perioder.sortedBy { it.fom }.map { it.fom to it.tom },
            "perioder" to sykmelding.perioder,
            "avventendeKombinert" to true,
        ) shouldBeEqualTo status.ruleInputs

        status.treeResult.ruleHit shouldBeEqualTo
            PeriodLogicRuleHit.AVVENTENDE_SYKMELDING_KOMBINERT.ruleHit
    }

    @Test
    internal fun `Manglende innstill til arbeidsgiver, Status MANUAL_PROCESSING`() {
        val sykmelding =
            generateSykemelding(
                perioder =
                    listOf(
                        generatePeriode(
                            fom = LocalDate.now(),
                            tom = LocalDate.now().plusDays(5),
                            avventendeInnspillTilArbeidsgiver = "      ",
                        ),
                    ),
            )

        val ruleMetadata = sykmelding.toRuleMetadata()

        val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata))

        status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
        status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
            listOf(
                PeriodLogicRules.PERIODER_MANGLER to false,
                PeriodLogicRules.FRADATO_ETTER_TILDATO to false,
                PeriodLogicRules.OVERLAPPENDE_PERIODER to false,
                PeriodLogicRules.OPPHOLD_MELLOM_PERIODER to false,
                PeriodLogicRules.IKKE_DEFINERT_PERIODE to false,
                PeriodLogicRules.AVVENTENDE_SYKMELDING_KOMBINERT to false,
                PeriodLogicRules.MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER to true,
            )

        mapOf(
            "perioder" to sykmelding.perioder,
            "periodeRanges" to sykmelding.perioder.sortedBy { it.fom }.map { it.fom to it.tom },
            "perioder" to sykmelding.perioder,
            "avventendeKombinert" to false,
            "manglendeInnspillArbeidsgiver" to true,
        ) shouldBeEqualTo status.ruleInputs

        status.treeResult.ruleHit shouldBeEqualTo
            PeriodLogicRuleHit.MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER.ruleHit
    }

    @Test
    internal fun `Avventende over 16 dager, Status MANUAL_PROCESSING`() {
        val sykmelding =
            generateSykemelding(
                perioder =
                    listOf(
                        generatePeriode(
                            fom = LocalDate.now(),
                            tom = LocalDate.now().plusDays(17),
                            avventendeInnspillTilArbeidsgiver = "Bør gå minst mulig på jobb",
                        ),
                    ),
            )

        val ruleMetadata = sykmelding.toRuleMetadata()

        val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata))

        status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
        status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
            listOf(
                PeriodLogicRules.PERIODER_MANGLER to false,
                PeriodLogicRules.FRADATO_ETTER_TILDATO to false,
                PeriodLogicRules.OVERLAPPENDE_PERIODER to false,
                PeriodLogicRules.OPPHOLD_MELLOM_PERIODER to false,
                PeriodLogicRules.IKKE_DEFINERT_PERIODE to false,
                PeriodLogicRules.AVVENTENDE_SYKMELDING_KOMBINERT to false,
                PeriodLogicRules.MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER to false,
                PeriodLogicRules.AVVENTENDE_SYKMELDING_OVER_16_DAGER to true,
            )

        mapOf(
            "perioder" to sykmelding.perioder,
            "periodeRanges" to sykmelding.perioder.sortedBy { it.fom }.map { it.fom to it.tom },
            "perioder" to sykmelding.perioder,
            "avventendeKombinert" to false,
            "manglendeInnspillArbeidsgiver" to false,
            "avventendeOver16Dager" to true,
        ) shouldBeEqualTo status.ruleInputs

        status.treeResult.ruleHit shouldBeEqualTo
            PeriodLogicRuleHit.AVVENTENDE_SYKMELDING_OVER_16_DAGER.ruleHit
    }

    @Test
    internal fun `For mange behandlingsdager pr uke, Status MANUAL_PROCESSING`() {
        val sykmelding =
            generateSykemelding(
                perioder =
                    listOf(
                        generatePeriode(
                            fom = LocalDate.now(),
                            tom = LocalDate.now(),
                            behandlingsdager = 2,
                        ),
                    ),
            )

        val ruleMetadata = sykmelding.toRuleMetadata()

        val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata))

        status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
        status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
            listOf(
                PeriodLogicRules.PERIODER_MANGLER to false,
                PeriodLogicRules.FRADATO_ETTER_TILDATO to false,
                PeriodLogicRules.OVERLAPPENDE_PERIODER to false,
                PeriodLogicRules.OPPHOLD_MELLOM_PERIODER to false,
                PeriodLogicRules.IKKE_DEFINERT_PERIODE to false,
                PeriodLogicRules.AVVENTENDE_SYKMELDING_KOMBINERT to false,
                PeriodLogicRules.MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER to false,
                PeriodLogicRules.AVVENTENDE_SYKMELDING_OVER_16_DAGER to false,
                PeriodLogicRules.FOR_MANGE_BEHANDLINGSDAGER_PER_UKE to true,
            )

        mapOf(
            "perioder" to sykmelding.perioder,
            "periodeRanges" to sykmelding.perioder.sortedBy { it.fom }.map { it.fom to it.tom },
            "perioder" to sykmelding.perioder,
            "avventendeKombinert" to false,
            "manglendeInnspillArbeidsgiver" to false,
            "avventendeOver16Dager" to false,
            "forMangeBehandlingsDagerPrUke" to true,
        ) shouldBeEqualTo status.ruleInputs

        status.treeResult.ruleHit shouldBeEqualTo
            PeriodLogicRuleHit.FOR_MANGE_BEHANDLINGSDAGER_PER_UKE.ruleHit
    }

    @Test
    internal fun `Gradert 0 prosent, Status MANUAL`() {
        val sykmelding =
            generateSykemelding(
                perioder =
                    listOf(
                        generatePeriode(
                            fom = LocalDate.now(),
                            tom = LocalDate.now(),
                            gradert = generateGradert(grad = 0),
                        ),
                    ),
            )

        val ruleMetadata = sykmelding.toRuleMetadata()

        val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata))

        status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
        status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
            listOf(
                PeriodLogicRules.PERIODER_MANGLER to false,
                PeriodLogicRules.FRADATO_ETTER_TILDATO to false,
                PeriodLogicRules.OVERLAPPENDE_PERIODER to false,
                PeriodLogicRules.OPPHOLD_MELLOM_PERIODER to false,
                PeriodLogicRules.IKKE_DEFINERT_PERIODE to false,
                PeriodLogicRules.AVVENTENDE_SYKMELDING_KOMBINERT to false,
                PeriodLogicRules.MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER to false,
                PeriodLogicRules.AVVENTENDE_SYKMELDING_OVER_16_DAGER to false,
                PeriodLogicRules.FOR_MANGE_BEHANDLINGSDAGER_PER_UKE to false,
                PeriodLogicRules.GRADERT_SYKMELDING_OVER_99_PROSENT to false,
                PeriodLogicRules.GRADERT_SYKMELDING_0_PROSENT to true,
            )

        mapOf(
            "perioder" to sykmelding.perioder,
            "periodeRanges" to sykmelding.perioder.sortedBy { it.fom }.map { it.fom to it.tom },
            "perioder" to sykmelding.perioder,
            "avventendeKombinert" to false,
            "manglendeInnspillArbeidsgiver" to false,
            "avventendeOver16Dager" to false,
            "forMangeBehandlingsDagerPrUke" to false,
            "gradertePerioder" to sykmelding.perioder.mapNotNull { it.gradert },
        ) shouldBeEqualTo status.ruleInputs

        status.treeResult.ruleHit shouldBeEqualTo
            PeriodLogicRuleHit.GRADERT_SYKMELDING_O_PROSENT.ruleHit
    }

    @Test
    internal fun `Gradert over 99 prosent, Status MANUAL`() {
        val sykmelding =
            generateSykemelding(
                perioder =
                    listOf(
                        generatePeriode(
                            fom = LocalDate.now(),
                            tom = LocalDate.now(),
                            gradert = generateGradert(grad = 100),
                        ),
                    ),
            )

        val ruleMetadata = sykmelding.toRuleMetadata()

        val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata))

        status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
        status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo
            listOf(
                PeriodLogicRules.PERIODER_MANGLER to false,
                PeriodLogicRules.FRADATO_ETTER_TILDATO to false,
                PeriodLogicRules.OVERLAPPENDE_PERIODER to false,
                PeriodLogicRules.OPPHOLD_MELLOM_PERIODER to false,
                PeriodLogicRules.IKKE_DEFINERT_PERIODE to false,
                PeriodLogicRules.AVVENTENDE_SYKMELDING_KOMBINERT to false,
                PeriodLogicRules.MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER to false,
                PeriodLogicRules.AVVENTENDE_SYKMELDING_OVER_16_DAGER to false,
                PeriodLogicRules.FOR_MANGE_BEHANDLINGSDAGER_PER_UKE to false,
                PeriodLogicRules.GRADERT_SYKMELDING_OVER_99_PROSENT to true,
            )

        mapOf(
            "perioder" to sykmelding.perioder,
            "periodeRanges" to sykmelding.perioder.sortedBy { it.fom }.map { it.fom to it.tom },
            "perioder" to sykmelding.perioder,
            "avventendeKombinert" to false,
            "manglendeInnspillArbeidsgiver" to false,
            "avventendeOver16Dager" to false,
            "forMangeBehandlingsDagerPrUke" to false,
            "gradertePerioder" to sykmelding.perioder.mapNotNull { it.gradert },
        ) shouldBeEqualTo status.ruleInputs

        status.treeResult.ruleHit shouldBeEqualTo
            PeriodLogicRuleHit.GRADERT_SYKMELDING_OVER_99_PROSENT.ruleHit
    }
}
