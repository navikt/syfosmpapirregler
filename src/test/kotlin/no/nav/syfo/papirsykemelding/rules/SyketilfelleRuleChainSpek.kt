package no.nav.syfo.papirsykemelding.rules

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import no.nav.syfo.generateKontaktMedPasient
import no.nav.syfo.generatePeriode
import no.nav.syfo.generateSykemelding
import no.nav.syfo.model.KontaktMedPasient
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.rules.RuleData
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object SyketilfelleRuleChainSpek : Spek({

    describe("Testing validation rules and checking the rule outcomes") {
        fun ruleData(
            healthInformation: Sykmelding,
            ruleMetadataAndForstegangsSykemelding: RuleMetadataAndForstegangsSykemelding
        ) =
            RuleData(healthInformation, ruleMetadataAndForstegangsSykemelding)

        it("Should check rule TILBAKEDATERT_INNTIL_8_DAGER_UTEN_KONTAKTDATO_OG_BEGRUNNELSE, should trigger rule") {
            val healthInformation = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.of(2019, 1, 3),
                        tom = LocalDate.of(2019, 1, 8)
                    )
                ),
                kontaktMedPasient = KontaktMedPasient(null, null)
            )

            val ruleMetadataAndForstegangsSykemelding =
                RuleMetadataAndForstegangsSykemelding(
                    ruleMetadata = RuleMetadata(
                        receivedDate = LocalDateTime.now(),
                        signatureDate = LocalDateTime.of(LocalDate.of(2019, 1, 8), LocalTime.NOON),
                        behandletTidspunkt = LocalDateTime.of(LocalDate.of(2019, 1, 8), LocalTime.NOON),
                        patientPersonNumber = "1232345244",
                        rulesetVersion = "2",
                        legekontorOrgnr = "12313",
                        tssid = "1355435"
                    ), erNyttSyketilfelle = true
                )

            SyketilfelleRuleChain.TILBAKEDATERT_INNTIL_8_DAGER_UTEN_KONTAKTDATO_OG_BEGRUNNELSE(
                ruleData(
                    healthInformation,
                    ruleMetadataAndForstegangsSykemelding
                )
            ) shouldEqual true
        }

        it("Should check rule TILBAKEDATERT_INNTIL_8_DAGER_UTEN_KONTAKTDATO, should NOT trigger rule") {
            val healthInformation = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.of(2019, 1, 7),
                        tom = LocalDate.of(2019, 1, 8)
                    )
                )
            )

            val ruleMetadataAndForstegangsSykemelding =
                RuleMetadataAndForstegangsSykemelding(
                    ruleMetadata = RuleMetadata(
                        receivedDate = LocalDateTime.now(),
                        signatureDate = LocalDateTime.of(LocalDate.of(2019, 1, 8), LocalTime.NOON),
                        behandletTidspunkt = LocalDateTime.now(),
                        patientPersonNumber = "1232345244",
                        rulesetVersion = "2",
                        legekontorOrgnr = "12313",
                        tssid = "1355435"
                    ), erNyttSyketilfelle = false
                )

            SyketilfelleRuleChain.TILBAKEDATERT_INNTIL_8_DAGER_UTEN_KONTAKTDATO_OG_BEGRUNNELSE(
                ruleData(
                    healthInformation,
                    ruleMetadataAndForstegangsSykemelding
                )
            ) shouldEqual false
        }

        it("Should check rule TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING, should trigger rule") {
            val healthInformation = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.of(2019, 1, 10),
                        tom = LocalDate.of(2019, 1, 20)
                    )
                ),
                kontaktMedPasient = KontaktMedPasient(null, null)
            )

            val ruleMetadataAndForstegangsSykemelding =
                RuleMetadataAndForstegangsSykemelding(
                    ruleMetadata = RuleMetadata(
                        receivedDate = LocalDateTime.now(),
                        signatureDate = LocalDateTime.of(LocalDate.of(2019, 1, 19), LocalTime.NOON),
                        behandletTidspunkt = LocalDateTime.of(LocalDate.of(2019, 1, 19), LocalTime.NOON),
                        patientPersonNumber = "1232345244",
                        rulesetVersion = "2",
                        legekontorOrgnr = "12313",
                        tssid = "1355435"
                    ), erNyttSyketilfelle = true
                )

            SyketilfelleRuleChain.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING(
                ruleData(
                    healthInformation,
                    ruleMetadataAndForstegangsSykemelding
                )
            ) shouldEqual true
        }

        it("Should check rule TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING, should NOT trigger rule") {
            val healthInformation = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.of(2019, 1, 7),
                        tom = LocalDate.of(2019, 1, 8)
                    )
                )
            )

            val ruleMetadataAndForstegangsSykemelding =
                RuleMetadataAndForstegangsSykemelding(
                    ruleMetadata = RuleMetadata(
                        receivedDate = LocalDateTime.now(),
                        signatureDate = LocalDateTime.of(LocalDate.of(2019, 1, 8), LocalTime.NOON),
                        behandletTidspunkt = LocalDateTime.now(),
                        patientPersonNumber = "1232345244",
                        rulesetVersion = "2",
                        legekontorOrgnr = "12313",
                        tssid = "1355435"
                    ), erNyttSyketilfelle = false
                )

            SyketilfelleRuleChain.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING(
                ruleData(
                    healthInformation,
                    ruleMetadataAndForstegangsSykemelding
                )
            ) shouldEqual false
        }

        it("Should check rule TILBAKEDATERT_FORLENGELSE_OVER_1_MND, should trigger rule") {
            val healthInformation = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.of(2019, 10, 10),
                        tom = LocalDate.of(2019, 10, 20)
                    )
                )
            )

            val ruleMetadataAndForstegangsSykemelding =
                RuleMetadataAndForstegangsSykemelding(
                    RuleMetadata(
                        receivedDate = LocalDateTime.now(),
                        signatureDate = LocalDateTime.now(),
                        behandletTidspunkt = LocalDateTime.of(LocalDate.of(2019, 11, 11), LocalTime.NOON),
                        patientPersonNumber = "1232345244",
                        rulesetVersion = "2",
                        legekontorOrgnr = "12313",
                        tssid = "1355435"
                    ), erNyttSyketilfelle = false
                )

            SyketilfelleRuleChain.TILBAKEDATERT_FORLENGELSE_OVER_1_MND(
                ruleData(
                    healthInformation,
                    ruleMetadataAndForstegangsSykemelding
                )
            ) shouldEqual true
        }

        it("Should check rule TILBAKEDATERT_FORLENGELSE_OVER_1_MND, should NOT trigger rule") {
            val healthInformation = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now(),
                        tom = LocalDate.now()
                    )
                )
            )

            val ruleMetadataAndForstegangsSykemelding =
                RuleMetadataAndForstegangsSykemelding(
                    ruleMetadata = RuleMetadata(
                        receivedDate = LocalDateTime.now(),
                        signatureDate = LocalDateTime.now().minusMonths(1),
                        behandletTidspunkt = LocalDateTime.now().minusMonths(1),
                        patientPersonNumber = "1232345244",
                        rulesetVersion = "2",
                        legekontorOrgnr = "12313",
                        tssid = "1355435"
                    ), erNyttSyketilfelle = false
                )

            SyketilfelleRuleChain.TILBAKEDATERT_FORLENGELSE_OVER_1_MND(
                ruleData(
                    healthInformation,
                    ruleMetadataAndForstegangsSykemelding
                )
            ) shouldEqual false
        }

        it("Should check rule TILBAKEDATERT_FORLENGELSE_OVER_1_MND, should NOT trigger rule") {
            val healthInformation = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now(),
                        tom = LocalDate.now()
                    )
                ),
                kontaktMedPasient = generateKontaktMedPasient(
                    begrunnelseIkkeKontakt = "Noe tull skjedde, med sykmeldingen"
                ),
                signaturDateTime = LocalDateTime.now(),
                tidspunkt = LocalDateTime.now()
            )

            val ruleMetadataAndForstegangsSykemelding =
                RuleMetadataAndForstegangsSykemelding(
                    ruleMetadata = RuleMetadata(
                        receivedDate = LocalDateTime.now(),
                        signatureDate = LocalDateTime.now().minusMonths(2),
                        behandletTidspunkt = LocalDateTime.now(),
                        patientPersonNumber = "1232345244",
                        rulesetVersion = "2",
                        legekontorOrgnr = "12313",
                        tssid = "1355435"
                    ), erNyttSyketilfelle = false
                )

            SyketilfelleRuleChain.TILBAKEDATERT_FORLENGELSE_OVER_1_MND(
                ruleData(
                    healthInformation,
                    ruleMetadataAndForstegangsSykemelding
                )
            ) shouldEqual false
        }

        it("Should check rule TILBAKEDATERT_MED_BEGRUNNELSE_FORSTE_SYKMELDING, should trigger rule") {
            val healthInformation = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now().minusDays(8),
                        tom = LocalDate.now()
                    )
                ),
                kontaktMedPasient = generateKontaktMedPasient(
                    begrunnelseIkkeKontakt = "Noe tull skjedde, med sykmeldingen"
                )
            )

            val ruleMetadataAndForstegangsSykemelding =
                RuleMetadataAndForstegangsSykemelding(
                    ruleMetadata = RuleMetadata(
                        receivedDate = LocalDateTime.now(),
                        signatureDate = LocalDateTime.now(),
                        behandletTidspunkt = LocalDateTime.now(),
                        patientPersonNumber = "1232345244",
                        rulesetVersion = "2",
                        legekontorOrgnr = "12313",
                        tssid = "1355435"
                    ), erNyttSyketilfelle = true
                )

            SyketilfelleRuleChain.TILBAKEDATERT_MED_BEGRUNNELSE_FORSTE_SYKMELDING(
                ruleData(
                    healthInformation,
                    ruleMetadataAndForstegangsSykemelding
                )
            ) shouldEqual true
        }

        it("Should check rule TILBAKEDATERT_MED_BEGRUNNELSE_FORSTE_SYKMELDING, should NOT trigger rule") {
            val healthInformation = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now().minusDays(7),
                        tom = LocalDate.now()
                    )
                ),
                kontaktMedPasient = generateKontaktMedPasient(
                    begrunnelseIkkeKontakt = "Noe tull skjedde, med sykmeldingen"
                )
            )

            val ruleMetadataAndForstegangsSykemelding =
                RuleMetadataAndForstegangsSykemelding(
                    ruleMetadata = RuleMetadata(
                        receivedDate = LocalDateTime.now(),
                        signatureDate = LocalDateTime.now(),
                        behandletTidspunkt = LocalDateTime.now(),
                        patientPersonNumber = "1232345244",
                        rulesetVersion = "2",
                        legekontorOrgnr = "12313",
                        tssid = "1355435"
                    ), erNyttSyketilfelle = true
                )

            SyketilfelleRuleChain.TILBAKEDATERT_MED_BEGRUNNELSE_FORSTE_SYKMELDING(
                ruleData(
                    healthInformation,
                    ruleMetadataAndForstegangsSykemelding
                )
            ) shouldEqual false
        }

        it("Should check rule TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE, should trigger rule") {
            val healthInformation = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now(),
                        tom = LocalDate.now()
                    )
                ),
                kontaktMedPasient = generateKontaktMedPasient(
                    begrunnelseIkkeKontakt = "Noe tull skjedde, med sykmeldingen"
                )
            )

            val ruleMetadataAndForstegangsSykemelding =
                RuleMetadataAndForstegangsSykemelding(
                    ruleMetadata = RuleMetadata(
                        receivedDate = LocalDateTime.now(),
                        signatureDate = LocalDateTime.now().plusDays(30),
                        behandletTidspunkt = LocalDateTime.now(),
                        patientPersonNumber = "1232345244",
                        rulesetVersion = "2",
                        legekontorOrgnr = "12313",
                        tssid = "1355435"
                    ), erNyttSyketilfelle = false
                )

            SyketilfelleRuleChain.TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE(
                ruleData(
                    healthInformation,
                    ruleMetadataAndForstegangsSykemelding
                )
            ) shouldEqual true
        }

        it("Should check rule TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE, NOT should trigger rule") {
            val healthInformation = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now(),
                        tom = LocalDate.now()
                    )
                ),
                kontaktMedPasient = generateKontaktMedPasient(
                    begrunnelseIkkeKontakt = "Noe tull skjedde, med sykmeldingen"
                )
            )

            val ruleMetadataAndForstegangsSykemelding =
                RuleMetadataAndForstegangsSykemelding(
                    ruleMetadata = RuleMetadata(
                        receivedDate = LocalDateTime.now(),
                        signatureDate = LocalDateTime.now().plusDays(29),
                        behandletTidspunkt = LocalDateTime.now(),
                        patientPersonNumber = "1232345244",
                        rulesetVersion = "2",
                        legekontorOrgnr = "12313",
                        tssid = "1355435"
                    ), erNyttSyketilfelle = false
                )

            SyketilfelleRuleChain.TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE(
                ruleData(
                    healthInformation,
                    ruleMetadataAndForstegangsSykemelding
                )
            ) shouldEqual false
        }

        it("Should check rule TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE, NOT should trigger rule") {
            val healthInformation = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now(),
                        tom = LocalDate.now()
                    )
                )
            )

            val ruleMetadataAndForstegangsSykemelding =
                RuleMetadataAndForstegangsSykemelding(
                    ruleMetadata = RuleMetadata(
                        receivedDate = LocalDateTime.now(),
                        signatureDate = LocalDateTime.now().plusDays(30),
                        behandletTidspunkt = LocalDateTime.now(),
                        patientPersonNumber = "1232345244",
                        rulesetVersion = "2",
                        legekontorOrgnr = "12313",
                        tssid = "1355435"
                    ), erNyttSyketilfelle = false
                )

            SyketilfelleRuleChain.TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE(
                ruleData(
                    healthInformation,
                    ruleMetadataAndForstegangsSykemelding
                )
            ) shouldEqual false
        }
    }
})
