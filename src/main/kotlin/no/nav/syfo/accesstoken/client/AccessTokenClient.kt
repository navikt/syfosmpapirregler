package no.nav.syfo.accesstoken.client

import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import no.nav.syfo.accesstoken.model.AadAccessToken

class AccessTokenClient(
    private val aadAccessTokenUrl: String,
    private val clientId: String,
    private val clientSecret: String,
    private val httpClient: HttpClient
) {
    suspend fun hentAccessToken(resource: String): AadAccessToken {
        return httpClient.post(aadAccessTokenUrl) {
            accept(ContentType.Application.Json)
            method = HttpMethod.Post
            body = FormDataContent(Parameters.build {
                append("client_id", clientId)
                append("resource", resource)
                append("grant_type", "client_credentials")
                append("client_secret", clientSecret)
            })
        }
    }
}
