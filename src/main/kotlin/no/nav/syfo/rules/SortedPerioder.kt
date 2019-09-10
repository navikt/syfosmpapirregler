package no.nav.syfo.rules

import java.time.LocalDate
import no.nav.syfo.model.Periode

fun List<Periode>.sortedFOMDate(): List<LocalDate> =
    map { it.fom }.sorted()

fun List<Periode>.sortedTOMDate(): List<LocalDate> =
    map { it.tom }.sorted()
