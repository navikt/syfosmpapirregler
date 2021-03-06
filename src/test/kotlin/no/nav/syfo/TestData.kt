package no.nav.syfo

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import no.nav.syfo.client.norskhelsenett.Godkjenning
import no.nav.syfo.client.norskhelsenett.Kode
import no.nav.syfo.model.Adresse
import no.nav.syfo.model.AktivitetIkkeMulig
import no.nav.syfo.model.Arbeidsgiver
import no.nav.syfo.model.AvsenderSystem
import no.nav.syfo.model.Behandler
import no.nav.syfo.model.Diagnose
import no.nav.syfo.model.Gradert
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
import no.nav.syfo.papirsykemelding.model.HelsepersonellKategori
import no.nav.syfo.sm.Diagnosekoder

val behandletTidspunkt = LocalDateTime.of(2019, 1, 1, 0, 0)
val signaturDato = LocalDateTime.of(2019, 1, 1, 0, 0)

fun Diagnosekoder.DiagnosekodeType.toDiagnose() = Diagnose(system = oid, kode = code, tekst = text)

fun generateReceivedSykemelding(perioder: List<Periode> = emptyList()): ReceivedSykmelding {
    return ReceivedSykmelding(
        fellesformat = "",
        legekontorHerId = null,
        legekontorOrgName = "",
        legekontorOrgNr = null,
        legekontorReshId = null,
        mottattDato = LocalDateTime.of(2019, 1, 1, 0, 0),
        msgId = UUID.randomUUID().toString(),
        navLogId = UUID.randomUUID().toString(),
        personNrLege = "12054475942",
        personNrPasient = "18028846896",
        rulesetVersion = null,
        sykmelding = generateSykemelding(perioder),
        tlfPasient = null,
        tssid = null,
        merknader = null
    )
}

fun generateSykemelding(
    perioder: List<Periode> = generatePerioder(),
    tidspunkt: LocalDateTime = behandletTidspunkt,
    signaturDateTime: LocalDateTime = signaturDato,
    kontaktMedPasient: KontaktMedPasient = generateKontaktMedPasient(),
    diagnose: Diagnose = Diagnosekoder.icd10.values.stream().findFirst().get().toDiagnose()
): Sykmelding {
    return Sykmelding("1",
        "1",
        "2",
        generateMedisinskVurdering(diagnose = diagnose),
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
        kontaktMedPasient,
        tidspunkt,
        generateBehandler(),
        AvsenderSystem("test", "1"),
        null,
        signaturDateTime,
        null
        )
}

fun generatePerioder(): List<Periode> {
    return listOf(Periode(
        LocalDate.of(2019, 1, 1),
        LocalDate.of(2019, 1, 4),
        AktivitetIkkeMulig(null, null),
        null,
        null,
        null,
        false
    ))
}
fun generatePeriode(
    fom: LocalDate = LocalDate.now(),
    tom: LocalDate = LocalDate.now().plusDays(10),
    aktivitetIkkeMulig: AktivitetIkkeMulig? = null,
    avventendeInnspillTilArbeidsgiver: String? = null,
    behandlingsdager: Int? = null,
    gradert: Gradert? = null,
    reisetilskudd: Boolean = false
) = Periode(
    fom = fom,
    tom = tom,
    aktivitetIkkeMulig = aktivitetIkkeMulig,
    avventendeInnspillTilArbeidsgiver = avventendeInnspillTilArbeidsgiver,
    behandlingsdager = behandlingsdager,
    gradert = gradert,
    reisetilskudd = reisetilskudd
)

fun generateBehandler(): Behandler {
    return Behandler("test", null, "Tester", "1",
        "12054475942", null, null, generateAdresse(), null)
}

fun generateAdresse(): Adresse {
    return Adresse(null, null, null, null, null)
}

fun generateKontaktMedPasient(begrunnelseIkkeKontakt: String? = null, localDateTime: LocalDate = LocalDateTime.now().toLocalDate()): KontaktMedPasient {
    return KontaktMedPasient(localDateTime, begrunnelseIkkeKontakt)
}

fun generatePrognose(): Prognose {
    return Prognose(true, null, null, null)
}

fun generateArbeidsgiver(): Arbeidsgiver {
    return Arbeidsgiver(HarArbeidsgiver.EN_ARBEIDSGIVER, null, null, null)
}

fun generateMedisinskVurdering(diagnose: Diagnose = Diagnosekoder.icd10.values.stream().findFirst().get().toDiagnose()): MedisinskVurdering {
    return MedisinskVurdering(
        hovedDiagnose = diagnose,
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
        Status.MANUAL_PROCESSING, listOf(
            RuleInfo("Ingen perioder",
                "Ingen perioder registrert",
                "Ingen perioder registrert", Status.MANUAL_PROCESSING)
        ))
}

fun getBehandlerNotInHPRRule(): ValidationResult {
    return ValidationResult(
        status = Status.MANUAL_PROCESSING,
        ruleHits = listOf(
            RuleInfo(
                ruleName = "BEHANLDER_IKKE_I_HPR",
                messageForSender = "Den som har skrevet sykmeldingen din har ikke autorisasjon til dette.",
                messageForUser = "Behandler er ikke register i HPR",
                ruleStatus = Status.MANUAL_PROCESSING
            )
        )
    )
}

fun generateGradert(
    reisetilskudd: Boolean = false,
    grad: Int = 50
) = Gradert(
    reisetilskudd = reisetilskudd,
    grad = grad
)

fun getGyldigBehandler(): no.nav.syfo.client.norskhelsenett.Behandler {
    return no.nav.syfo.client.norskhelsenett.Behandler(
        listOf(
            Godkjenning(
                autorisasjon = Kode(
                    true,
                    7704,
                    "17"
                ),
                helsepersonellkategori = Kode(
                    true,
                    7702,
                    HelsepersonellKategori.LEGE.verdi
                )
            )
        )
    )
}

fun getUgyldigBehandler(): no.nav.syfo.client.norskhelsenett.Behandler {
    return no.nav.syfo.client.norskhelsenett.Behandler(
        listOf(
            Godkjenning(
                autorisasjon = Kode(
                    false,
                    2,
                    "LE"
                )
            )
        )
    )
}
