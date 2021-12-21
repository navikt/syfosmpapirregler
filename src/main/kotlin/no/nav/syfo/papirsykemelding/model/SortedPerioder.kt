package no.nav.syfo.papirsykemelding.model

import no.nav.syfo.model.Periode
import java.time.LocalDate

fun List<Periode>.sortedFOMDate(): List<LocalDate> =
    map { it.fom }.sorted()

fun List<Periode>.sortedTOMDate(): List<LocalDate> =
    map { it.tom }.sorted()
