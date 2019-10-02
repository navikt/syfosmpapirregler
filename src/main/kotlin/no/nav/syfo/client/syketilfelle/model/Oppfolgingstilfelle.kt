package no.nav.syfo.client.syketilfelle.model

data class Oppfolgingstilfelle(
    val antallBrukteDager: Int,
    val oppbruktArbeidsgvierperiode: Boolean,
    val arbeidsgiverperiode: Periode?
)
