package no.nav.syfo.papirsykemelding.rules

import no.nav.syfo.generateGradert
import no.nav.syfo.generatePeriode
import no.nav.syfo.generateSykemelding
import no.nav.syfo.model.Periode
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.LocalDate
import java.time.LocalDateTime

object PeriodLogicRuleChainSpek : Spek({
    fun ruleData(
        receivedDate: LocalDateTime = LocalDateTime.now(),
        signatureDate: LocalDateTime = LocalDateTime.now(),
        patientPersonNumber: String = "1234567891",
        tssid: String? = "1314445"
    ): RuleMetadata =
        RuleMetadata(
            signatureDate,
            receivedDate,
            LocalDateTime.now(),
            patientPersonNumber,
            "1",
            "123456789",
            tssid
        )

    describe("Testing validation rules and checking the rule outcomes") {

        it("Should check rule PERIODER_MANGLER, should trigger rule") {
            val sykemelding = generateSykemelding(
                perioder = listOf()
            )

            PeriodLogicRuleChain(sykemelding, ruleData()).getRuleByName("PERIODER_MANGLER").executeRule().result shouldBeEqualTo true
        }

        it("Should check rule PERIODER_MANGLER, should NOT trigger rule") {
            val sykemelding = generateSykemelding()

            PeriodLogicRuleChain(sykemelding, ruleData()).getRuleByName("PERIODER_MANGLER").executeRule().result shouldBeEqualTo false
        }

        it("Should check rule FRADATO_ETTER_TILDATO, should trigger rule") {
            val sykemelding = generateSykemelding(perioder = listOf(generatePeriode(fom = LocalDate.of(2018, 1, 9), tom = LocalDate.of(2018, 1, 7))))

            PeriodLogicRuleChain(sykemelding, ruleData()).getRuleByName("FRADATO_ETTER_TILDATO").executeRule().result shouldBeEqualTo true
        }

        it("Should check rule FRADATO_ETTER_TILDATO, should NOT trigger rule") {
            val sykemelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.of(2018, 1, 7),
                        tom = LocalDate.of(2018, 1, 9)
                    )
                )
            )

            PeriodLogicRuleChain(sykemelding, ruleData()).getRuleByName("FRADATO_ETTER_TILDATO").executeRule().result shouldBeEqualTo false
        }

        it("Should check rule OVERLAPPENDE_PERIODER, should trigger rule") {
            val sykemelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.of(2018, 1, 7),
                        tom = LocalDate.of(2018, 1, 9)
                    ),
                    generatePeriode(
                        fom = LocalDate.of(2018, 1, 8),
                        tom = LocalDate.of(2018, 1, 12)
                    )
                )
            )

            PeriodLogicRuleChain(sykemelding, ruleData()).getRuleByName("OVERLAPPENDE_PERIODER").executeRule().result shouldBeEqualTo true
        }

        it("Should check rule OVERLAPPENDE_PERIODER, should NOT trigger rule") {
            val sykemelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.of(2018, 1, 7),
                        tom = LocalDate.of(2018, 1, 9)
                    ),
                    generatePeriode(
                        fom = LocalDate.of(2018, 1, 10),
                        tom = LocalDate.of(2018, 1, 12)
                    )
                )
            )

            PeriodLogicRuleChain(sykemelding, ruleData()).getRuleByName("OVERLAPPENDE_PERIODER").executeRule().result shouldBeEqualTo false
        }

        it("Should check rule OPPHOLD_MELLOM_PERIODER, should trigger rule") {
            val healthInformation = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.of(2020, 2, 15),
                        tom = LocalDate.of(2020, 3, 18)
                    ),
                    generatePeriode(
                        fom = LocalDate.of(2019, 10, 31),
                        tom = LocalDate.of(2019, 12, 19)
                    ),
                    generatePeriode(
                        fom = LocalDate.of(2020, 3, 19),
                        tom = LocalDate.of(2020, 3, 26)
                    )
                )
            )

            PeriodLogicRuleChain(healthInformation, ruleData()).getRuleByName("OPPHOLD_MELLOM_PERIODER").executeRule().result shouldBeEqualTo true
        }

        it("Should check rule OPPHOLD_MELLOM_PERIODER, should trigger rule") {
            val sykemelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.of(2018, 1, 1),
                        tom = LocalDate.of(2018, 1, 3)
                    ),
                    generatePeriode(
                        fom = LocalDate.of(2018, 1, 3),
                        tom = LocalDate.of(2018, 1, 9)
                    ),
                    generatePeriode(
                        fom = LocalDate.of(2018, 1, 9),
                        tom = LocalDate.of(2018, 1, 10)
                    ),
                    generatePeriode(
                        fom = LocalDate.of(2018, 1, 15),
                        tom = LocalDate.of(2018, 1, 20)
                    )
                )
            )

            PeriodLogicRuleChain(sykemelding, ruleData()).getRuleByName("OPPHOLD_MELLOM_PERIODER").executeRule().result shouldBeEqualTo true
        }

        it("Should check rule OPPHOLD_MELLOM_PERIODER, should NOT trigger rule") {
            val sykemelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.of(2018, 1, 1),
                        tom = LocalDate.of(2018, 2, 1)
                    ),

                    generatePeriode(
                        fom = LocalDate.of(2018, 2, 1),
                        tom = LocalDate.of(2018, 5, 1)
                    )
                )
            )

            PeriodLogicRuleChain(sykemelding, ruleData()).getRuleByName("OPPHOLD_MELLOM_PERIODER").executeRule().result shouldBeEqualTo false
        }

        it("Should check rule TILBAKEDATERT_MER_ENN_3_AR, should trigger rule") {
            val healthInformation = generateSykemelding(
                perioder = listOf(
                    Periode(
                        fom = LocalDate.now().minusYears(3).minusDays(14),
                        tom = LocalDate.now().minusYears(3),
                        aktivitetIkkeMulig = null,
                        avventendeInnspillTilArbeidsgiver = null,
                        behandlingsdager = 1,
                        gradert = null,
                        reisetilskudd = false
                    )
                ),
                tidspunkt = LocalDateTime.now()
            )

            PeriodLogicRuleChain(healthInformation, ruleData()).getRuleByName("TILBAKEDATERT_MER_ENN_3_AR").executeRule().result shouldBeEqualTo true
        }

        it("Should check rule TILBAKEDATERT_MER_ENN_3_AR, should not trigger rule") {
            val healthInformation = generateSykemelding(
                perioder = listOf(
                    Periode(
                        fom = LocalDate.now().minusDays(14),
                        tom = LocalDate.now(),
                        aktivitetIkkeMulig = null,
                        avventendeInnspillTilArbeidsgiver = null,
                        behandlingsdager = 1,
                        gradert = null,
                        reisetilskudd = false
                    )
                ),
                tidspunkt = LocalDateTime.now()
            )

            PeriodLogicRuleChain(healthInformation, ruleData()).getRuleByName("TILBAKEDATERT_MER_ENN_3_AR").executeRule().result shouldBeEqualTo false
        }

        it("Should check rule FREMDATERT, should trigger rule") {
            val sykemelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now().plusDays(31),
                        tom = LocalDate.now().plusDays(37)
                    )
                )
            )

            PeriodLogicRuleChain(sykemelding, ruleData()).getRuleByName("FREMDATERT").executeRule().result shouldBeEqualTo true
        }

        it("Should check rule FREMDATERT, should NOT trigger rule") {
            val sykemelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now().plusDays(29),
                        tom = LocalDate.now().plusDays(31)
                    )
                )
            )

            PeriodLogicRuleChain(sykemelding, ruleData()).getRuleByName("FREMDATERT").executeRule().result shouldBeEqualTo false
        }

        it("Should check rule VARIGHET_OVER_ETT_AAR, should trigger rule") {
            val sykemelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now(),
                        tom = LocalDate.now().plusDays(366)
                    )
                ),
                tidspunkt = LocalDateTime.now().minusDays(1)
            )

            PeriodLogicRuleChain(sykemelding, ruleData()).getRuleByName("TOTAL_VARIGHET_OVER_ETT_AAR").executeRule().result shouldBeEqualTo true
        }

        it("Should check rule VARIGHET_OVER_ETT_AAR, should NOT trigger rule") {
            val sykemelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now(),
                        tom = LocalDate.now().plusYears(1)
                    )
                ),
                tidspunkt = LocalDateTime.now()
            )

            PeriodLogicRuleChain(sykemelding, ruleData()).getRuleByName("TOTAL_VARIGHET_OVER_ETT_AAR").executeRule().result shouldBeEqualTo false
        }

        it("Should check rule AVVENTENDE_SYKMELDING_KOMBINERT, should trigger rule") {
            val sykemelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now(),
                        tom = LocalDate.now().plusDays(5),
                        avventendeInnspillTilArbeidsgiver = "Bør gå minst mulig på jobb"
                    ),

                    generatePeriode(
                        fom = LocalDate.now().plusDays(5),
                        tom = LocalDate.now().plusDays(10)
                    )
                )
            )

            PeriodLogicRuleChain(sykemelding, ruleData()).getRuleByName("AVVENTENDE_SYKMELDING_KOMBINERT").executeRule().result shouldBeEqualTo true
        }

        it("Should check rule AVVENTENDE_SYKMELDING_KOMBINERT, should NOT trigger rule") {
            val sykemelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now(),
                        tom = LocalDate.now().plusDays(5)
                    )
                )
            )

            PeriodLogicRuleChain(sykemelding, ruleData()).getRuleByName("AVVENTENDE_SYKMELDING_KOMBINERT").executeRule().result shouldBeEqualTo false
        }

        it("Should check rule MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER, should trigger rule") {
            val sykemelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now(),
                        tom = LocalDate.now().plusDays(5),
                        avventendeInnspillTilArbeidsgiver = "      "
                    )
                )
            )

            PeriodLogicRuleChain(sykemelding, ruleData()).getRuleByName("MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER").executeRule().result shouldBeEqualTo true
        }

        it("Should check rule MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER, should NOT trigger rule") {
            val sykemelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now(),
                        tom = LocalDate.now().plusDays(5),
                        avventendeInnspillTilArbeidsgiver = "Bør gå minst mulig på jobb"
                    )
                )
            )

            PeriodLogicRuleChain(sykemelding, ruleData()).getRuleByName("MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER").executeRule().result shouldBeEqualTo false
        }

        it("Should check rule AVVENTENDE_SYKMELDING_OVER_16_DAGER, should trigger rule") {
            val sykemelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now(),
                        tom = LocalDate.now().plusDays(17),
                        avventendeInnspillTilArbeidsgiver = "Bør gå minst mulig på jobb"
                    )
                )
            )

            PeriodLogicRuleChain(sykemelding, ruleData()).getRuleByName("AVVENTENDE_SYKMELDING_OVER_16_DAGER").executeRule().result shouldBeEqualTo true
        }

        it("Should check rule AVVENTENDE_SYKMELDING_OVER_16_DAGER, should NOT trigger rule") {
            val sykemelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now(),
                        tom = LocalDate.now().plusDays(16),
                        avventendeInnspillTilArbeidsgiver = "Bør gå minst mulig på jobb"
                    )
                )
            )

            PeriodLogicRuleChain(sykemelding, ruleData()).getRuleByName("AVVENTENDE_SYKMELDING_OVER_16_DAGER").executeRule().result shouldBeEqualTo false
        }

        it("Should check rule FOR_MANGE_BEHANDLINGSDAGER_PER_UKE, should trigger rule") {
            val sykemelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.of(2019, 1, 7),
                        tom = LocalDate.of(2019, 1, 13),
                        behandlingsdager = 2
                    )
                )
            )

            PeriodLogicRuleChain(sykemelding, ruleData()).getRuleByName("FOR_MANGE_BEHANDLINGSDAGER_PER_UKE").executeRule().result shouldBeEqualTo true
        }

        it("Should check rule FOR_MANGE_BEHANDLINGSDAGER_PER_UKE, should NOT trigger rule") {
            val sykemelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.of(2019, 1, 7),
                        tom = LocalDate.of(2019, 1, 13),
                        behandlingsdager = 1
                    )
                )
            )

            PeriodLogicRuleChain(sykemelding, ruleData()).getRuleByName("FOR_MANGE_BEHANDLINGSDAGER_PER_UKE").executeRule().result shouldBeEqualTo false
        }

        it("Should check rule GRADERT_SYKMELDING_UNDER_20_PROSENT, should trigger rule") {
            val sykemelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now(),
                        tom = LocalDate.now(),
                        gradert = generateGradert(grad = 19)
                    )
                )
            )

            PeriodLogicRuleChain(sykemelding, ruleData()).getRuleByName("GRADERT_SYKMELDING_UNDER_20_PROSENT").executeRule().result shouldBeEqualTo true
        }

        it("Should check rule GRADERT_SYKMELDING_UNDER_20_PROSENT, should NOT trigger rule") {
            val sykemelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now(),
                        tom = LocalDate.now(),
                        gradert = generateGradert(grad = 20)
                    )
                )
            )

            PeriodLogicRuleChain(sykemelding, ruleData()).getRuleByName("GRADERT_SYKMELDING_UNDER_20_PROSENT").executeRule().result shouldBeEqualTo false
        }

        it("Should check rule GRADERT_SYKMELDING_OVER_99_PROSENT, should trigger rule") {
            val sykemelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now(),
                        tom = LocalDate.now(),
                        gradert = generateGradert(grad = 100)
                    )
                )
            )

            PeriodLogicRuleChain(sykemelding, ruleData()).getRuleByName("GRADERT_SYKMELDING_OVER_99_PROSENT").executeRule().result shouldBeEqualTo true
        }

        it("Should check rule GRADERT_SYKMELDING_OVER_99_PROSENT, should NOT trigger rule") {
            val sykemelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now(),
                        tom = LocalDate.now(),
                        gradert = generateGradert(grad = 99)
                    )
                )
            )

            PeriodLogicRuleChain(sykemelding, ruleData()).getRuleByName("GRADERT_SYKMELDING_OVER_99_PROSENT").executeRule().result shouldBeEqualTo false
        }
    }
})
