package no.nav.syfo.papirsykemelding.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import no.nav.syfo.model.Periode

fun List<Periode>.sortedFOMDate(): List<LocalDate> = map { it.fom }.sorted()

fun List<Periode>.sortedTOMDate(): List<LocalDate> = map { it.tom }.sorted()

fun ClosedRange<LocalDate>.daysBetween(): Long = ChronoUnit.DAYS.between(start, endInclusive)

fun workdaysBetween(a: LocalDate, b: LocalDate): Int =
    (1..(ChronoUnit.DAYS.between(a, b) - 1))
        .map { a.plusDays(it) }
        .filter { it.dayOfWeek !in arrayOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) }
        .count()

fun ClosedRange<LocalDate>.startedWeeksBetween(): Int =
    ChronoUnit.WEEKS.between(start, endInclusive).toInt() + 1

fun Periode.range(): ClosedRange<LocalDate> = fom.rangeTo(tom)
