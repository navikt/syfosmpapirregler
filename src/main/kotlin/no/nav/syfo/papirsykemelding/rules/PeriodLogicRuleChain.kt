package no.nav.syfo.papirsykemelding.rules

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import no.nav.syfo.model.Periode
import no.nav.syfo.model.Status
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.papirsykemelding.model.sortedFOMDate
import no.nav.syfo.papirsykemelding.model.sortedTOMDate
import no.nav.syfo.rules.Description
import no.nav.syfo.rules.Rule
import no.nav.syfo.rules.RuleData

enum class PeriodLogicRuleChain(
    override val ruleId: Int?,
    override val status: Status,
    override val messageForUser: String,
    override val messageForSender: String,
    override val predicate: (RuleData<RuleMetadata>) -> Boolean
) : Rule<RuleData<RuleMetadata>> {

    @Description("Hvis ingen perioder er oppgitt skal sykmeldingen avvises.")
    PERIODER_MANGLER(
        1200,
        Status.MANUAL_PROCESSING,
        "Det er ikke oppgitt hvilken periode sykmeldingen gjelder for.",
        "Hvis ingen perioder er oppgitt skal sykmeldingen avvises.",
        { (sykmelding, _) ->
            sykmelding.perioder.isNullOrEmpty()
        }),

    @Description("Hvis tildato for en periode ligger før fradato avvises meldingen og hvilken periode det gjelder oppgis.")
    FRADATO_ETTER_TILDATO(
        1201,
        Status.MANUAL_PROCESSING,
        "Det er lagt inn datoer som ikke stemmer innbyrdes.",
        "Hvis tildato for en periode ligger før fradato avvises meldingen og hvilken periode det gjelder oppgis.",
        { (sykmelding, _) ->
            sykmelding.perioder.any { it.fom.isAfter(it.tom) }
        }),

    @Description("Hvis en eller flere perioder er overlappende avvises meldingen og hvilken periode det gjelder oppgis.")
    OVERLAPPENDE_PERIODER(
        1202,
        Status.MANUAL_PROCESSING,
        "Periodene må ikke overlappe hverandre.",
        "Hvis en eller flere perioder er overlappende avvises meldingen og hvilken periode det gjelder oppgis.",
        { (sykemelding, _) ->
            sykemelding.perioder.any { periodA ->
                sykemelding.perioder
                    .filter { periodB -> periodB != periodA }
                    .any { periodB ->
                        periodA.fom in periodB.range() || periodA.tom in periodB.range()
                    }
            }
        }),

    @Description("Hvis det finnes opphold mellom perioder i sykmeldingen avvises meldingen.")
    OPPHOLD_MELLOM_PERIODER(
        1203,
        Status.MANUAL_PROCESSING,
        "Det er opphold mellom sykmeldingsperiodene.",
        "Hvis det finnes opphold mellom perioder i sykmeldingen avvises meldingen.",
        { (sykemelding, _) ->
            val ranges = sykemelding.perioder
                .sortedBy { it.fom }
                .map { it.fom to it.tom }

            var gapBetweenPeriods = false
            for (i in 1..(ranges.size - 1)) {
                gapBetweenPeriods = workdaysBetween(
                    ranges[i - 1].second,
                    ranges[i].first
                ) > 0
            }
            gapBetweenPeriods
        }),

    @Description("Sykmeldinges fom-dato er mer enn 3 år tilbake i tid.")
    TILBAKEDATERT_MER_ENN_3_AR(
        1206,
        Status.MANUAL_PROCESSING,
        "Startdatoen er mer enn tre år tilbake.",
        "Sykmeldinges fom-dato er mer enn 3 år tilbake i tid.",
        { (sykemelding, _) ->
            sykemelding.perioder.sortedFOMDate().first().atStartOfDay().minusYears(3)
                .isAfter(sykemelding.behandletTidspunkt)
        }),

    @Description("Hvis sykmeldingen er fremdatert mer enn 30 dager etter konsultasjonsdato/signaturdato avvises meldingen.")
    FREMDATERT(
        1209,
        Status.MANUAL_PROCESSING,
        "Sykmeldingen er datert mer enn 30 dager fram i tid.",
        "Hvis sykmeldingen er fremdatert mer enn 30 dager etter behandletDato",
        { (sykemelding, ruleMetadata) ->
            sykemelding.perioder.sortedFOMDate().first().atStartOfDay() > ruleMetadata.behandletTidspunkt.plusDays(30)
        }),

    @Description("Hvis sykmeldingens sluttdato er mer enn ett år frem i tid, avvises meldingen.")
    VARIGHET_OVER_ETT_AAR(
        1211,
        Status.MANUAL_PROCESSING,
        "Den kan ikke ha en varighet på over ett år.",
        "Hvis sykmeldingens sluttdato er mer enn ett år frem i tid, avvises meldingen.",
        { (sykemelding, _) ->
            val firstFomDate = sykemelding.perioder.sortedFOMDate().first().atStartOfDay().toLocalDate()
            val lastTomDate = sykemelding.perioder.sortedTOMDate().last().atStartOfDay().toLocalDate()
            (firstFomDate..lastTomDate).daysBetween() > 365
        }),

    @Description("Hvis avventende sykmelding er funnet og det finnes en eller flere perioder")
    AVVENTENDE_SYKMELDING_KOMBINERT(
        9999,
        Status.MANUAL_PROCESSING,
        "En avventende sykmelding kan bare inneholde én periode.",
        "Hvis avventende sykmelding er funnet og det finnes en eller flere perioder. ",
        { (sykemelding, _) ->
            sykemelding.perioder.count { it.avventendeInnspillTilArbeidsgiver != null } != 0 &&
                    sykemelding.perioder.size > 1
        }),

    @Description("Hvis innspill til arbeidsgiver om tilrettelegging i pkt 4.1.3 ikke er utfylt ved avventende sykmelding avvises meldingen")
    MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER(
        1241,
        Status.MANUAL_PROCESSING,
        "En avventende sykmelding forutsetter at du kan jobbe hvis arbeidsgiveren din legger til rette for det. Den som har sykmeldt deg har ikke foreslått hva arbeidsgiveren kan gjøre, noe som kreves for denne typen sykmelding.",
        "Hvis innspill til arbeidsgiver om tilrettelegging i pkt 4.1.3 ikke er utfylt ved avventende sykmelding avvises meldingen",
        { (sykemelding, _) ->
            sykemelding.perioder
                .any { it.avventendeInnspillTilArbeidsgiver != null && it.avventendeInnspillTilArbeidsgiver?.trim().isNullOrEmpty() }
        }),

    @Description("Hvis avventende sykmelding benyttes utover i arbeidsgiverperioden på 16 kalenderdager, avvises meldingen.")
    AVVENTENDE_SYKMELDING_OVER_16_DAGER(
        1242,
        Status.MANUAL_PROCESSING,
        "En avventende sykmelding kan bare gis for 16 dager.",
        "Hvis avventende sykmelding benyttes utover i arbeidsgiverperioden på 16 kalenderdager, avvises meldingen.",
        { (sykemelding, _) ->
            sykemelding.perioder
                .filter { it.avventendeInnspillTilArbeidsgiver != null }
                .any { (it.fom..it.tom).daysBetween() > 16 }
        }),

    @Description("Hvis antall dager oppgitt for behandlingsdager periode er for høyt i forhold til periodens lengde avvises meldingen. Mer enn en dag per uke er for høyt. 1 dag per påbegynt uke.")
    FOR_MANGE_BEHANDLINGSDAGER_PER_UKE(
        1250,
        Status.MANUAL_PROCESSING,
        "Det er angitt for mange behandlingsdager. Det kan bare angis én behandlingsdag per uke.",
        "Hvis antall dager oppgitt for behandlingsdager periode er for høyt i forhold til periodens lengde avvises meldingen. Mer enn en dag per uke er for høyt. 1 dag per påbegynt uke.",
        { (sykemelding, _) ->
            sykemelding.perioder.any {
                it.behandlingsdager != null && it.behandlingsdager!! > it.range().startedWeeksBetween()
            }
        }),

    @Description("Hvis sykmeldingsgrad er mindre enn 20% for gradert sykmelding, avvises meldingen")
    GRADERT_SYKMELDING_UNDER_20_PROSENT(
        1251,
        Status.MANUAL_PROCESSING,
        "Sykmeldingsgraden kan ikke være mindre enn 20 %.",
        "Hvis sykmeldingsgrad er mindre enn 20% for gradert sykmelding, avvises meldingen",
        { (sykemelding, _) ->
            sykemelding.perioder.any {
                it.gradert != null && it.gradert!!.grad < 20
            }
        }),

    @Description("Hvis sykmeldingsgrad er høyere enn 99% for delvis sykmelding avvises meldingen")
    GRADERT_SYKMELDING_OVER_99_PROSENT(
        1252,
        Status.MANUAL_PROCESSING,
        "Sykmeldingsgraden kan ikke være mer enn 99% fordi det er en gradert sykmelding.",
        "Hvis sykmeldingsgrad er høyere enn 99% for delvis sykmelding avvises meldingen",
        { (sykemelding, _) ->
            sykemelding.perioder.mapNotNull { it.gradert }.any { it.grad > 99 }
        }),
}

fun workdaysBetween(a: LocalDate, b: LocalDate): Int = (1 until ChronoUnit.DAYS.between(a, b))
    .map { a.plusDays(it) }
    .filter { it.dayOfWeek !in arrayOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) }
    .count()

fun ClosedRange<LocalDate>.daysBetween(): Long = ChronoUnit.DAYS.between(start, endInclusive)
fun ClosedRange<LocalDate>.startedWeeksBetween(): Int = ChronoUnit.WEEKS.between(start, endInclusive).toInt() + 1
fun Periode.range(): ClosedRange<LocalDate> = fom.rangeTo(tom)
