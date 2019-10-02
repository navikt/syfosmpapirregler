package no.nav.syfo.papirsykemelding.rules

import no.nav.syfo.model.Status
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.papirsykemelding.model.sortedFOMDate
import no.nav.syfo.papirsykemelding.model.sortedTOMDate
import no.nav.syfo.rules.Description
import no.nav.syfo.rules.Rule
import no.nav.syfo.rules.RuleData
import no.nav.syfo.sm.Diagnosekoder
import no.nav.syfo.sm.isICPC2
import no.nav.syfo.sm.toICPC2
import no.nav.syfo.validation.extractBornDate

enum class ValideringRuleChain(
    override val ruleId: Int?,
    override val status: Status,
    override val messageForUser: String,
    override val messageForSender: String,
    override val predicate: (RuleData<RuleMetadata>) -> Boolean
) : Rule<RuleData<RuleMetadata>> {

    @Description("Hele sykmeldingsperioden er før bruker har fylt 13 år. Pensjonsopptjening kan starte fra 13 år.")
    PASIENT_YNGRE_ENN_13(
        1101,
        Status.MANUAL_PROCESSING,
        "Pasienten er under 13 år. Sykmelding kan ikke benyttes.",
        "Pasienten er under 13 år. Sykmelding kan ikke benyttes.", { (sykemelding, metadata) ->
            sykemelding.perioder.sortedTOMDate().last() < extractBornDate(metadata.patientPersonNumber).plusYears(13)
        }),

    @Description("Hele sykmeldingsperioden er etter at bruker har fylt 70 år. Dersom bruker fyller 70 år i perioden skal sykmelding gå gjennom på vanlig måte.")
    PASIENT_ELDRE_ENN_70(
        1102,
        Status.MANUAL_PROCESSING,
        "Sykmelding kan ikke benyttes etter at du har fylt 70 år",
        "Pasienten er over 70 år. Sykmelding kan ikke benyttes.", { (sykemelding, metadata) ->
            sykemelding.perioder.sortedFOMDate().first() > extractBornDate(metadata.patientPersonNumber).plusYears(70)
        }),

    @Description("Ukjent houved diagnosekode type")
    UKJENT_DIAGNOSEKODETYPE(
        1137,
        Status.MANUAL_PROCESSING,
        "Den må ha en kjent diagnosekode.",
        "Ukjent diagnosekode er benyttet. ", { (sykemelding, _) ->
            sykemelding.medisinskVurdering.hovedDiagnose != null &&
                    sykemelding.medisinskVurdering.hovedDiagnose?.system !in Diagnosekoder
        }),

    @Description("Hvis hoveddiagnose er Z-diagnose (ICPC-2), avvises meldingen.")
    ICPC_2_Z_DIAGNOSE(
        1132,
        Status.MANUAL_PROCESSING,
        "Den må ha en gyldig diagnosekode som gir rett til sykepenger.",
        "Angitt hoveddiagnose (z-diagnose) gir ikke rett til sykepenger.", { (sykemelding, _) ->
            sykemelding.medisinskVurdering.hovedDiagnose?.toICPC2()?.firstOrNull()?.code?.startsWith("Z") == true
        }),

    @Description("Hvis hoveddiagnose mangler og det ikke er angitt annen lovfestet fraværsgrunn, avvises meldingen")
    HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER(
        1133,
        Status.MANUAL_PROCESSING,
        "Den må ha en hoveddiagnose eller en annen gyldig fraværsgrunn.",
        "Hoveddiagnose eller annen lovfestet fraværsgrunn mangler. ",
        { (sykemelding, _) ->
            sykemelding.medisinskVurdering.annenFraversArsak == null &&
                    sykemelding.medisinskVurdering.hovedDiagnose == null
        }),

    @Description("Hvis kodeverk ikke er angitt eller korrekt for hoveddiagnose, avvises meldingen.")
    UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE(
        1540,
        Status.MANUAL_PROCESSING,
        "Den må ha riktig kode for hoveddiagnose.",
        "Kodeverk for hoveddiagnose er feil eller mangler.", { (sykemelding, _) ->
            sykemelding.medisinskVurdering.hovedDiagnose?.system !in arrayOf(
                Diagnosekoder.ICPC2_CODE,
                Diagnosekoder.ICD10_CODE
            ) ||
                    sykemelding.medisinskVurdering.hovedDiagnose?.let { diagnose ->
                        if (diagnose.isICPC2()) {
                            Diagnosekoder.icpc2.containsKey(diagnose.kode)
                        } else {
                            Diagnosekoder.icd10.containsKey(diagnose.kode)
                        }
                    } != true
        }),

    // Revurder regel når IT ikkje lenger skal brukes
    // Her mener jeg fremdeles at vi skal nulle ut bidiagnosen dersom den er feil - ikke avvise sykmeldingen!!
    @Description("Hvis kodeverk ikke er angitt eller korrekt for bidiagnose, avvises meldingen.")
    UGYLDIG_KODEVERK_FOR_BIDIAGNOSE(
        1541,
        Status.MANUAL_PROCESSING, "Det er feil i koden for bidiagnosen.",
        "Hvis kodeverk ikke er angitt eller korrekt for bidiagnose, avvises meldingen.", { (sykemelding, _) ->
            !sykemelding.medisinskVurdering.biDiagnoser.all { diagnose ->
                if (diagnose.isICPC2()) {
                    Diagnosekoder.icpc2.containsKey(diagnose.kode)
                } else {
                    Diagnosekoder.icd10.containsKey(diagnose.kode)
                }
            }
        }),

    @Description("Organisasjonsnummeret som er oppgitt er ikke 9 tegn.")
    UGYLDIG_ORGNR_LENGDE(
        9999,
        Status.MANUAL_PROCESSING,
        "Den må ha riktig organisasjonsnummer.",
        "Feil format på organisasjonsnummer. Dette skal være 9 sifre..", { (_, metadata) ->
            metadata.legekontorOrgnr != null && metadata.legekontorOrgnr.length != 9
        }),
}
