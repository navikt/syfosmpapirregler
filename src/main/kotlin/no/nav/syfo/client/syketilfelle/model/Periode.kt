package no.nav.syfo.client.syketilfelle.model

import java.time.LocalDate

data class Periode(
    val fom: LocalDate,
    val tom: LocalDate
)
