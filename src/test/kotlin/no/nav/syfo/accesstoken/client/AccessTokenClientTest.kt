package no.nav.syfo.accesstoken.client

import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.ClientRequestException
import io.ktor.client.request.HttpRequest
import io.ktor.client.response.DefaultHttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.util.InternalAPI
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkClass
import java.time.Instant
import kotlin.test.assertFailsWith
import kotlinx.coroutines.runBlocking
import no.nav.syfo.accesstoken.model.AadAccessToken
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

@InternalAPI
class AccessTokenClientTest : Spek({

    val httpClientCall = mockkClass(HttpClientCall::class)
    val httpClient = mockkClass(HttpClient::class)

    val accessTokenClient = AccessTokenClient("url", "1", "2", httpClient)

    beforeEachTest {
        coEvery { httpClient.execute(any()) } returns httpClientCall
    }

    describe("Testing AccessTokenClient") {
        it("Should get valid token") {
            coEvery { httpClientCall.receive(any()) } returns AadAccessToken("token1", Instant.now().plusSeconds(200))
            runBlocking {
                val token = accessTokenClient.hentAccessToken("resource1")
                token.access_token shouldEqual "token1"
            }
        }
        it("Should get 401 when inncorrect secret") {
            val mockRequest = mockkClass(HttpRequest::class)
            coEvery { httpClientCall.request } returns mockRequest
            every { mockRequest.url } returns Url("test")
            coEvery { httpClient.execute(any()) } throws ClientRequestException(DefaultHttpResponse(httpClientCall, respond("401 Unautorized", HttpStatusCode.Unauthorized)))

            runBlocking {
                assertFailsWith<ClientRequestException>() {
                    accessTokenClient.hentAccessToken("resouce1")
                }
            }
        }
    }
})
