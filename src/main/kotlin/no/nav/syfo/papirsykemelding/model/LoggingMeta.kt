package no.nav.syfo.papirsykemelding.model

data class LoggingMeta(
    val mottakId: String,
    val orgNr: String?,
    val msgId: String,
    val sykmeldingId: String,
)
