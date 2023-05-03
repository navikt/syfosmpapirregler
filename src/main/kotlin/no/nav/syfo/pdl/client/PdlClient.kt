package no.nav.syfo.pdl.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import no.nav.syfo.client.AccessTokenClientV2
import no.nav.syfo.pdl.client.model.GetPersonRequest
import no.nav.syfo.pdl.client.model.GetPersonVariables
import no.nav.syfo.pdl.client.model.GraphQLResponse
import no.nav.syfo.pdl.client.model.PdlResponse

class PdlClient(
    private val httpClient: HttpClient,
    private val accessTokenClientV2: AccessTokenClientV2,
    private val pdlScope: String,
    private val basePath: String,
    private val graphQlQuery: String,
) {

    private val temaHeader = "TEMA"
    private val tema = "SYM"

    suspend fun getPerson(fnr: String): GraphQLResponse<PdlResponse> {
        val token = accessTokenClientV2.getAccessTokenV2(pdlScope)
        val getPersonRequest = GetPersonRequest(query = graphQlQuery, variables = GetPersonVariables(ident = fnr))
        return httpClient.post(basePath) {
            setBody(getPersonRequest)
            header(HttpHeaders.Authorization, "Bearer $token")
            header(temaHeader, tema)
            header(HttpHeaders.ContentType, "application/json")
        }.body()
    }
}
