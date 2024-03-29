package no.nav.syfo.client.norskhelsenett

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import java.io.IOException
import java.time.LocalDateTime
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.client.AccessTokenClientV2
import no.nav.syfo.helpers.retry
import no.nav.syfo.papirsykemelding.model.LoggingMeta
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class NorskHelsenettClient(
    private val endpointUrl: String,
    private val accessTokenClient: AccessTokenClientV2,
    private val resourceId: String,
    private val httpClient: HttpClient,
) {
    private val log: Logger = LoggerFactory.getLogger(NorskHelsenettClient::class.java)

    suspend fun finnBehandler(
        behandlerFnr: String,
        msgId: String,
        loggingMeta: LoggingMeta
    ): Behandler? =
        retry(
            callName = "finnbehandler",
            retryIntervals = arrayOf(500L, 1000L, 1000L),
        ) {
            log.info("Henter behandler fra syfohelsenettproxy for msgId {}", msgId)
            val httpResponse =
                httpClient.get("$endpointUrl/api/v2/behandler") {
                    accept(ContentType.Application.Json)
                    val accessToken = accessTokenClient.getAccessTokenV2(resourceId)
                    headers {
                        append("Authorization", "Bearer $accessToken")
                        append("Nav-CallId", msgId)
                        append("behandlerFnr", behandlerFnr)
                    }
                }
            when (httpResponse.status) {
                InternalServerError -> {
                    log.error(
                        "Syfohelsenettproxy svarte med feilmelding for msgId {}, {}",
                        msgId,
                        StructuredArguments.fields(loggingMeta),
                    )
                    throw IOException("Syfohelsenettproxy svarte med feilmelding for $msgId")
                }
                HttpStatusCode.BadRequest -> {
                    log.error(
                        "BehandlerFnr mangler i request for msgId {}, {}",
                        msgId,
                        StructuredArguments.fields(loggingMeta),
                    )
                    return@retry null
                }
                NotFound -> {
                    log.warn(
                        "BehandlerFnr ikke funnet {}, {}",
                        msgId,
                        StructuredArguments.fields(loggingMeta)
                    )
                    return@retry null
                }
                else -> {
                    log.info(
                        "Hentet behandler for msgId {}, {}",
                        msgId,
                        StructuredArguments.fields(loggingMeta)
                    )
                    httpResponse.call.response.body<Behandler>()
                }
            }
        }
}

data class Behandler(
    val godkjenninger: List<Godkjenning>,
    val hprNummer: Int? = null,
)

data class Godkjenning(
    val helsepersonellkategori: Kode? = null,
    val autorisasjon: Kode? = null,
    val tillegskompetanse: List<Tilleggskompetanse>? = null,
)

data class Tilleggskompetanse(
    val avsluttetStatus: Kode?,
    val eTag: String?,
    val gyldig: Periode?,
    val id: Int?,
    val type: Kode?
)

data class Periode(val fra: LocalDateTime?, val til: LocalDateTime?)

data class Kode(
    val aktiv: Boolean,
    val oid: Int,
    val verdi: String?,
)
