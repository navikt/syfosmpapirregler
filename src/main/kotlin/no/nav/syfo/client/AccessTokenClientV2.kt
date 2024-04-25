package no.nav.syfo.client

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import java.time.Instant
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.nav.syfo.logger

class AccessTokenClientV2(
    private val aadAccessTokenUrl: String,
    private val clientId: String,
    private val clientSecret: String,
    private val httpClient: HttpClient,
) {
    private val mutex = Mutex()

    @Volatile
    private var tokenMap = HashMap<String, AadAccessTokenMedExpiry>()

    suspend fun getAccessTokenV2(resource: String): String {
        val omToMinutter = Instant.now().plusSeconds(120L)

        return mutex.withLock {
            val token = tokenMap[resource];
            if (token != null && !token.expiresOn.isBefore(omToMinutter)) {
                return token.access_token
            }

            logger.debug("Henter nytt token fra Azure AD")
            val response = exchangeToken(resource)

            if (!(200..299).contains(response.status.value)) {
                throw RuntimeException("Failed to get access token from Azure AD: ${response.status}, text: ${response.body<String>()}")
            }

            val result: AadAccessTokenV2 = response.body()
            val tokenMedExpiry = AadAccessTokenMedExpiry(
                access_token = result.access_token,
                expires_in = result.expires_in,
                expiresOn = Instant.now().plusSeconds(result.expires_in.toLong()),
            )

            tokenMap[resource] = tokenMedExpiry
            logger.debug("Har hentet accesstoken")
            tokenMedExpiry.access_token
        }
    }

    private suspend fun exchangeToken(resource: String) =
        httpClient
            .post(aadAccessTokenUrl) {
                accept(ContentType.Application.Json)
                setBody(
                    FormDataContent(
                        Parameters.build {
                            append("client_id", clientId)
                            append("scope", resource)
                            append("grant_type", "client_credentials")
                            append("client_secret", clientSecret)
                        },
                    ),
                )
            }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class AadAccessTokenV2(
    val access_token: String,
    val expires_in: Int,
)

data class AadAccessTokenMedExpiry(
    val access_token: String,
    val expires_in: Int,
    val expiresOn: Instant,
)
