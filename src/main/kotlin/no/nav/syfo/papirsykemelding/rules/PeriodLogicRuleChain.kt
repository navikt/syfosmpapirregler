package no.nav.syfo.papirsykemelding.rules

import no.nav.syfo.model.Periode
import no.nav.syfo.model.Rule
import no.nav.syfo.model.Status
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.model.juridisk.JuridiskHenvisning
import no.nav.syfo.model.juridisk.Lovverk
import no.nav.syfo.papirsykemelding.model.RuleChain
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class PeriodLogicRuleChain(
    sykmelding: Sykmelding,
    metadata: RuleMetadata,
) : RuleChain {
    override val rules: List<Rule<*>> = listOf(
        // Sykmeldingen må inneholden en fom-dato og en tom-dato
        // Hvis ingen perioder er oppgitt skal sykmeldingen avvises.
        Rule(
            name = "PERIODER_MANGLER",
            ruleId = 1200,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "Det er ikke oppgitt hvilken periode sykmeldingen gjelder for.",
            messageForSender = "Hvis ingen perioder er oppgitt skal sykmeldingen avvises.",
            juridiskHenvisning = null,
            input = object {
                val perioder = sykmelding.perioder
            },
            predicate = { input ->
                input.perioder.isNullOrEmpty()
            }
        ),

        // fom-dato må være før tom-dato
        // Hvis tildato for en periode ligger før fradato avvises meldingen og hvilken periode det gjelder oppgis.
        Rule(
            name = "FRADATO_ETTER_TILDATO",
            ruleId = 1201,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "Det er lagt inn datoer som ikke stemmer innbyrdes.",
            messageForSender = "Hvis tildato for en periode ligger før fradato avvises meldingen og hvilken periode det gjelder oppgis.",
            juridiskHenvisning = null,
            input = object {
                val perioder = sykmelding.perioder
            },
            predicate = { input ->
                input.perioder.any { it.fom.isAfter(it.tom) }
            },
        ),

        // Hvis sykmeldingen inneholder flere perioden kan ikke periodene overlappe hverandre
        // Hvis en eller flere perioder er overlappende avvises meldingen og hvilken periode det gjelder oppgis.
        Rule(
            name = "OVERLAPPENDE_PERIODER",
            ruleId = 1202,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "Periodene må ikke overlappe hverandre.",
            messageForSender = "Hvis en eller flere perioder er overlappende avvises meldingen og hvilken periode det gjelder oppgis.",
            null,
            input = object {
                val perioder = sykmelding.perioder
            },
            predicate = { input ->
                input.perioder.any { periodA ->
                    input.perioder
                        .filter { periodB -> periodB != periodA }
                        .any { periodB ->
                            periodA.fom in periodB.range() || periodA.tom in periodB.range()
                        }
                }
            }
        ),

        // Det kan ikke være opphold mellom perioder i sykmeldingen
        // Hvis det finnes opphold mellom perioder i sykmeldingen avvises meldingen.
        Rule(
            name = "OPPHOLD_MELLOM_PERIODER",
            ruleId = 1203,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "Det er opphold mellom sykmeldingsperiodene.",
            messageForSender = "Hvis det finnes opphold mellom perioder i sykmeldingen avvises meldingen.",
            juridiskHenvisning = null,
            input = object {
                val periodeRanges = sykmelding.perioder
                    .sortedBy { it.fom }
                    .map { it.fom to it.tom }
            },
            predicate = { input ->
                var gapBetweenPeriods = false
                for (i in 1 until input.periodeRanges.size) {
                    gapBetweenPeriods =
                        workdaysBetween(input.periodeRanges[i - 1].second, input.periodeRanges[i].first) > 0
                    if (gapBetweenPeriods == true) {
                        break
                    }
                }
                gapBetweenPeriods
            }
        ),

        // Vi tar ikke imot sykmeldinger som ligger mer enn 3 år tilbake i tid
        // Sykmeldinges fom-dato er mer enn 3 år tilbake i tid.
        Rule(
            name = "TILBAKEDATERT_MER_ENN_3_AR",
            ruleId = 1206,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "Startdatoen er mer enn tre år tilbake.",
            messageForSender = "Sykmeldinges fom-dato er mer enn 3 år tilbake i tid.",
            null,
            input = object {
                val forsteFomDato = sykmelding.perioder.sortedFOMDate().firstOrNull()
            },
            predicate = { input ->
                when (input.forsteFomDato) {
                    null -> false
                    else -> input.forsteFomDato.atStartOfDay().isBefore(LocalDate.now().minusYears(3).atStartOfDay())
                }
            }
        ),

        // En sykmelding kan ikke fremdateres mer enn 30 dager
        // Hvis sykmeldingen er fremdatert mer enn 30 dager etter konsultasjonsdato/signaturdato avvises meldingen.
        Rule(
            name = "FREMDATERT",
            ruleId = 1209,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "Sykmeldingen er datert mer enn 30 dager fram i tid.",
            messageForSender = "Sykmeldingen er fremdatert mer enn 30 dager etter behandletDato",
            juridiskHenvisning = null,
            input = object {
                val forsteFomDato = sykmelding.perioder.sortedFOMDate().firstOrNull()
                val behandletTidspunkt = metadata.behandletTidspunkt
            },
            predicate = {
                when (it.forsteFomDato) {
                    null -> false
                    else -> it.forsteFomDato > it.behandletTidspunkt.plusDays(30).toLocalDate()
                }
            }
        ),

        // En sykmelding kan ikke vare mer enn 1 år
        // Hvis sykmeldingen første fom og siste tom har ein varighet som er over 1 år. avvises meldingen.
        Rule(
            name = "TOTAL_VARIGHET_OVER_ETT_AAR",
            ruleId = 1211,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "Den kan ikke ha en varighet på over ett år.",
            messageForSender = "Hvis sykmeldingens sluttdato er mer enn ett år frem i tid, avvises meldingen.",
            juridiskHenvisning = null,
            input = object {
                val forsteFomDato = sykmelding.perioder.sortedFOMDate().firstOrNull()
                val sisteTomDato = sykmelding.perioder.sortedTOMDate().lastOrNull()
            },
            predicate = {
                if (it.forsteFomDato == null || it.sisteTomDato == null) false
                else {
                    val firstFomDate = it.forsteFomDato.atStartOfDay().toLocalDate()
                    val lastFomDate = it.sisteTomDato.atStartOfDay().toLocalDate()
                    (firstFomDate..lastFomDate).daysBetween() > 365
                }
            }
        ),

        // Avventende sykmelding kan ikke kombineres med noe annet
        // Hvis avventende sykmelding er funnet og det finnes flere perioder
        Rule(
            name = "AVVENTENDE_SYKMELDING_KOMBINERT",
            ruleId = 9999,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "En avventende sykmelding kan bare inneholde én periode.",
            messageForSender = "Hvis avventende sykmelding er funnet og det finnes en eller flere perioder. ",
            juridiskHenvisning = null,
            input = object {
                val perioder = sykmelding.perioder
            },
            predicate = { input ->
                input.perioder.count { it.avventendeInnspillTilArbeidsgiver != null } != 0 &&
                    input.perioder.size > 1
            }
        ),

        // Avventende sykmelding må inneholde melding til arbeidsgiver om tilrettelegging
        // Hvis innspill til arbeidsgiver om tilrettelegging i pkt 4.1.3 ikke er utfylt ved avventende sykmelding avvises meldingen
        Rule(
            name = "MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER",
            ruleId = 1241,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "En avventende sykmelding forutsetter at du kan jobbe hvis arbeidsgiveren din legger til rette for det. Den som har sykmeldt deg har ikke foreslått hva arbeidsgiveren kan gjøre, noe som kreves for denne typen sykmelding.",
            messageForSender = "Hvis innspill til arbeidsgiver om tilrettelegging i pkt 4.1.3 ikke er utfylt ved avventende sykmelding avvises meldingen",
            juridiskHenvisning = null,
            input = object {
                val perioder = sykmelding.perioder
            },
            predicate = { input ->
                input.perioder.any {
                    it.avventendeInnspillTilArbeidsgiver != null &&
                        it.avventendeInnspillTilArbeidsgiver?.trim().isNullOrEmpty()
                }
            }
        ),

        // En avventende sykmelding kan ikke vare mer enn 16 dager
        // Hvis avventende sykmelding benyttes utover arbeidsgiverperioden på 16 kalenderdager, avvises meldingen.
        Rule(
            name = "AVVENTENDE_SYKMELDING_OVER_16_DAGER",
            ruleId = 1242,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "En avventende sykmelding kan bare gis for 16 dager.",
            messageForSender = "Hvis avventende sykmelding benyttes utover i arbeidsgiverperioden på 16 kalenderdager, avvises meldingen.",
            juridiskHenvisning = null,
            input = object {
                val perioder = sykmelding.perioder
            },
            predicate = { input ->
                input.perioder
                    .filter { it.avventendeInnspillTilArbeidsgiver != null }
                    .any { (it.fom..it.tom).daysBetween() > 16 }
            }
        ),

        // Sykmelding med behandlingsdager kan ha maks 1 behandlingsdag per uke
        // Hvis antall dager oppgitt for behandlingsdager periode er for høyt i forhold til periodens lengde avvises meldingen. Mer enn en dag per uke er for høyt. 1 dag per påbegynt uke.
        Rule(
            name = "FOR_MANGE_BEHANDLINGSDAGER_PER_UKE",
            ruleId = 1250,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "Det er angitt for mange behandlingsdager. Det kan bare angis én behandlingsdag per uke.",
            messageForSender = "Hvis antall dager oppgitt for behandlingsdager periode er for høyt i forhold til periodens lengde avvises meldingen. Mer enn en dag per uke er for høyt. 1 dag per påbegynt uke.",
            juridiskHenvisning = null,
            input = object {
                val perioder = sykmelding.perioder
            },
            predicate = { input ->
                input.perioder.any {
                    it.behandlingsdager != null && it.behandlingsdager!! > it.range().startedWeeksBetween()
                }
            }
        ),

        // §8-13: Dersom medlemmet er delvis arbeidsufør, kan det ytes graderte sykepenger.
        // Det er et vilkår at evnen til å utføre inntektsgivende arbeid er nedsatt med minst 20 prosent.
        // Hvis sykmeldingsgrad er mindre enn 20% for gradert sykmelding, avvises meldingen
        Rule(
            name = "GRADERT_SYKMELDING_UNDER_20_PROSENT",
            ruleId = 1251,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "Sykmeldingsgraden kan ikke være mindre enn 20 %.",
            messageForSender = "Hvis sykmeldingsgrad er mindre enn 20% for gradert sykmelding, avvises meldingen",
            juridiskHenvisning = JuridiskHenvisning(
                lovverk = Lovverk.FOLKETRYGDLOVEN,
                paragraf = "8-13",
                ledd = 1,
                punktum = null,
                bokstav = null
            ),
            input = object {
                val perioder = sykmelding.perioder
            },
            predicate = { input ->
                input.perioder.any {
                    it.gradert != null && it.gradert!!.grad < 20
                }
            }
        ),

        // Gradering kan ikke være høyere enn 99 prosent
        // Hvis sykmeldingsgrad er høyere enn 99% for delvis sykmelding avvises meldingen
        Rule(
            name = "GRADERT_SYKMELDING_OVER_99_PROSENT",
            ruleId = 1252,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "Sykmeldingsgraden kan ikke være mer enn 99% fordi det er en gradert sykmelding.",
            messageForSender = "Hvis sykmeldingsgrad er høyere enn 99% for delvis sykmelding avvises meldingen",
            juridiskHenvisning = null,
            input = object {
                val perioder = sykmelding.perioder
            },
            predicate = { input ->
                input.perioder.mapNotNull { it.gradert }.any { it.grad > 99 }
            }
        )
    )
}

fun List<Periode>.sortedFOMDate(): List<LocalDate> =
    map { it.fom }.sorted()

fun List<Periode>.sortedTOMDate(): List<LocalDate> =
    map { it.tom }.sorted()

fun workdaysBetween(a: LocalDate, b: LocalDate): Int = (1 until ChronoUnit.DAYS.between(a, b))
    .map { a.plusDays(it) }
    .filter { it.dayOfWeek !in arrayOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) }
    .count()

fun ClosedRange<LocalDate>.daysBetween(): Long = ChronoUnit.DAYS.between(start, endInclusive)
fun ClosedRange<LocalDate>.startedWeeksBetween(): Int = ChronoUnit.WEEKS.between(start, endInclusive).toInt() + 1
fun Periode.range(): ClosedRange<LocalDate> = fom.rangeTo(tom)
