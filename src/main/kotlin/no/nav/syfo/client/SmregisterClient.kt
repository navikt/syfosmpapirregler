package no.nav.syfo.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import net.logstash.logback.argument.StructuredArguments.fields
import no.nav.syfo.log
import no.nav.syfo.model.Periode
import java.time.LocalDate
import java.time.OffsetDateTime
import no.nav.syfo.papirsykemelding.model.LoggingMeta

class SmregisterClient(
    private val smregisterEndpointURL: String,
    private val accessTokenClientV2: AccessTokenClientV2,
    private val scope: String,
    private val httpClient: HttpClient
) {

    suspend fun harOverlappendeSykmelding(
        fnr: String,
        periodeliste: List<Periode>,
        diagnosekode: String?,
        loggingMeta: LoggingMeta
    ): Boolean {
        if (periodeliste.size > 1) {
            log.info("Flere perioder i periodelisten returnerer false {}", fields(loggingMeta))
            return false
        }
        if (diagnosekode.isNullOrEmpty()) {
            log.info("Diagnosekode mangler {}", fields(loggingMeta))
            return false
        }
        val periode = periodeliste.first()
        val sykmeldinger = hentSykmeldinger(fnr)
        sykmeldinger.filter {
            it.behandlingsutfall.status == RegelStatusDTO.OK &&
                it.medisinskVurdering?.hovedDiagnose?.kode == diagnosekode &&
                it.behandletTidspunkt.toLocalDate() <= periode.fom.plusDays(8)
        }.forEach { sykmelding ->
            if (sykmelding.sykmeldingsperioder.any { p ->
                p.fom == periode.fom &&
                    p.tom == periode.tom &&
                    p.gradert?.grad == periode.gradert?.grad &&
                    p.type == periode.tilPeriodetypeDTO()
            }
            ) {
                log.info("Fant tidligere innsendt sykmelding ${sykmelding.id} {}", fields(loggingMeta))
                return true
            }
        }
        return false
    }

    private suspend fun hentSykmeldinger(fnr: String): List<SykmeldingDTO> =
        httpClient.get("$smregisterEndpointURL/api/v2/sykmelding/sykmeldinger") {
            accept(ContentType.Application.Json)
            val accessToken = accessTokenClientV2.getAccessTokenV2(scope)
            if (accessToken == null) {
                throw RuntimeException("Klarte ikke hente ut accessToken for smregister")
            }
            headers {
                append("Authorization", "Bearer ${accessToken}")
                append("fnr", fnr)
            }
        }.body()
}

data class SykmeldingDTO(
    val id: String,
    val behandlingsutfall: BehandlingsutfallDTO,
    val sykmeldingsperioder: List<SykmeldingsperiodeDTO>,
    val behandletTidspunkt: OffsetDateTime,
    val medisinskVurdering: MedisinskVurderingDTO?
)

data class SykmeldingsperiodeDTO(
    val fom: LocalDate,
    val tom: LocalDate,
    val gradert: GradertDTO?,
    val type: PeriodetypeDTO
)

data class MedisinskVurderingDTO(
    val hovedDiagnose: DiagnoseDTO?
)

data class DiagnoseDTO(
    val kode: String
)

data class GradertDTO(
    val grad: Int,
    val reisetilskudd: Boolean
)

enum class PeriodetypeDTO {
    AKTIVITET_IKKE_MULIG,
    AVVENTENDE,
    BEHANDLINGSDAGER,
    GRADERT,
    REISETILSKUDD
}

data class BehandlingsutfallDTO(
    val status: RegelStatusDTO
)

enum class RegelStatusDTO {
    OK, MANUAL_PROCESSING, INVALID
}

fun List<SykmeldingsperiodeDTO>.sortedFOMDate(): List<LocalDate> =
    map { it.fom }.sorted()

fun List<SykmeldingsperiodeDTO>.sortedTOMDate(): List<LocalDate> =
    map { it.tom }.sorted()

fun SykmeldingsperiodeDTO.periodelisteInneholderSammeType(perioder: List<Periode>): Boolean {
    val periodetyper = perioder.mapNotNull { it.tilPeriodetypeDTO() }.distinct()

    return if (periodetyper.contains(type) && type != PeriodetypeDTO.GRADERT) {
        true
    } else if (periodetyper.contains(type) && type == PeriodetypeDTO.GRADERT) {
        val sykmeldingsgrader = perioder.mapNotNull { it.gradert?.grad }
        sykmeldingsgrader.contains(gradert?.grad)
    } else {
        false
    }
}

fun Periode.tilPeriodetypeDTO(): PeriodetypeDTO? {
    return when {
        aktivitetIkkeMulig != null -> return PeriodetypeDTO.AKTIVITET_IKKE_MULIG
        gradert != null -> return PeriodetypeDTO.GRADERT
        reisetilskudd -> return PeriodetypeDTO.REISETILSKUDD
        avventendeInnspillTilArbeidsgiver != null -> return PeriodetypeDTO.AVVENTENDE
        behandlingsdager != null && behandlingsdager!! > 0 -> return PeriodetypeDTO.BEHANDLINGSDAGER
        else -> null
    }
}
