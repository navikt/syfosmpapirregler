package no.nav.syfo.client.legesuspensjon

import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpStatement
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.util.KtorExperimentalAPI
import io.ktor.utils.io.errors.IOException
import no.nav.syfo.VaultCredentials
import no.nav.syfo.client.StsOidcClient
import no.nav.syfo.client.legesuspensjon.model.Suspendert
import no.nav.syfo.helpers.retry
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@KtorExperimentalAPI
class LegeSuspensjonClient(
    private val endpointUrl: String,
    private val credentials: VaultCredentials,
    private val stsClient: StsOidcClient,
    private val httpClient: HttpClient
) {

    suspend fun checkTherapist(therapistId: String, ediloggid: String, oppslagsdato: String): Suspendert = retry(
        "lege_suspansjon",
        retryIntervals = arrayOf(500L, 1000L)
    ) {

        val log: Logger = LoggerFactory.getLogger(LegeSuspensjonClient::class.java)

        val httpResponse = httpClient.get<HttpStatement>("$endpointUrl/api/v1/suspensjon/status") {
            accept(ContentType.Application.Json)
            val oidcToken = stsClient.oidcToken()
            headers {
                append("Nav-Call-Id", ediloggid)
                append("Nav-Consumer-Id", credentials.serviceuserUsername)
                append("Nav-Personident", therapistId)

                append("Authorization", "Bearer ${oidcToken.access_token}")
            }
            parameter("oppslagsdato", oppslagsdato)
        }.execute()

        when (httpResponse.status) {
            HttpStatusCode.OK -> {
                log.info("Hentet supensjonstatus for ediloggId {}", ediloggid)
                httpResponse.call.response.receive<Suspendert>()
            }
            else -> {
                log.error("Btsys svarte med kode {} for ediloggId {}, {}", httpResponse.status, ediloggid)
                throw IOException("Btsys svarte med uventet kode ${httpResponse.status} for $ediloggid")
            }
        }
    }
}
