package no.nav.syfo.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import no.nav.syfo.client.SykmeldingsperiodeDTO

fun List<SykmeldingsperiodeDTO>.sortedFOMDate(): List<LocalDate> = map { it.fom }.sorted()

fun List<SykmeldingsperiodeDTO>.sortedTOMDate(): List<LocalDate> = map { it.tom }.sorted()

fun allDaysBetween(fom: LocalDate, tom: LocalDate): List<LocalDate> {
    return (0..ChronoUnit.DAYS.between(fom, tom)).map { fom.plusDays(it) }
}

fun isWorkingDaysBetween(firstFom: LocalDate, periodeTom: LocalDate): Boolean {
    val daysBetween = ChronoUnit.DAYS.between(periodeTom, firstFom).toInt()
    if (daysBetween < 0) return true
    return when (firstFom.dayOfWeek) {
        DayOfWeek.MONDAY -> daysBetween > 3
        DayOfWeek.SUNDAY -> daysBetween > 2
        else -> daysBetween > 1
    }
}
