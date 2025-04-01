package no.nav.syfo.papirsykemelding.rules.arbeidsuforhet

import no.nav.syfo.model.Status
import no.nav.syfo.papirsykemelding.rules.common.RuleHit

enum class ArbeidsuforhetRuleHit(
    val ruleHit: RuleHit,
) {
    ICPC_2_Z_DIAGNOSE(
        ruleHit =
            RuleHit(
                rule = "ICPC_2_Z_DIAGNOSE",
                status = Status.MANUAL_PROCESSING,
                messageForSender =
                    "Angitt hoveddiagnose (z-diagnose) gir ikke rett til sykepenger.",
                messageForUser = "Den må ha en gyldig diagnosekode som gir rett til sykepenger.",
            ),
    ),
    FRAVAERSGRUNN_MANGLER(
        ruleHit =
            RuleHit(
                rule = "FRAVAERSGRUNN_MANGLER",
                status = Status.MANUAL_PROCESSING,
                messageForSender =
                    "Sykmeldingen kan ikke rettes, det må skrives en ny. " +
                        "Pasienten har fått beskjed om å vente på ny sykmelding fra deg. Grunnet følgende:" +
                        "Hoveddiagnose eller annen lovfestet fraværsgrunn mangler. ",
                messageForUser = "Den må ha en hoveddiagnose eller en annen gyldig fraværsgrunn.",
            ),
    ),
    UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE(
        ruleHit =
            RuleHit(
                rule = "UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE",
                status = Status.MANUAL_PROCESSING,
                messageForSender =
                    "Kodeverk for hoveddiagnose er feil. Prosesskoder ikke kan benyttes for å angi diagnose.",
                messageForUser = "Den må ha riktig kode for hoveddiagnose.",
            ),
    ),
    UGYLDIG_KODEVERK_FOR_BIDIAGNOSE(
        ruleHit =
            RuleHit(
                rule = "UGYLDIG_KODEVERK_FOR_BIDIAGNOSE",
                status = Status.MANUAL_PROCESSING,
                messageForSender =
                    "Hvis kodeverk ikke er angitt eller korrekt for bidiagnose, avvises meldingen. Prosesskoder ikke kan benyttes for å angi diagnose.",
                messageForUser = "Det er brukt eit ukjent kodeverk for bidiagnosen.",
            ),
    ),
}
