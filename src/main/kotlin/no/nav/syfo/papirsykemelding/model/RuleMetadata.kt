package no.nav.syfo.papirsykemelding.model

import java.time.LocalDate
import java.time.LocalDateTime

data class RuleMetadata(
    val signatureDate: LocalDateTime,
    val receivedDate: LocalDateTime,
    val behandletTidspunkt: LocalDateTime,
    val patientPersonNumber: String,
    val rulesetVersion: String?,
    val legekontorOrgnr: String?,
    val tssid: String?,
    val pasientFodselsdato: LocalDate,
)
