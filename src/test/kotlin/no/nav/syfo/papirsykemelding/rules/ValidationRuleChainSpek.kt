package no.nav.syfo.papirsykemelding.rules

import no.nav.syfo.behandletTidspunkt
import no.nav.syfo.generateArbeidsgiver
import no.nav.syfo.generateBehandler
import no.nav.syfo.generateKontaktMedPasient
import no.nav.syfo.generatePerioder
import no.nav.syfo.generatePrognose
import no.nav.syfo.generateSykemelding
import no.nav.syfo.model.AvsenderSystem
import no.nav.syfo.model.Diagnose
import no.nav.syfo.model.MedisinskVurdering
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.signaturDato
import no.nav.syfo.sm.Diagnosekoder
import no.nav.syfo.toDiagnose
import no.nav.syfo.validering.extractBornYear
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.LocalDate
import java.time.LocalDateTime

object ValidationRuleChainSpek : Spek({
    fun ruleData(
        receivedDate: LocalDateTime = LocalDateTime.now(),
        signatureDate: LocalDateTime = LocalDateTime.now(),
        patientPersonNumber: String = "1234567891",
        rulesetVersion: String = "1",
        legekontorOrgNr: String = "123456789",
        tssid: String? = "1314445",
        pasientFodselsdato: LocalDate = LocalDate.of(1980, 1, 1)
    ): RuleMetadata =
        RuleMetadata(
            signatureDate,
            receivedDate,
            LocalDateTime.now(),
            patientPersonNumber,
            rulesetVersion,
            legekontorOrgNr,
            tssid,
            pasientFodselsdato
        )

    describe("Testing validation rules and checking the rule outcomes") {

        it("Should check rule PASIENT_YNGRE_ENN_13,should trigger rule") {
            ValidationRuleChain(
                generateSykemelding(),
                ruleData(
                    pasientFodselsdato = LocalDate.now().minusYears(12)
                )
            ).getRuleByName("PASIENT_YNGRE_ENN_13").executeRule().result shouldBeEqualTo true
        }

        it("Should check rule PASIENT_YNGRE_ENN_13,should NOT trigger rule") {
            ValidationRuleChain(
                generateSykemelding(generatePerioder()),
                ruleData(
                    pasientFodselsdato = LocalDate.now().minusYears(40)
                )
            ).getRuleByName("PASIENT_YNGRE_ENN_13").executeRule().result shouldBeEqualTo false
        }

        it("Should check rule PASIENT_ELDRE_ENN_70,should trigger rule") {
            ValidationRuleChain(
                generateSykemelding(generatePerioder()),
                ruleData(
                    pasientFodselsdato = LocalDate.now().minusYears(72)
                )
            ).getRuleByName("PASIENT_ELDRE_ENN_70").executeRule().result shouldBeEqualTo true
        }

        it("Should check rule PASIENT_ELDRE_ENN_70,should NOT trigger rule") {
            ValidationRuleChain(
                generateSykemelding(generatePerioder()),
                ruleData(
                    pasientFodselsdato = LocalDate.now().minusYears(68)
                )
            ).getRuleByName("PASIENT_ELDRE_ENN_70").executeRule().result shouldBeEqualTo false
        }

        it("Skal håndtere fødselsnummer fra 1854-1899") {
            val beregnetFodselsar1 = extractBornYear("01015450000")
            val beregnetFodselsar2 = extractBornYear("01015474900")
            val beregnetFodselsar3 = extractBornYear("01019950000")
            val beregnetFodselsar4 = extractBornYear("01019974900")

            beregnetFodselsar1 shouldBeEqualTo 1854
            beregnetFodselsar2 shouldBeEqualTo 1854
            beregnetFodselsar3 shouldBeEqualTo 1899
            beregnetFodselsar4 shouldBeEqualTo 1899
        }

        it("Skal håndtere fødselsnummer fra 1900-1999") {
            val beregnetFodselsar1 = extractBornYear("01010000000")
            val beregnetFodselsar2 = extractBornYear("01010049900")
            val beregnetFodselsar3 = extractBornYear("01019900000")
            val beregnetFodselsar4 = extractBornYear("01019949900")

            beregnetFodselsar1 shouldBeEqualTo 1900
            beregnetFodselsar2 shouldBeEqualTo 1900
            beregnetFodselsar3 shouldBeEqualTo 1999
            beregnetFodselsar4 shouldBeEqualTo 1999
        }

        it("Skal håndtere fødselsnummer fra 1940-1999") {
            val beregnetFodselsar1 = extractBornYear("01014090000")
            val beregnetFodselsar2 = extractBornYear("01014099900")
            val beregnetFodselsar3 = extractBornYear("01019990000")
            val beregnetFodselsar4 = extractBornYear("01019999900")

            beregnetFodselsar1 shouldBeEqualTo 1940
            beregnetFodselsar2 shouldBeEqualTo 1940
            beregnetFodselsar3 shouldBeEqualTo 1999
            beregnetFodselsar4 shouldBeEqualTo 1999
        }

        it("Skal håndtere fødselsnummer fra 2000-2039") {
            val beregnetFodselsar1 = extractBornYear("01010050000")
            val beregnetFodselsar2 = extractBornYear("01010099900")
            val beregnetFodselsar3 = extractBornYear("01013950000")
            val beregnetFodselsar4 = extractBornYear("01013999900")

            beregnetFodselsar1 shouldBeEqualTo 2000
            beregnetFodselsar2 shouldBeEqualTo 2000
            beregnetFodselsar3 shouldBeEqualTo 2039
            beregnetFodselsar4 shouldBeEqualTo 2039
        }

        it("Should check rule ICPC_2_Z_DIAGNOSE,should trigger rule") {
            val sykmelding = Sykmelding(
                "1",
                "1",
                "2",
                MedisinskVurdering(
                    hovedDiagnose = Diagnosekoder.icpc2["Z09"]!!.toDiagnose(),
                    biDiagnoser = emptyList(),
                    svangerskap = false,
                    yrkesskadeDato = null,
                    annenFraversArsak = null,
                    yrkesskade = false
                ),
                false,
                generateArbeidsgiver(),
                generatePerioder(),
                generatePrognose(),
                emptyMap(),
                null,
                null,
                null,
                null,
                null,
                generateKontaktMedPasient(),
                behandletTidspunkt,
                generateBehandler(),
                AvsenderSystem("test", "1"),
                null,
                signaturDato,
                null
            )

            ValidationRuleChain(sykmelding, ruleData()).getRuleByName("ICPC_2_Z_DIAGNOSE")
                .executeRule().result shouldBeEqualTo true
        }

        it("Should check rule ICPC_2_Z_DIAGNOSE,should NOT trigger rule") {
            val sykmelding = Sykmelding(
                "1",
                "1",
                "2",
                MedisinskVurdering(
                    hovedDiagnose = Diagnosekoder.icpc2["A09"]!!.toDiagnose(),
                    biDiagnoser = emptyList(),
                    svangerskap = false,
                    yrkesskadeDato = null,
                    annenFraversArsak = null,
                    yrkesskade = false
                ),
                false,
                generateArbeidsgiver(),
                generatePerioder(),
                generatePrognose(),
                emptyMap(),
                null,
                null,
                null,
                null,
                null,
                generateKontaktMedPasient(),
                behandletTidspunkt,
                generateBehandler(),
                AvsenderSystem("test", "1"),
                null,
                signaturDato,
                null
            )
            ValidationRuleChain(sykmelding, ruleData()).getRuleByName("ICPC_2_Z_DIAGNOSE")
                .executeRule().result shouldBeEqualTo false
        }

        it("Should check rule ICPC_2_Z_DIAGNOSE,should NOT trigger rule") {
            val sykemelding = Sykmelding(
                "1",
                "1",
                "2",
                MedisinskVurdering(
                    hovedDiagnose = Diagnose(
                        system = "2.16.578.1.12.4.1.1.7170",
                        kode = "A62",
                        tekst = "vondt i hodet"
                    ),
                    biDiagnoser = emptyList(),
                    svangerskap = false,
                    yrkesskadeDato = null,
                    annenFraversArsak = null,
                    yrkesskade = false
                ),
                false,
                generateArbeidsgiver(),
                generatePerioder(),
                generatePrognose(),
                emptyMap(),
                null,
                null,
                null,
                null,
                null,
                generateKontaktMedPasient(),
                behandletTidspunkt,
                generateBehandler(),
                AvsenderSystem("test", "1"),
                null,
                signaturDato,
                null
            )

            ValidationRuleChain(sykemelding, ruleData()).getRuleByName("ICPC_2_Z_DIAGNOSE")
                .executeRule().result shouldBeEqualTo false
        }

        it("Should check rule HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER,should trigger rule") {
            val sykemelding = Sykmelding(
                "1",
                "1",
                "2",
                MedisinskVurdering(
                    hovedDiagnose = null,
                    biDiagnoser = emptyList(),
                    svangerskap = false,
                    yrkesskadeDato = null,
                    annenFraversArsak = null,
                    yrkesskade = false
                ),
                false,
                generateArbeidsgiver(),
                generatePerioder(),
                generatePrognose(),
                emptyMap(),
                null,
                null,
                null,
                null,
                null,
                generateKontaktMedPasient(),
                behandletTidspunkt,
                generateBehandler(),
                AvsenderSystem("test", "1"),
                null,
                signaturDato,
                null
            )

            ValidationRuleChain(sykemelding, ruleData())
                .getRuleByName("HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER")
                .executeRule().result shouldBeEqualTo true
        }

        it("Should check rule HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER,should NOT trigger rule") {
            val sykemelding = Sykmelding(
                "1",
                "1",
                "2",
                MedisinskVurdering(
                    hovedDiagnose = Diagnose(
                        system = "2.16.578.1.12.4.1.1.9999",
                        kode = "A09",
                        tekst = "Svetteproblemer"
                    ),
                    biDiagnoser = emptyList(),
                    svangerskap = false,
                    yrkesskadeDato = null,
                    annenFraversArsak = null,
                    yrkesskade = false
                ),
                false,
                generateArbeidsgiver(),
                generatePerioder(),
                generatePrognose(),
                emptyMap(),
                null,
                null,
                null,
                null,
                null,
                generateKontaktMedPasient(),
                behandletTidspunkt,
                generateBehandler(),
                AvsenderSystem("test", "1"),
                null,
                signaturDato,
                null
            )

            ValidationRuleChain(sykemelding, ruleData())
                .getRuleByName("HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER")
                .executeRule().result shouldBeEqualTo false
        }

        it("Should check rule UKJENT_DIAGNOSEKODETYPE,should trigger rule") {
            val sykemelding = Sykmelding(
                "1",
                "1",
                "2",
                MedisinskVurdering(
                    hovedDiagnose = Diagnose(
                        system = "2.16.578.1.12.4.1.1.9999",
                        kode = "A09",
                        tekst = "Svetteproblemer"
                    ),
                    biDiagnoser = emptyList(),
                    svangerskap = false,
                    yrkesskadeDato = null,
                    annenFraversArsak = null,
                    yrkesskade = false
                ),
                false,
                generateArbeidsgiver(),
                generatePerioder(),
                generatePrognose(),
                emptyMap(),
                null,
                null,
                null,
                null,
                null,
                generateKontaktMedPasient(),
                behandletTidspunkt,
                generateBehandler(),
                AvsenderSystem("test", "1"),
                null,
                signaturDato,
                null
            )

            ValidationRuleChain(sykemelding, ruleData())
                .getRuleByName("UKJENT_DIAGNOSEKODETYPE")
                .executeRule().result shouldBeEqualTo true
        }

        it("Should check rule UKJENT_DIAGNOSEKODETYPE,should NOT trigger rule") {
            val sykemelding = Sykmelding(
                "1",
                "1",
                "2",
                MedisinskVurdering(
                    hovedDiagnose = null,
                    biDiagnoser = emptyList(),
                    svangerskap = false,
                    yrkesskadeDato = null,
                    annenFraversArsak = null,
                    yrkesskade = false
                ),
                false,
                generateArbeidsgiver(),
                generatePerioder(),
                generatePrognose(),
                emptyMap(),
                null,
                null,
                null,
                null,
                null,
                generateKontaktMedPasient(),
                behandletTidspunkt,
                generateBehandler(),
                AvsenderSystem("test", "1"),
                null,
                signaturDato,
                null
            )

            ValidationRuleChain(sykemelding, ruleData())
                .getRuleByName("UKJENT_DIAGNOSEKODETYPE")
                .executeRule().result shouldBeEqualTo false
        }

        it("Should check rule UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE, wrong kodeverk for hoveddiagnose") {
            val sykemelding = Sykmelding(
                "1",
                "1",
                "2",
                MedisinskVurdering(
                    hovedDiagnose = Diagnose(
                        system = "2.16.578.1.12.4.1.1.7110",
                        kode = "Z09",
                        tekst = "Problem jus/poli"
                    ),
                    biDiagnoser = emptyList(),
                    svangerskap = false,
                    yrkesskadeDato = null,
                    annenFraversArsak = null,
                    yrkesskade = false
                ),
                false,
                generateArbeidsgiver(),
                generatePerioder(),
                generatePrognose(),
                emptyMap(),
                null,
                null,
                null,
                null,
                null,
                generateKontaktMedPasient(),
                behandletTidspunkt,
                generateBehandler(),
                AvsenderSystem("test", "1"),
                null,
                signaturDato,
                null
            )

            ValidationRuleChain(sykemelding, ruleData())
                .getRuleByName("UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE")
                .executeRule().result shouldBeEqualTo true
        }

        it("Should check rule UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE, null hovedDiagnose should not trigger") {
            val sykemelding = Sykmelding(
                "1",
                "1",
                "2",
                MedisinskVurdering(
                    hovedDiagnose = null,
                    biDiagnoser = emptyList(),
                    svangerskap = false,
                    yrkesskadeDato = null,
                    annenFraversArsak = null,
                    yrkesskade = false
                ),
                false,
                generateArbeidsgiver(),
                generatePerioder(),
                generatePrognose(),
                emptyMap(),
                null,
                null,
                null,
                null,
                null,
                generateKontaktMedPasient(),
                behandletTidspunkt,
                generateBehandler(),
                AvsenderSystem("test", "1"),
                null,
                signaturDato,
                null
            )

            ValidationRuleChain(sykemelding, ruleData())
                .getRuleByName("UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE")
                .executeRule().result shouldBeEqualTo false
        }

        it("Should check rule UGYLDIG_KODEVERK_FOR_BIDIAGNOSE, wrong kodeverk for biDiagnoser") {
            val sykemelding = Sykmelding(
                "1",
                "1",
                "2",
                MedisinskVurdering(
                    hovedDiagnose = null,
                    biDiagnoser = listOf(
                        Diagnose(
                            system = "2.16.578.1.12.4.1.1.7110",
                            kode = "Z09",
                            tekst = "Problem jus/poli"
                        )
                    ),
                    svangerskap = false,
                    yrkesskadeDato = null,
                    annenFraversArsak = null,
                    yrkesskade = false
                ),
                false,
                generateArbeidsgiver(),
                generatePerioder(),
                generatePrognose(),
                emptyMap(),
                null,
                null,
                null,
                null,
                null,
                generateKontaktMedPasient(),
                behandletTidspunkt,
                generateBehandler(),
                AvsenderSystem("test", "1"),
                null,
                signaturDato,
                null
            )

            ValidationRuleChain(sykemelding, ruleData())
                .getRuleByName("UGYLDIG_KODEVERK_FOR_BIDIAGNOSE")
                .executeRule().result shouldBeEqualTo true
        }

        it("Should check rule UGYLDIG_KODEVERK_FOR_BIDIAGNOSE, correct kodeverk for biDiagnoser") {
            val sykemelding = Sykmelding(
                "1",
                "1",
                "2",
                MedisinskVurdering(
                    hovedDiagnose = null,
                    biDiagnoser = listOf(
                        Diagnose(
                            system = "2.16.578.1.12.4.1.1.7170",
                            kode = "L92",
                            tekst = "Skuldersyndrom"
                        )
                    ),
                    svangerskap = false,
                    yrkesskadeDato = null,
                    annenFraversArsak = null,
                    yrkesskade = false
                ),
                false,
                generateArbeidsgiver(),
                generatePerioder(),
                generatePrognose(),
                emptyMap(),
                null,
                null,
                null,
                null,
                null,
                generateKontaktMedPasient(),
                behandletTidspunkt,
                generateBehandler(),
                AvsenderSystem("test", "1"),
                null,
                signaturDato,
                null
            )

            ValidationRuleChain(sykemelding, ruleData())
                .getRuleByName("UGYLDIG_KODEVERK_FOR_BIDIAGNOSE")
                .executeRule().result shouldBeEqualTo false
        }

        it("UGYLDIG_ORGNR_LENGDE should trigger on when orgnr lengt is not 9") {
            val sykemelding = Sykmelding(
                "1",
                "1",
                "2",
                MedisinskVurdering(
                    hovedDiagnose = null,
                    biDiagnoser = emptyList(),
                    svangerskap = false,
                    yrkesskadeDato = null,
                    annenFraversArsak = null,
                    yrkesskade = false
                ),
                false,
                generateArbeidsgiver(),
                generatePerioder(),
                generatePrognose(),
                emptyMap(),
                null,
                null,
                null,
                null,
                null,
                generateKontaktMedPasient(),
                behandletTidspunkt,
                generateBehandler(),
                AvsenderSystem("test", "1"),
                null,
                signaturDato,
                null
            )
            ValidationRuleChain(sykemelding, ruleData(legekontorOrgNr = "1234567890"))
                .getRuleByName("UGYLDIG_ORGNR_LENGDE")
                .executeRule().result shouldBeEqualTo true
        }

        it("UGYLDIG_ORGNR_LENGDE should not trigger on when orgnr is 9") {
            val sykemelding = Sykmelding(
                "1",
                "1",
                "2",
                MedisinskVurdering(
                    hovedDiagnose = null,
                    biDiagnoser = emptyList(),
                    svangerskap = false,
                    yrkesskadeDato = null,
                    annenFraversArsak = null,
                    yrkesskade = false
                ),
                false,
                generateArbeidsgiver(),
                generatePerioder(),
                generatePrognose(),
                emptyMap(),
                null,
                null,
                null,
                null,
                null,
                generateKontaktMedPasient(),
                behandletTidspunkt,
                generateBehandler(),
                AvsenderSystem("test", "1"),
                null,
                signaturDato,
                null
            )

            ValidationRuleChain(sykemelding, ruleData(legekontorOrgNr = "123456789"))
                .getRuleByName("UGYLDIG_ORGNR_LENGDE")
                .executeRule().result shouldBeEqualTo false
        }
    }
})
