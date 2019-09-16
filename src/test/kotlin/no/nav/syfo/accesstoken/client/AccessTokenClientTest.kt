package no.nav.syfo.accesstoken.client

import io.ktor.client.features.ClientRequestException
import io.ktor.http.HttpStatusCode
import java.time.Instant
import kotlin.test.assertFailsWith
import kotlinx.coroutines.runBlocking
import no.nav.syfo.ResponseData
import no.nav.syfo.ResponseHandler
import no.nav.syfo.accesstoken.model.AadAccessToken
import no.nav.syfo.getHttpClient
import no.nav.syfo.getStringValue
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class AccessTokenClientTest : Spek({
    val responseHandler = ResponseHandler()
    val httpClient = getHttpClient(responseHandler)
    val accessTokenClient = AccessTokenClient("url", "1", "2", httpClient)
    describe("Testing AccessTokenClient") {
        it("Should get valid token") {
            responseHandler.updateResponseData(
                ResponseData(
                    getStringValue(
                        AadAccessToken(
                            "token1",
                            Instant.now().plusSeconds(200)
                        )
                    )
                )
            )
            runBlocking {
                val token = accessTokenClient.hentAccessToken("resource1")
                token.access_token shouldEqual "token1"
            }
        }
        it("Should get 401 when inncorrect secret") {
            responseHandler.updateResponseData(
                ResponseData(statusCode = HttpStatusCode.Unauthorized)
            )
            runBlocking {
                assertFailsWith<ClientRequestException>() {
                    accessTokenClient.hentAccessToken("resouce1")
                }
            }
        }
    }
})
