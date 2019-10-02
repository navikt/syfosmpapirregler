
import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.call.receive
import io.ktor.client.engine.mock.respond
import io.ktor.client.response.DefaultHttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.util.InternalAPI
import io.ktor.util.KtorExperimentalAPI
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkClass
import io.mockk.mockkStatic
import java.io.IOException
import kotlin.test.assertFailsWith
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.syfo.accesstoken.service.AccessTokenService
import no.nav.syfo.client.norskhelsenett.Behandler
import no.nav.syfo.client.norskhelsenett.Godkjenning
import no.nav.syfo.client.norskhelsenett.NorskHelsenettClient
import no.nav.syfo.getStringValue
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

private val fnr = "12345647981"

@InternalAPI
@KtorExperimentalAPI
class NorskHelsenettClientTest : Spek({
    val accessTokenService = mockkClass(AccessTokenService::class)
    val httpClientCall = mockkClass(HttpClientCall::class)
    val httpClient = mockkClass(HttpClient::class)

    mockkStatic("kotlinx.coroutines.DelayKt")
    coEvery { delay(any()) } returns Unit

    val norskHelsenettClient =
        NorskHelsenettClient("url", accessTokenService, "resource", httpClient)

    beforeEachTest {
        clearMocks(httpClient, httpClientCall)
        coEvery { httpClient.execute(any()) } returns httpClientCall
        coEvery { httpClientCall.response.receive<Behandler>() } returns Behandler(listOf(Godkjenning()))
    }

    coEvery { accessTokenService.getAccessToken(any()) } returns "token"
    describe("Test NorskHelsenettClient") {
        it("Should get behandler ") {
            coEvery { httpClientCall.receive(any()) } returns getDefaultResponse(httpClientCall)
            coEvery { httpClientCall.response.receive<Behandler>() } returns Behandler(listOf(Godkjenning()))

            runBlocking {
                val behandler = norskHelsenettClient.finnBehandler(fnr, "1")
                behandler shouldEqual Behandler(listOf(Godkjenning()))
            }
        }

        it("Should get null when 404") {
            coEvery { httpClientCall.receive(any()) } returns
                    DefaultHttpResponse(httpClientCall, respond("Not Found", HttpStatusCode.NotFound))
            runBlocking {
                val behandler = norskHelsenettClient.finnBehandler(fnr, "1")
                behandler shouldEqual null
            }
        }
    }

    describe("Test retry") {
        it("Should retry when getting internal server error") {
            coEvery { httpClientCall.receive(any()) } returns DefaultHttpResponse(httpClientCall, respond("Internal Server Error", HttpStatusCode.InternalServerError)) andThen
                    getDefaultResponse(httpClientCall)

            runBlocking {
                val behandler = norskHelsenettClient.finnBehandler(fnr, "1")
                behandler shouldEqual Behandler(listOf(Godkjenning()))
                coVerify(exactly = 2) { httpClient.execute(any()) }
            }
        }

        it("should throw exception when exceeds max retires") {
            coEvery { httpClientCall.receive(any()) } returns DefaultHttpResponse(httpClientCall, respond("Internal Server Error", HttpStatusCode.InternalServerError))
            runBlocking {
                assertFailsWith<IOException> { norskHelsenettClient.finnBehandler(fnr, "1") }
                coVerify(exactly = 3) { httpClient.execute(any()) }
            }
        }
    }
})

@InternalAPI
private fun getDefaultResponse(httpClientCall: HttpClientCall) =
    DefaultHttpResponse(
        httpClientCall, respond(
            getStringValue(
                Behandler(
                    listOf(Godkjenning())
                )
            ), HttpStatusCode.OK
        )
    )
