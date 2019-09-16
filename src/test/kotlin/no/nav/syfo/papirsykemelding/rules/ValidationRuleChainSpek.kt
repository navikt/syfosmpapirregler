package no.nav.syfo.papirsykemelding.rules

import com.devskiller.jfairy.Fairy
import com.devskiller.jfairy.producer.person.PersonProperties
import com.devskiller.jfairy.producer.person.PersonProvider
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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
import no.nav.syfo.rules.RuleData
import no.nav.syfo.signaturDato
import no.nav.syfo.sm.Diagnosekoder
import no.nav.syfo.toDiagnose
import no.nav.syfo.validation.extractBornYear
import no.nav.syfo.validation.validatePersonAndDNumber
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

val fairy: Fairy = Fairy.create() // (Locale("no", "NO"))
val personNumberDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("ddMMyy")

object ValidationRuleChainSpek : Spek({
    fun ruleData(
        sykmelding: Sykmelding,
        receivedDate: LocalDateTime = LocalDateTime.now(),
        signatureDate: LocalDateTime = LocalDateTime.now(),
        patientPersonNumber: String = "1234567891",
        rulesetVersion: String = "1",
        legekontorOrgNr: String = "123456789",
        tssid: String? = "1314445"
    ): RuleData<RuleMetadata> = RuleData(
        sykmelding,
        RuleMetadata(
            signatureDate,
            receivedDate,
            patientPersonNumber,
            rulesetVersion,
            legekontorOrgNr,
            tssid
        )
    )

    describe("Testing validation rules and checking the rule outcomes") {

        it("Should check rule PASIENT_YNGRE_ENN_13,should trigger rule") {
            val person = fairy.person(PersonProperties.ageBetween(PersonProvider.MIN_AGE, 12))

            ValideringRuleChain.PASIENT_YNGRE_ENN_13(ruleData(
                generateSykemelding(generatePerioder()),
                    patientPersonNumber = generatePersonNumber(
                        person.dateOfBirth,
                        false
                    )
            )) shouldEqual true
        }

        it("Should check rule PASIENT_YNGRE_ENN_13,should NOT trigger rule") {
            val person = fairy.person(
                    PersonProperties.ageBetween(13, 70))

            ValideringRuleChain.PASIENT_YNGRE_ENN_13(ruleData(
                generateSykemelding(generatePerioder()),
                    patientPersonNumber = generatePersonNumber(
                        person.dateOfBirth,
                        false
                    )
            )) shouldEqual false
        }

        it("Should check rule PASIENT_ELDRE_ENN_70,should trigger rule") {
            val person = fairy.person(
                    PersonProperties.ageBetween(71, 88))

            ValideringRuleChain.PASIENT_ELDRE_ENN_70(ruleData(
                generateSykemelding(generatePerioder()),
                    patientPersonNumber = generatePersonNumber(
                        person.dateOfBirth,
                        false
                    )
            )) shouldEqual true
        }

        it("Should check rule PASIENT_ELDRE_ENN_70,should NOT trigger rule") {
            val person = fairy.person(
                    PersonProperties.ageBetween(13, 69))

            ValideringRuleChain.PASIENT_ELDRE_ENN_70(ruleData(
                generateSykemelding(generatePerioder()),
                    patientPersonNumber = generatePersonNumber(
                        person.dateOfBirth,
                        false
                    )
            )) shouldEqual false
        }

        it("Skal håndtere fødselsnummer fra 1854-1899") {
            val beregnetFodselsar1 = extractBornYear("01015450000")
            val beregnetFodselsar2 = extractBornYear("01015474900")
            val beregnetFodselsar3 = extractBornYear("01019950000")
            val beregnetFodselsar4 = extractBornYear("01019974900")

            beregnetFodselsar1 shouldEqual 1854
            beregnetFodselsar2 shouldEqual 1854
            beregnetFodselsar3 shouldEqual 1899
            beregnetFodselsar4 shouldEqual 1899
        }

        it("Skal håndtere fødselsnummer fra 1900-1999") {
            val beregnetFodselsar1 = extractBornYear("01010000000")
            val beregnetFodselsar2 = extractBornYear("01010049900")
            val beregnetFodselsar3 = extractBornYear("01019900000")
            val beregnetFodselsar4 = extractBornYear("01019949900")

            beregnetFodselsar1 shouldEqual 1900
            beregnetFodselsar2 shouldEqual 1900
            beregnetFodselsar3 shouldEqual 1999
            beregnetFodselsar4 shouldEqual 1999
        }

        it("Skal håndtere fødselsnummer fra 1940-1999") {
            val beregnetFodselsar1 = extractBornYear("01014090000")
            val beregnetFodselsar2 = extractBornYear("01014099900")
            val beregnetFodselsar3 = extractBornYear("01019990000")
            val beregnetFodselsar4 = extractBornYear("01019999900")

            beregnetFodselsar1 shouldEqual 1940
            beregnetFodselsar2 shouldEqual 1940
            beregnetFodselsar3 shouldEqual 1999
            beregnetFodselsar4 shouldEqual 1999
        }

        it("Skal håndtere fødselsnummer fra 2000-2039") {
            val beregnetFodselsar1 = extractBornYear("01010050000")
            val beregnetFodselsar2 = extractBornYear("01010099900")
            val beregnetFodselsar3 = extractBornYear("01013950000")
            val beregnetFodselsar4 = extractBornYear("01013999900")

            beregnetFodselsar1 shouldEqual 2000
            beregnetFodselsar2 shouldEqual 2000
            beregnetFodselsar3 shouldEqual 2039
            beregnetFodselsar4 shouldEqual 2039
        }

        it("Should check rule ICPC_2_Z_DIAGNOSE,should trigger rule") {
            val sykmelding = Sykmelding("1",
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

            ValideringRuleChain.ICPC_2_Z_DIAGNOSE(ruleData(sykmelding)) shouldEqual true
        }

        it("Should check rule ICPC_2_Z_DIAGNOSE,should NOT trigger rule") {
            val sykmelding = Sykmelding("1",
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
            ValideringRuleChain.ICPC_2_Z_DIAGNOSE(ruleData(sykmelding)) shouldEqual false
        }

        it("Should check rule ICPC_2_Z_DIAGNOSE,should NOT trigger rule") {
            val sykemelding = Sykmelding("1",
                "1",
                "2",
                MedisinskVurdering(
                    hovedDiagnose = Diagnose(
                        system = "2.16.578.1.12.4.1.1.7170",
                        kode = "A62"
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

            ValideringRuleChain.ICPC_2_Z_DIAGNOSE(ruleData(sykemelding)) shouldEqual false
        }

        it("Should check rule HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER,should trigger rule") {
            val sykemelding = Sykmelding("1",
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

            ValideringRuleChain.HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER(ruleData(sykemelding)) shouldEqual true
        }

        it("Should check rule HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER,should NOT trigger rule") {
            val sykemelding = Sykmelding("1",
                "1",
                "2",
                MedisinskVurdering(
                    hovedDiagnose = Diagnose(system = "2.16.578.1.12.4.1.1.9999", kode = "A09"),
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

            ValideringRuleChain.HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER(ruleData(sykemelding)) shouldEqual false
        }

        it("Should check rule UKJENT_DIAGNOSEKODETYPE,should trigger rule") {
            val sykemelding = Sykmelding("1",
                "1",
                "2",
                MedisinskVurdering(
                    hovedDiagnose = Diagnose(system = "2.16.578.1.12.4.1.1.9999", kode = "A09"),
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

            ValideringRuleChain.UKJENT_DIAGNOSEKODETYPE(ruleData(sykemelding)) shouldEqual true
        }

        it("Should check rule UKJENT_DIAGNOSEKODETYPE,should NOT trigger rule") {
            val sykemelding = Sykmelding("1",
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

            ValideringRuleChain.UKJENT_DIAGNOSEKODETYPE(ruleData(sykemelding)) shouldEqual false
        }

        it("Should check rule UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE, wrong kodeverk for hoveddiagnose") {
            val sykemelding = Sykmelding("1",
                "1",
                "2",
                MedisinskVurdering(
                    hovedDiagnose = Diagnose(system = "2.16.578.1.12.4.1.1.7110", kode = "Z09"),
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

            ValideringRuleChain.UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE(ruleData(sykemelding)) shouldEqual true
        }

        it("Should check rule UGYLDIG_KODEVERK_FOR_BIDIAGNOSE, wrong kodeverk for biDiagnoser") {
            val sykemelding = Sykmelding("1",
                "1",
                "2",
                MedisinskVurdering(
                    hovedDiagnose = null,
                    biDiagnoser = listOf(Diagnose(system = "2.16.578.1.12.4.1.1.7110", kode = "Z09")),
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

            ValideringRuleChain.UGYLDIG_KODEVERK_FOR_BIDIAGNOSE(ruleData(sykemelding)) shouldEqual true
        }

        it("Should check rule UGYLDIG_KODEVERK_FOR_BIDIAGNOSE, correct kodeverk for biDiagnoser") {
            val sykemelding = Sykmelding("1",
                "1",
                "2",
                MedisinskVurdering(
                    hovedDiagnose = null,
                    biDiagnoser = listOf(Diagnose(system = "2.16.578.1.12.4.1.1.7170", kode = "L92")),
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

            ValideringRuleChain.UGYLDIG_KODEVERK_FOR_BIDIAGNOSE(ruleData(sykemelding)) shouldEqual false
        }

        it("UGYLDIG_ORGNR_LENGDE should trigger on when orgnr lengt is not 9") {
            val sykemelding = Sykmelding("1",
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

            ValideringRuleChain.UGYLDIG_ORGNR_LENGDE(ruleData(sykemelding, legekontorOrgNr = "1234567890")) shouldEqual true
        }

        it("UGYLDIG_ORGNR_LENGDE should not trigger on when orgnr is 9") {
            val sykemelding = Sykmelding("1",
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

            ValideringRuleChain.UGYLDIG_ORGNR_LENGDE(ruleData(sykemelding, legekontorOrgNr = "123456789")) shouldEqual false
        }
    }
})

fun generatePersonNumber(bornDate: LocalDate, useDNumber: Boolean = false): String {
    val personDate = bornDate.format(personNumberDateFormat).let {
        if (useDNumber) "${it[0] + 4}${it.substring(1)}" else it
    }
    return (if (bornDate.year >= 2000) (75011..99999) else (11111..50099))
            .map { "$personDate$it" }
            .first {
                validatePersonAndDNumber(it)
            }
}
