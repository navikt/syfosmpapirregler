package no.nav.syfo.client.syketilfelle

import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.util.KtorExperimentalAPI
import java.time.LocalDate
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.client.StsOidcClient
import no.nav.syfo.log
import no.nav.syfo.model.Periode
import no.nav.syfo.papirsykemelding.model.LoggingMeta
import no.nav.syfo.papirsykemelding.model.sortedFOMDate
import no.nav.syfo.papirsykemelding.model.sortedTOMDate

@KtorExperimentalAPI
class SyketilfelleClient(
    private val endpointUrl: String,
    private val stsClient: StsOidcClient,
    private val httpClient: HttpClient
) {

    suspend fun finnStartdatoForSammenhengendeSyketilfelle(aktorId: String, periodeliste: List<Periode>, loggingMeta: LoggingMeta): LocalDate? {
        log.info("Sjekker om nytt syketilfelle mot syfosyketilfelle {}", StructuredArguments.fields(loggingMeta))
        val sykeforloep = hentSykeforloep(aktorId)

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

    private suspend fun hentSykeforloep(aktorId: String): List<Sykeforloep> =
        httpClient.get<List<Sykeforloep>>("$endpointUrl/sparenaproxy/$aktorId/sykeforloep") {
            accept(ContentType.Application.Json)
            val oidcToken = stsClient.oidcToken()
            headers {
                append("Authorization", "Bearer ${oidcToken.access_token}")
            }
        }
}

data class Sykeforloep(
    var oppfolgingsdato: LocalDate,
    val sykmeldinger: List<SimpleSykmelding>
)

data class SimpleSykmelding(
    val id: String,
    val fom: LocalDate,
    val tom: LocalDate
)
