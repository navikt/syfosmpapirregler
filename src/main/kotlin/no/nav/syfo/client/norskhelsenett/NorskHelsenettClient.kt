package no.nav.syfo.client.norskhelsenett

import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpStatement
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.util.KtorExperimentalAPI
import java.io.IOException
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.accesstoken.service.AccessTokenService
import no.nav.syfo.helpers.retry
import no.nav.syfo.papirsykemelding.model.LoggingMeta
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@KtorExperimentalAPI
class NorskHelsenettClient(
    private val endpointUrl: String,
    private val accessTokenClient: AccessTokenService,
    private val resourceId: String,
    private val httpClient: HttpClient
) {
    private val log: Logger = LoggerFactory.getLogger(NorskHelsenettClient::class.java)

    suspend fun finnBehandler(behandlerFnr: String, msgId: String, loggingMeta: LoggingMeta): Behandler? = retry(
        callName = "finnbehandler",
        retryIntervals = arrayOf(500L, 1000L, 1000L)
    ) {
        log.info("Henter behandler fra syfohelsenettproxy for msgId {}", msgId)
        val httpResponse = httpClient.get<HttpStatement>("$endpointUrl/api/behandler") {
            accept(ContentType.Application.Json)
            val accessToken = accessTokenClient.getAccessToken(resourceId)
            headers {
                append("Authorization", "Bearer $accessToken")
                append("Nav-CallId", msgId)
                append("behandlerFnr", behandlerFnr)
            }
        }.execute()
        when (httpResponse.status) {
            InternalServerError -> {
                log.error(
                    "Syfohelsenettproxy svarte med feilmelding for msgId {}, {}", msgId,
                    StructuredArguments.fields(loggingMeta)
                )
                throw IOException("Syfohelsenettproxy svarte med feilmelding for $msgId")
            }

            HttpStatusCode.BadRequest -> {
                log.error(
                    "BehandlerFnr mangler i request for msgId {}, {}", msgId,
                    StructuredArguments.fields(loggingMeta)
                )
                return@retry null
            }

            NotFound -> {
                log.warn("BehandlerFnr ikke funnet {}, {}", msgId, StructuredArguments.fields(loggingMeta))
                return@retry null
            }
            else -> {
                log.info("Hentet behandler for msgId {}, {}", msgId, StructuredArguments.fields(loggingMeta))
                httpResponse.call.response.receive<Behandler>()
            }
        }
    }
}

data class Behandler(
    val godkjenninger: List<Godkjenning>
)

data class Godkjenning(
    val helsepersonellkategori: Kode? = null,
    val autorisasjon: Kode? = null
)

data class Kode(
    val aktiv: Boolean,
    val oid: Int,
    val verdi: String?
)
