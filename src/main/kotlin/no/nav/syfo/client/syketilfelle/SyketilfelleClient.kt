package no.nav.syfo.client.syketilfelle

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.client.AccessTokenClientV2
import no.nav.syfo.log
import no.nav.syfo.model.Periode
import no.nav.syfo.papirsykemelding.model.LoggingMeta
import no.nav.syfo.papirsykemelding.model.sortedFOMDate
import no.nav.syfo.papirsykemelding.model.sortedTOMDate
import java.time.LocalDate

class SyketilfelleClient(
    private val endpointUrl: String,
    private val accessTokenClient: AccessTokenClientV2,
    private val resourceId: String,
    private val httpClient: HttpClient,
) {

    suspend fun finnStartdatoForSammenhengendeSyketilfelle(fnr: String, periodeliste: List<Periode>, loggingMeta: LoggingMeta): LocalDate? {
        log.info("Sjekker om nytt syketilfelle mot syfosyketilfelle {}", StructuredArguments.fields(loggingMeta))
        val sykeforloep = hentSykeforloep(fnr)

        return finnStartdato(sykeforloep, periodeliste, loggingMeta)
    }

    fun finnStartdato(sykeforloep: List<Sykeforloep>, periodeliste: List<Periode>, loggingMeta: LoggingMeta): LocalDate? {
        if (sykeforloep.isEmpty()) {
            return null
        }
        val forsteFomIMottattSykmelding = periodeliste.sortedFOMDate().firstOrNull()
        val sisteTomIMottattSykmelding = periodeliste.sortedTOMDate().lastOrNull()
        if (forsteFomIMottattSykmelding == null || sisteTomIMottattSykmelding == null) {
            log.warn("Mangler fom eller tom for sykmeldingsperioder: {}", StructuredArguments.fields(loggingMeta))
            return null
        }
        val periodeRange = forsteFomIMottattSykmelding.rangeTo(sisteTomIMottattSykmelding)
        val sammeSykeforloep = sykeforloep.firstOrNull {
            it.sykmeldinger.any { simpleSykmelding -> simpleSykmelding.erSammeOppfolgingstilfelle(periodeRange) }
        }
        return sammeSykeforloep?.oppfolgingsdato
    }

    private fun SimpleSykmelding.erSammeOppfolgingstilfelle(periodeRange: ClosedRange<LocalDate>): Boolean {
        if (fom.minusDays(16) in periodeRange || tom.plusDays(16) in periodeRange) {
            return true
        }
        return false
    }

    private suspend fun hentSykeforloep(fnr: String): List<Sykeforloep> =
        httpClient.get("$endpointUrl/api/v1/sykeforloep?inkluderPapirsykmelding=true") {
            accept(ContentType.Application.Json)
            val accessToken = accessTokenClient.getAccessTokenV2(resourceId)
            headers {
                append("Authorization", "Bearer $accessToken")
                append("fnr", fnr)
            }
        }.body()
}

data class Sykeforloep(
    var oppfolgingsdato: LocalDate,
    val sykmeldinger: List<SimpleSykmelding>,
)

data class SimpleSykmelding(
    val id: String,
    val fom: LocalDate,
    val tom: LocalDate,
)
