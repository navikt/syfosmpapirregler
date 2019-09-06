package no.nav.syfo

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import no.nav.syfo.model.Adresse
import no.nav.syfo.model.Arbeidsgiver
import no.nav.syfo.model.AvsenderSystem
import no.nav.syfo.model.Behandler
import no.nav.syfo.model.HarArbeidsgiver
import no.nav.syfo.model.KontaktMedPasient
import no.nav.syfo.model.MedisinskVurdering
import no.nav.syfo.model.Periode
import no.nav.syfo.model.Prognose
import no.nav.syfo.model.ReceivedSykmelding
import no.nav.syfo.model.RuleInfo
import no.nav.syfo.model.Status
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.model.ValidationResult

val behandletTidspunkt = LocalDateTime.of(2019, 1, 1, 0, 0)
val signaturDato = LocalDateTime.of(2019, 1, 1, 0, 0)
fun generateReceivedSykemelding(perioder: List<Periode> = emptyList()): ReceivedSykmelding {
    return ReceivedSykmelding(
        fellesformat = "felles",
        legekontorHerId = "1",
        legekontorOrgName = "legekontor",
        legekontorOrgNr = "12345",
        legekontorReshId = "123",
        mottattDato = LocalDateTime.of(2019, 1, 1, 0, 0),
        msgId = UUID.randomUUID().toString(),
        navLogId = UUID.randomUUID().toString(),
        personNrLege = "12054475942",
        personNrPasient = "18028846896",
        rulesetVersion = null,
        sykmelding = generateSykemelding(perioder),
        tlfPasient = null,
        tssid = null
    )
}

fun generateSykemelding(perioder: List<Periode>): Sykmelding {
    return Sykmelding("1",
        "1",
        "2",
        generateMedisinskVurdering(),
        false,
        generateArbeidsgiver(),
        perioder,
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
}

fun generatePerioder(): List<Periode> {
    return listOf(Periode(
        LocalDate.of(2019, 1, 1),
        LocalDate.of(2019, 1, 4),
        null,
        null,
        null,
        null,
        false
    ))
}

fun generateBehandler(): Behandler {
    return Behandler("test", null, "Tester", "1",
        "12054475942", null, null, generateAdresse(), null)
}

fun generateAdresse(): Adresse {
    return Adresse(null, null, null, null, null)
}

fun generateKontaktMedPasient(): KontaktMedPasient {
    return KontaktMedPasient(null, null)
}

fun generatePrognose(): Prognose {
    return Prognose(true, null, null, null)
}

fun generateArbeidsgiver(): Arbeidsgiver {
    return Arbeidsgiver(HarArbeidsgiver.EN_ARBEIDSGIVER, null, null, null)
}

fun generateMedisinskVurdering(): MedisinskVurdering {
    return MedisinskVurdering(
        hovedDiagnose = null,
        biDiagnoser = emptyList(),
        svangerskap = false,
        yrkesskadeDato = null,
        annenFraversArsak = null,
        yrkesskade = false
    )
}

fun getValidResult(): ValidationResult {
    return ValidationResult(Status.OK, emptyList())
}

fun getInvalidResult(): ValidationResult {
    return ValidationResult(
        Status.INVALID, listOf(
            RuleInfo("Ingen perioder",
                "Ingen perioder registrert",
                "Ingen perioder registrert")
        ))
}