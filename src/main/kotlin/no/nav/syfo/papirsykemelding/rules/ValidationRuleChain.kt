package no.nav.syfo.papirsykemelding.rules

import no.nav.syfo.model.Rule
import no.nav.syfo.model.Status
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.model.juridisk.JuridiskHenvisning
import no.nav.syfo.model.juridisk.Lovverk
import no.nav.syfo.papirsykemelding.model.RuleChain
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.papirsykemelding.model.sortedFOMDate
import no.nav.syfo.papirsykemelding.model.sortedTOMDate
import no.nav.syfo.sm.Diagnosekoder
import no.nav.syfo.sm.isICPC2

class ValidationRuleChain(
    private val sykmelding: Sykmelding,
    private val metadata: RuleMetadata,
) : RuleChain {
    override val rules: List<Rule<*>> = listOf(
        // Opptjening før 13 år er ikke mulig.
        // Hele sykmeldingsperioden er før bruker har fylt 13 år. Pensjonsopptjening kan starte fra 13 år.
        Rule(
            name = "PASIENT_YNGRE_ENN_13",
            ruleId = 1101,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "Pasienten er under 13 år. Sykmelding kan ikke benyttes.",
            messageForSender = "Pasienten er under 13 år. Sykmelding kan ikke benyttes.",
            juridiskHenvisning = null,
            input = object {
                val sisteTomDato = sykmelding.perioder.sortedTOMDate().last()
                val pasientFodselsdato = metadata.pasientFodselsdato
            },
            predicate = { it.sisteTomDato < it.pasientFodselsdato.plusYears(13) }
        ),

        // §8-3 Det ytes ikke sykepenger til medlem som er fylt 70 år.
        // Hele sykmeldingsperioden er etter at bruker har fylt 70 år. Dersom bruker fyller 70 år i perioden skal sykmelding gå gjennom på vanlig måte.
        Rule(
            name = "PASIENT_ELDRE_ENN_70",
            ruleId = 1102,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "Sykmelding kan ikke benyttes etter at du har fylt 70 år",
            messageForSender = "Pasienten er over 70 år. Sykmelding kan ikke benyttes.",
            JuridiskHenvisning(
                lovverk = Lovverk.FOLKETRYGDLOVEN,
                paragraf = "8-3",
                ledd = 1,
                punktum = 2,
                bokstav = null
            ),
            input = object {
                val forsteFomDato = sykmelding.perioder.sortedFOMDate().first()
                val pasientFodselsdato = metadata.pasientFodselsdato
            },
            predicate = {
                it.forsteFomDato > it.pasientFodselsdato.plusYears(70)
            }
        ),

        // §8-4 Sykmeldingen må angi sykdom eller skade eller annen gyldig fraværsgrunn som angitt i loven.
        // Kodeverk må være satt i henhold til gyldige kodeverk som angitt av Helsedirektoratet (ICPC-2 og ICD-10).
        // Ukjent houved diagnosekode type
        Rule(
            name = "UKJENT_DIAGNOSEKODETYPE",
            ruleId = 1137,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "Den må ha en kjent diagnosekode.",
            messageForSender = "Ukjent diagnosekode er benyttet.",
            JuridiskHenvisning(
                lovverk = Lovverk.FOLKETRYGDLOVEN,
                paragraf = "8-4",
                ledd = 1,
                punktum = 1,
                bokstav = null
            ),
            input = object {
                val hoveddiagnose = sykmelding.medisinskVurdering.hovedDiagnose
            },
            predicate = {
                it.hoveddiagnose != null && it.hoveddiagnose.system !in Diagnosekoder
            }
        ),

        // §8-4 Arbeidsuførhet som skyldes sosiale eller økomoniske problemer o.l. gir ikke rett til sykepenger.
        // Hvis hoveddiagnose er Z-diagnose (ICPC-2), avvises meldingen.
        Rule(
            name = "ICPC_2_Z_DIAGNOSE",
            ruleId = 1132,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "Den må ha en gyldig diagnosekode som gir rett til sykepenger.",
            messageForSender = "Angitt hoveddiagnose (z-diagnose) gir ikke rett til sykepenger.",
            JuridiskHenvisning(
                lovverk = Lovverk.FOLKETRYGDLOVEN,
                paragraf = "8-4",
                ledd = 1,
                punktum = 2,
                bokstav = null
            ),
            input = object {
                val hoveddiagnose = sykmelding.medisinskVurdering.hovedDiagnose
            },
            predicate = {
                it.hoveddiagnose != null && it.hoveddiagnose.isICPC2() && it.hoveddiagnose.kode.startsWith("Z")
            }
        ),

        // §8-4 Sykmeldingen må angi sykdom eller skade eller annen gyldig fraværsgrunn som angitt i loven.
        // Hvis hoveddiagnose mangler og det ikke er angitt annen lovfestet fraværsgrunn, avvises meldingen
        Rule(
            name = "HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER",
            ruleId = 1133,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "Den må ha en hoveddiagnose eller en annen gyldig fraværsgrunn.",
            messageForSender = "Hoveddiagnose eller annen lovfestet fraværsgrunn mangler. ",
            JuridiskHenvisning(
                lovverk = Lovverk.FOLKETRYGDLOVEN,
                paragraf = "8-4",
                ledd = 1,
                punktum = 1,
                bokstav = null
            ),
            input = object {
                val annenFraversArsak = sykmelding.medisinskVurdering.annenFraversArsak
                val hoveddiagnose = sykmelding.medisinskVurdering.hovedDiagnose
            },
            predicate = {
                it.annenFraversArsak == null && it.hoveddiagnose == null
            }
        ),

        // §8-4 Sykmeldingen må angi sykdom eller skade eller annen gyldig fraværsgrunn som angitt i loven.
        // Diagnose må være satt i henhold til angitt kodeverk.
        // Hvis kodeverk ikke er angitt eller korrekt for hoveddiagnose, avvises meldingen.
        Rule(
            name = "UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE",
            ruleId = 1540,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "Den må ha riktig kode for hoveddiagnose.",
            messageForSender = "Kodeverk for hoveddiagnose er feil. Prosesskoder ikke kan benyttes for å angi diagnose.",
            JuridiskHenvisning(
                lovverk = Lovverk.FOLKETRYGDLOVEN,
                paragraf = "8-4",
                ledd = 1,
                punktum = 1,
                bokstav = null
            ),
            input = object {
                val hoveddiagnose = sykmelding.medisinskVurdering.hovedDiagnose
            },
            predicate = {
                if (it.hoveddiagnose == null) {
                    false
                } else {
                    it.hoveddiagnose.system !in arrayOf(Diagnosekoder.ICPC2_CODE, Diagnosekoder.ICD10_CODE) || !it.hoveddiagnose.let { diagnose ->
                        if (diagnose.isICPC2()) {
                            Diagnosekoder.icpc2.containsKey(diagnose.kode)
                        } else {
                            Diagnosekoder.icd10.containsKey(diagnose.kode)
                        }
                    }
                }
            }
        ),

        // §8-4 Sykmeldingen må angi sykdom eller skade eller annen gyldig fraværsgrunn som angitt i loven.
        // Diagnose må være satt i henhold til angitt kodeverk.
        // Hvis kodeverk ikke er angitt eller korrekt for bidiagnose, avvises meldingen.
        Rule(
            name = "UGYLDIG_KODEVERK_FOR_BIDIAGNOSE",
            ruleId = 1541,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "Det er brukt eit ukjent kodeverk for bidiagnosen.",
            messageForSender = "Hvis kodeverk ikke er angitt eller korrekt for bidiagnose, avvises meldingen. Prosesskoder ikke kan benyttes for å angi diagnose.",
            JuridiskHenvisning(
                lovverk = Lovverk.FOLKETRYGDLOVEN,
                paragraf = "8-4",
                ledd = 1,
                punktum = 1,
                bokstav = null
            ),
            input = object {
                val biDiagnoser = sykmelding.medisinskVurdering.biDiagnoser
            },
            predicate = {
                !it.biDiagnoser.all { diagnose ->
                    if (diagnose.isICPC2()) {
                        Diagnosekoder.icpc2.containsKey(diagnose.kode)
                    } else {
                        Diagnosekoder.icd10.containsKey(diagnose.kode)
                    }
                }
            }
        ),

        // Orgnr må være korrekt angitt
        // Organisasjonsnummeret som er oppgitt er ikke 9 tegn.
        Rule(
            name = "UGYLDIG_ORGNR_LENGDE",
            ruleId = 9999,
            status = Status.INVALID,
            messageForUser = "Den må ha riktig organisasjonsnummer.",
            messageForSender = "Sykmeldingen kan ikke rettes, det må skrives en ny. Pasienten har fått beskjed om å vente på ny sykmelding fra deg. Grunnet følgende:" +
                "Feil format på organisasjonsnummer. Dette skal være 9 sifre.",
            juridiskHenvisning = null,
            input = object {
                val legekontorOrgnr = metadata.legekontorOrgnr
            },
            predicate = {
                it.legekontorOrgnr != null && it.legekontorOrgnr.length != 9
            }
        )
    )
}
