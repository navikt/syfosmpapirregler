package no.nav.syfo

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.syfo.client.norskhelsenett.Behandler
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.papirsykemelding.service.BehandlerOgStartdato
import no.nav.syfo.papirsykemelding.service.RuleMetadataSykmelding
import no.nav.syfo.papirsykemelding.service.SykmeldingMetadataInfo
import java.time.LocalDate
import java.time.LocalDateTime

val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule()).registerModule(
    KotlinModule.Builder()
        .withReflectionCacheSize(512)
        .configure(KotlinFeature.NullToEmptyCollection, false)
        .configure(KotlinFeature.NullToEmptyMap, false)
        .configure(KotlinFeature.NullIsSameAsDefault, false)
        .configure(KotlinFeature.StrictNullChecks, false)
        .build(),
)

fun <T> getStringValue(content: T): String {
    return objectMapper.writeValueAsString(content)
}

fun Sykmelding.toRuleMetadata(
    signatureDate: LocalDateTime = signaturDato,
    receivedDate: LocalDateTime = signaturDato,
) = RuleMetadata(
    signatureDate = signatureDate,
    receivedDate = receivedDate,
    behandletTidspunkt = behandletTidspunkt,
    patientPersonNumber = "1",
    rulesetVersion = null,
    legekontorOrgnr = null,
    tssid = null,
    pasientFodselsdato = LocalDate.now(),
)

fun ruleMetadataSykmelding(
    ruleMetadata: RuleMetadata,
    erNyttSyketilfelle: Boolean = false,
) =
    RuleMetadataSykmelding(
        ruleMetadata = ruleMetadata,
        erNyttSyketilfelle = erNyttSyketilfelle,
        doctorSuspensjon = false,
        behandlerOgStartdato = BehandlerOgStartdato(Behandler(emptyList(), null), null),
        sykmeldingMetadataInfo = SykmeldingMetadataInfo(null, emptyList()),
    )
