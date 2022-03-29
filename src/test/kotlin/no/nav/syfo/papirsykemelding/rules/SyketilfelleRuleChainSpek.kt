package no.nav.syfo.papirsykemelding.rules

import io.kotest.core.spec.style.FunSpec
import no.nav.syfo.generateKontaktMedPasient
import no.nav.syfo.generatePeriode
import no.nav.syfo.generateSykemelding
import no.nav.syfo.model.KontaktMedPasient
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import org.amshove.kluent.shouldBeEqualTo
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class SyketilfelleRuleChainSpek : FunSpec({

    context("Testing validation rules and checking the rule outcomes") {

        test("Should check rule TILBAKEDATERT_INNTIL_8_DAGER_UTEN_KONTAKTDATO_OG_BEGRUNNELSE, should trigger rule") {
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
                        tssid = "1355435",
                        pasientFodselsdato = LocalDate.of(1980, 1, 1)
                    ),
                    erNyttSyketilfelle = true
                )

            SyketilfelleRuleChain(healthInformation, ruleMetadataAndForstegangsSykemelding)
                .getRuleByName("TILBAKEDATERT_INNTIL_8_DAGER_UTEN_KONTAKTDATO_OG_BEGRUNNELSE")
                .executeRule().result shouldBeEqualTo true
        }

        test("Should check rule TILBAKEDATERT_INNTIL_8_DAGER_UTEN_KONTAKTDATO, should NOT trigger rule") {
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
                        behandletTidspunkt = LocalDateTime.of(LocalDate.of(2019, 1, 8), LocalTime.NOON),
                        patientPersonNumber = "1232345244",
                        rulesetVersion = "2",
                        legekontorOrgnr = "12313",
                        tssid = "1355435",
                        pasientFodselsdato = LocalDate.of(1980, 1, 1)
                    ),
                    erNyttSyketilfelle = false
                )

            SyketilfelleRuleChain(healthInformation, ruleMetadataAndForstegangsSykemelding).getRuleByName("TILBAKEDATERT_INNTIL_8_DAGER_UTEN_KONTAKTDATO_OG_BEGRUNNELSE")
                .executeRule()
                .result shouldBeEqualTo false
        }

        test("Should check rule TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING, should trigger rule") {
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
                        tssid = "1355435",
                        pasientFodselsdato = LocalDate.of(1980, 1, 1)
                    ),
                    erNyttSyketilfelle = true
                )

            SyketilfelleRuleChain(healthInformation, ruleMetadataAndForstegangsSykemelding).getRuleByName("TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING")
                .executeRule().result shouldBeEqualTo true
        }

        test("Should check rule TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING, should NOT trigger rule") {
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
                        behandletTidspunkt = LocalDateTime.of(LocalDate.of(2019, 1, 8), LocalTime.NOON),
                        patientPersonNumber = "1232345244",
                        rulesetVersion = "2",
                        legekontorOrgnr = "12313",
                        tssid = "1355435",
                        pasientFodselsdato = LocalDate.of(1980, 1, 1)
                    ),
                    erNyttSyketilfelle = false
                )

            SyketilfelleRuleChain(healthInformation, ruleMetadataAndForstegangsSykemelding).getRuleByName("TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING")
                .executeRule().result shouldBeEqualTo false
        }

        test("Should check rule TILBAKEDATERT_FORLENGELSE_OVER_1_MND, should trigger rule") {
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
                        tssid = "1355435",
                        pasientFodselsdato = LocalDate.of(1980, 1, 1)
                    ),
                    erNyttSyketilfelle = false
                )

            SyketilfelleRuleChain(healthInformation, ruleMetadataAndForstegangsSykemelding).getRuleByName("TILBAKEDATERT_FORLENGELSE_OVER_1_MND")
                .executeRule().result shouldBeEqualTo true
        }

        test("Should check rule TILBAKEDATERT_FORLENGELSE_OVER_1_MND, should NOT trigger rule") {
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
                        tssid = "1355435",
                        pasientFodselsdato = LocalDate.of(1980, 1, 1)
                    ),
                    erNyttSyketilfelle = false
                )

            SyketilfelleRuleChain(healthInformation, ruleMetadataAndForstegangsSykemelding).getRuleByName("TILBAKEDATERT_FORLENGELSE_OVER_1_MND")
                .executeRule().result shouldBeEqualTo false
        }

        test("Should check rule TILBAKEDATERT_FORLENGELSE_OVER_1_MND, should NOT trigger rule") {
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
                        behandletTidspunkt = LocalDateTime.now().minusMonths(2),
                        patientPersonNumber = "1232345244",
                        rulesetVersion = "2",
                        legekontorOrgnr = "12313",
                        tssid = "1355435",
                        pasientFodselsdato = LocalDate.of(1980, 1, 1)
                    ),
                    erNyttSyketilfelle = false
                )

            SyketilfelleRuleChain(healthInformation, ruleMetadataAndForstegangsSykemelding).getRuleByName("TILBAKEDATERT_FORLENGELSE_OVER_1_MND")
                .executeRule().result shouldBeEqualTo false
        }

        test("Should check rule TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING_MED_BEGRUNNELSE, should trigger rule") {
            val healthInformation = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now().minusDays(9),
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
                        tssid = "1355435",
                        pasientFodselsdato = LocalDate.of(1980, 1, 1)
                    ),
                    erNyttSyketilfelle = true
                )

            SyketilfelleRuleChain(healthInformation, ruleMetadataAndForstegangsSykemelding)
                .getRuleByName("TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING_MED_BEGRUNNELSE")
                .executeRule().result shouldBeEqualTo true
        }

        test("Should check rule TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING_MED_BEGRUNNELSE, should NOT trigger rule") {
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
                        tssid = "1355435",
                        pasientFodselsdato = LocalDate.of(1980, 1, 1)
                    ),
                    erNyttSyketilfelle = true
                )

            SyketilfelleRuleChain(healthInformation, ruleMetadataAndForstegangsSykemelding)
                .getRuleByName("TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING_MED_BEGRUNNELSE")
                .executeRule().result shouldBeEqualTo false
        }

        test("Should check rule TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE, should trigger rule") {
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
                        behandletTidspunkt = LocalDateTime.now().plusDays(30),
                        patientPersonNumber = "1232345244",
                        rulesetVersion = "2",
                        legekontorOrgnr = "12313",
                        tssid = "1355435",
                        pasientFodselsdato = LocalDate.of(1980, 1, 1)
                    ),
                    erNyttSyketilfelle = false
                )

            SyketilfelleRuleChain(healthInformation, ruleMetadataAndForstegangsSykemelding)
                .getRuleByName("TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE")
                .executeRule().result shouldBeEqualTo true
        }

        test("Should check rule TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE, NOT should trigger rule") {
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
                        behandletTidspunkt = LocalDateTime.now().plusDays(29),
                        patientPersonNumber = "1232345244",
                        rulesetVersion = "2",
                        legekontorOrgnr = "12313",
                        tssid = "1355435",
                        pasientFodselsdato = LocalDate.of(1980, 1, 1)
                    ),
                    erNyttSyketilfelle = false
                )

            SyketilfelleRuleChain(healthInformation, ruleMetadataAndForstegangsSykemelding)
                .getRuleByName("TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE")
                .executeRule().result shouldBeEqualTo false
        }

        test("Should check rule TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE, NOT should trigger rule") {
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
                        behandletTidspunkt = LocalDateTime.now().plusDays(30),
                        patientPersonNumber = "1232345244",
                        rulesetVersion = "2",
                        legekontorOrgnr = "12313",
                        tssid = "1355435",
                        pasientFodselsdato = LocalDate.of(1980, 1, 1)
                    ),
                    erNyttSyketilfelle = false
                )

            SyketilfelleRuleChain(healthInformation, ruleMetadataAndForstegangsSykemelding)
                .getRuleByName("TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE")
                .executeRule().result shouldBeEqualTo false
        }
    }
})
