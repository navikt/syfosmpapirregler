package no.nav.syfo.client.syketilfelle

import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.util.KtorExperimentalAPI
import no.nav.syfo.client.StsOidcClient
import no.nav.syfo.client.syketilfelle.model.Syketilfelle
import no.nav.syfo.helpers.retry

@KtorExperimentalAPI
class SyketilfelleClient(
    private val endpointUrl: String,
    private val stsClient: StsOidcClient,
    private val httpClient: HttpClient
) {

    suspend fun fetchErNytttilfelle(syketilfelleList: List<Syketilfelle>, aktorId: String): Boolean = retry(
        "ernytttilfelle",
        retryIntervals = arrayOf(500L, 1000L)
    ) {
        httpClient.post<Boolean>("$endpointUrl/oppfolgingstilfelle/ernytttilfelle/$aktorId") {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            val oidcToken = stsClient.oidcToken()
            headers {
                append("Authorization", "Bearer ${oidcToken.access_token}")
            }
            body = syketilfelleList
        }
    }
}
