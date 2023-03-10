package no.nav.syfo.rules.periodlogic

import no.nav.syfo.model.Status
import no.nav.syfo.papirsykemelding.rules.common.RuleHit

enum class PeriodLogicRuleHit(
    val ruleHit: RuleHit
) {
    PERIODER_MANGLER(
        ruleHit = RuleHit(
            rule = "PERIODER_MANGLER",
            status = Status.MANUAL_PROCESSING,
            messageForSender = "Hvis ingen perioder er oppgitt skal sykmeldingen avvises.",
            messageForUser = "Det er ikke oppgitt hvilken periode sykmeldingen gjelder for."
        )
    ),
    FRADATO_ETTER_TILDATO(
        ruleHit = RuleHit(
            rule = "FRADATO_ETTER_TILDATO",
            status = Status.MANUAL_PROCESSING,
            messageForSender = "Hvis tildato for en periode ligger før fradato avvises meldingen og hvilken periode det gjelder oppgis.",
            messageForUser = "Det er lagt inn datoer som ikke stemmer innbyrdes."
        )
    ),
    OVERLAPPENDE_PERIODER(
        ruleHit = RuleHit(
            rule = "OVERLAPPENDE_PERIODER",
            status = Status.MANUAL_PROCESSING,
            messageForSender = "Hvis det finnes opphold mellom perioder i sykmeldingen avvises meldingen.",
            messageForUser = "Periodene må ikke overlappe hverandre."
        )
    ),
    OPPHOLD_MELLOM_PERIODER(
        ruleHit = RuleHit(
            rule = "OPPHOLD_MELLOM_PERIODER",
            status = Status.MANUAL_PROCESSING,
            messageForSender = "Sykmeldingen kan ikke rettes, det må skrives en ny. Pasienten har fått beskjed om å " +
                "vente på ny sykmelding fra deg. Grunnet følgende: " +
                "Hvis det finnes opphold mellom perioder i sykmeldingen avvises meldingen.",
            messageForUser = "Det er opphold mellom sykmeldingsperiodene."
        )
    ),
    TILBAKEDATERT_MER_ENN_3_AR(
        ruleHit = RuleHit(
            rule = "TILBAKEDATERT_MER_ENN_3_AR",
            status = Status.MANUAL_PROCESSING,
            messageForSender = "Sykmeldinges fom-dato er mer enn 3 år tilbake i tid.",
            messageForUser = "Startdatoen er mer enn tre år tilbake."
        )
    ),
    FREMDATERT(
        ruleHit = RuleHit(
            rule = "FREMDATERT",
            status = Status.MANUAL_PROCESSING,
            messageForSender = "Sykmeldingen er fremdatert mer enn 30 dager etter behandletDato",
            messageForUser = "Sykmeldingen er datert mer enn 30 dager fram i tid."
        )
    ),
    TOTAL_VARIGHET_OVER_ETT_AAR(
        ruleHit = RuleHit(
            rule = "TOTAL_VARIGHET_OVER_ETT_AAR",
            status = Status.MANUAL_PROCESSING,
            messageForSender = "Hvis sykmeldingens sluttdato er mer enn ett år frem i tid, avvises meldingen.",
            messageForUser = "Den kan ikke ha en varighet på over ett år."
        )
    ),
    AVVENTENDE_SYKMELDING_KOMBINERT(
        ruleHit = RuleHit(
            rule = "AVVENTENDE_SYKMELDING_KOMBINERT",
            status = Status.MANUAL_PROCESSING,
            messageForSender = "Hvis avventende sykmelding er funnet og det finnes en eller flere perioder. ",
            messageForUser = "En avventende sykmelding kan bare inneholde én periode."
        )
    ),
    MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER(
        ruleHit = RuleHit(
            rule = "MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER",
            status = Status.MANUAL_PROCESSING,
            messageForSender = "Hvis innspill til arbeidsgiver om tilrettelegging i pkt 4.1.3 ikke er utfylt ved avventende sykmelding avvises meldingen",
            messageForUser = "En avventende sykmelding forutsetter at du kan jobbe hvis arbeidsgiveren din legger til " +
                "rette for det. Den som har sykmeldt deg har ikke foreslått hva arbeidsgiveren kan gjøre, " +
                "noe som kreves for denne typen sykmelding."
        )
    ),
    AVVENTENDE_SYKMELDING_OVER_16_DAGER(
        ruleHit = RuleHit(
            rule = "AVVENTENDE_SYKMELDING_OVER_16_DAGER",
            status = Status.MANUAL_PROCESSING,
            messageForSender = "SHvis avventende sykmelding benyttes utover i arbeidsgiverperioden på 16 kalenderdager, avvises meldingen.",
            messageForUser = "En avventende sykmelding kan bare gis for 16 dager."
        )
    ),
    FOR_MANGE_BEHANDLINGSDAGER_PER_UKE(
        ruleHit = RuleHit(
            rule = "FOR_MANGE_BEHANDLINGSDAGER_PER_UKE",
            status = Status.MANUAL_PROCESSING,
            messageForSender = "Hvis antall dager oppgitt for behandlingsdager periode er for høyt i forhold til" +
                " periodens lengde avvises meldingen. Mer enn en dag per uke er for høyt. 1 dag per påbegynt uke.",
            messageForUser = "Det er angitt for mange behandlingsdager. Det kan bare angis én behandlingsdag per uke."
        )
    ),
    GRADERT_SYKMELDING_OVER_99_PROSENT(
        ruleHit = RuleHit(
            rule = "GRADERT_SYKMELDING_OVER_99_PROSENT",
            status = Status.INVALID,
            messageForSender = "Sykmeldingen kan ikke rettes, det må skrives en ny. " +
                "Pasienten har fått beskjed om å vente på ny sykmelding fra deg. Grunnet følgende:" +
                "Hvis sykmeldingsgrad er høyere enn 99% for delvis sykmelding avvises meldingen",
            messageForUser = "Sykmeldingsgraden kan ikke være mer enn 99% fordi det er en gradert sykmelding."
        )
    )
}
