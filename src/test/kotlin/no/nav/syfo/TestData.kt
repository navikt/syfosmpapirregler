package no.nav.syfo

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import no.nav.helse.diagnosekoder.Diagnosekoder
import no.nav.syfo.model.Adresse
import no.nav.syfo.model.AktivitetIkkeMulig
import no.nav.syfo.model.AnnenFraversArsak
import no.nav.syfo.model.Arbeidsgiver
import no.nav.syfo.model.AvsenderSystem
import no.nav.syfo.model.Behandler
import no.nav.syfo.model.Diagnose
import no.nav.syfo.model.Gradert
import no.nav.syfo.model.HarArbeidsgiver
import no.nav.syfo.model.KontaktMedPasient
import no.nav.syfo.model.MedisinskVurdering
import no.nav.syfo.model.MeldingTilNAV
import no.nav.syfo.model.Periode
import no.nav.syfo.model.Prognose
import no.nav.syfo.model.ReceivedSykmelding
import no.nav.syfo.model.RuleInfo
import no.nav.syfo.model.SporsmalSvar
import no.nav.syfo.model.Status
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.model.ValidationResult

val behandletTidspunkt: LocalDateTime = LocalDateTime.now()
val signaturDato: LocalDateTime = LocalDateTime.now()

fun Diagnosekoder.ICPC2.toDiagnose() = Diagnose(system = oid, kode = code, tekst = text)

fun generateReceivedSykemelding(perioder: List<Periode> = emptyList()): ReceivedSykmelding {
    return ReceivedSykmelding(
        fellesformat = "",
        legekontorHerId = null,
        legekontorOrgName = "",
        legekontorOrgNr = null,
        legekontorReshId = null,
        mottattDato = LocalDateTime.now(),
        msgId = UUID.randomUUID().toString(),
        navLogId = UUID.randomUUID().toString(),
        personNrLege = "12054475942",
        personNrPasient = "18028846896",
        rulesetVersion = null,
        sykmelding = generateSykemelding(perioder),
        tlfPasient = null,
        tssid = null,
        merknader = null,
        legeHelsepersonellkategori = null,
        legeHprNr = null,
        partnerreferanse = null,
        vedlegg = null,
        utenlandskSykmelding = null,
    )
}

fun generateSykmelding(
    fom: LocalDate = LocalDate.now(),
    tom: LocalDate = LocalDate.now().plusDays(10),
    id: String = UUID.randomUUID().toString(),
    pasientAktoerId: String = UUID.randomUUID().toString(),
    syketilfelleStartDato: LocalDate = LocalDate.now(),
    diagnose: Diagnose? = Diagnosekoder.icpc2.values.stream().findFirst().get().toDiagnose(),
    biDiagnose: List<Diagnose> = emptyList(),
    medisinskVurdering: MedisinskVurdering =
        generateMedisinskVurdering(diagnose = diagnose, biDiagnose = biDiagnose),
    skjermetForPasient: Boolean = false,
    perioder: List<Periode> = listOf(generatePeriode(fom = fom, tom = tom)),
    prognose: Prognose = generatePrognose(),
    utdypendeOpplysninger: Map<String, Map<String, SporsmalSvar>> = mapOf(),
    tiltakArbeidsplassen: String? = null,
    tiltakNAV: String? = null,
    andreTiltak: String? = null,
    meldingTilNAV: MeldingTilNAV? = null,
    meldingTilArbeidsgiver: String? = null,
    kontaktMedPasient: KontaktMedPasient = generateKontaktMedPasient(),
    behandletTidspunkt: LocalDateTime = LocalDateTime.now(),
    behandler: Behandler = generateBehandler(),
    avsenderSystem: AvsenderSystem = generateAvsenderSystem(),
    arbeidsgiver: Arbeidsgiver = generateArbeidsgiver(),
    msgid: String = UUID.randomUUID().toString(),
    navnFastlege: String? = null,
    signaturDato: LocalDateTime = behandletTidspunkt,
) =
    Sykmelding(
        id = id,
        msgId = msgid,
        pasientAktoerId = pasientAktoerId,
        signaturDato = signaturDato,
        syketilfelleStartDato = syketilfelleStartDato,
        medisinskVurdering = medisinskVurdering,
        skjermesForPasient = skjermetForPasient,
        perioder = perioder,
        prognose = prognose,
        utdypendeOpplysninger = utdypendeOpplysninger,
        tiltakArbeidsplassen = tiltakArbeidsplassen,
        tiltakNAV = tiltakNAV,
        andreTiltak = andreTiltak,
        meldingTilNAV = meldingTilNAV,
        meldingTilArbeidsgiver = meldingTilArbeidsgiver,
        kontaktMedPasient = kontaktMedPasient,
        behandletTidspunkt = behandletTidspunkt,
        behandler = behandler,
        avsenderSystem = avsenderSystem,
        arbeidsgiver = arbeidsgiver,
        navnFastlege = navnFastlege,
    )

fun generateAvsenderSystem(
    navn: String = "test",
    versjon: String = "1.2.3",
) =
    AvsenderSystem(
        navn = navn,
        versjon = versjon,
    )

fun generateSykemelding(
    perioder: List<Periode> = generatePerioder(),
    tidspunkt: LocalDateTime = behandletTidspunkt,
    signaturDateTime: LocalDateTime = signaturDato,
    kontaktMedPasient: KontaktMedPasient = generateKontaktMedPasient(),
    diagnose: Diagnose? = Diagnosekoder.icpc2.values.stream().findFirst().get().toDiagnose(),
    biDiagnose: List<Diagnose> = emptyList(),
    annenFravarArsak: AnnenFraversArsak? = null,
): Sykmelding {
    return Sykmelding(
        "1",
        "1",
        "2",
        generateMedisinskVurdering(diagnose = diagnose, biDiagnose = biDiagnose, annenFravarArsak),
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
        null,
    )
}

fun generatePerioder(): List<Periode> {
    return listOf(
        Periode(
            LocalDate.now(),
            LocalDate.now().plusDays(3),
            AktivitetIkkeMulig(null, null),
            null,
            null,
            null,
            false,
        ),
    )
}

fun generatePeriode(
    fom: LocalDate = LocalDate.now(),
    tom: LocalDate = LocalDate.now().plusDays(10),
    aktivitetIkkeMulig: AktivitetIkkeMulig? = null,
    avventendeInnspillTilArbeidsgiver: String? = null,
    behandlingsdager: Int? = null,
    gradert: Gradert? = null,
    reisetilskudd: Boolean = false,
) =
    Periode(
        fom = fom,
        tom = tom,
        aktivitetIkkeMulig = aktivitetIkkeMulig,
        avventendeInnspillTilArbeidsgiver = avventendeInnspillTilArbeidsgiver,
        behandlingsdager = behandlingsdager,
        gradert = gradert,
        reisetilskudd = reisetilskudd,
    )

fun generateBehandler(): Behandler {
    return Behandler(
        "test",
        null,
        "Tester",
        "1",
        "12054475942",
        null,
        null,
        generateAdresse(),
        null,
    )
}

fun generateAdresse(): Adresse {
    return Adresse(null, null, null, null, null)
}

fun generateKontaktMedPasient(
    begrunnelseIkkeKontakt: String? = null,
    localDateTime: LocalDate = LocalDateTime.now().toLocalDate()
): KontaktMedPasient {
    return KontaktMedPasient(localDateTime, begrunnelseIkkeKontakt)
}

fun generatePrognose(): Prognose {
    return Prognose(true, null, null, null)
}

fun generateArbeidsgiver(): Arbeidsgiver {
    return Arbeidsgiver(HarArbeidsgiver.EN_ARBEIDSGIVER, null, null, null)
}

fun generateMedisinskVurdering(
    diagnose: Diagnose? = Diagnosekoder.icpc2.values.stream().findFirst().get().toDiagnose(),
    biDiagnose: List<Diagnose>,
    annenFraversArsak: AnnenFraversArsak? = null,
): MedisinskVurdering {
    return MedisinskVurdering(
        hovedDiagnose = diagnose,
        biDiagnoser = biDiagnose,
        annenFraversArsak = annenFraversArsak,
        svangerskap = false,
        yrkesskadeDato = null,
        yrkesskade = false,
    )
}

fun getValidResult(): ValidationResult {
    return ValidationResult(Status.OK, emptyList())
}

fun getInvalidResult(): ValidationResult {
    return ValidationResult(
        Status.MANUAL_PROCESSING,
        listOf(
            RuleInfo(
                "Ingen perioder",
                "Ingen perioder registrert",
                "Ingen perioder registrert",
                Status.MANUAL_PROCESSING,
            ),
        ),
    )
}

fun generateGradert(
    reisetilskudd: Boolean = false,
    grad: Int = 50,
) =
    Gradert(
        reisetilskudd = reisetilskudd,
        grad = grad,
    )
