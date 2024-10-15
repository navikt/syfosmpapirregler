package no.nav.syfo.client.legesuspensjon

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import no.nav.syfo.client.AccessTokenClientV2
import no.nav.syfo.client.legesuspensjon.model.Suspendert
import no.nav.syfo.helpers.retry
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LegeSuspensjonClient(
    private val endpointUrl: String,
    private val accessTokenClientV2: AccessTokenClientV2,
    private val httpClient: HttpClient,
    private val scope: String,
) {

    suspend fun checkTherapist(
        therapistId: String,
        ediloggid: String,
        oppslagsdato: String
    ): Suspendert =
        retry(
            "lege_suspansjon",
            retryIntervals = arrayOf(500L, 1000L),
        ) {
            val log: Logger = LoggerFactory.getLogger(LegeSuspensjonClient::class.java)

            val httpResponse =
                httpClient.get("$endpointUrl/api/v1/suspensjon/status") {
                    accept(ContentType.Application.Json)
                    val accessToken = accessTokenClientV2.getAccessTokenV2(scope)
                    headers {
                        append("Nav-Call-Id", ediloggid)
                        append("Nav-Consumer-Id", "srvsyfosmpapirregler")
                        append("Nav-Personident", therapistId)

                        append("Authorization", "Bearer $accessToken")
                    }
                    parameter("oppslagsdato", oppslagsdato)
                }

            when (httpResponse.status) {
                HttpStatusCode.OK -> {
                    log.info("Hentet supensjonstatus for ediloggId {}", ediloggid)
                    httpResponse.call.response.body<Suspendert>()
                }
                else -> {
                    log.error(
                        "Btsys svarte med kode {} for ediloggId {}",
                        httpResponse.status,
                        ediloggid
                    )
                    throw kotlinx.io.IOException(
                        "Btsys svarte med uventet kode ${httpResponse.status} for $ediloggid"
                    )
                }
            }
        }
}
