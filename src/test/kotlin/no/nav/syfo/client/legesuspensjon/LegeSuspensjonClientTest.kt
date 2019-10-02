import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
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
import no.nav.syfo.VaultCredentials
import no.nav.syfo.client.OidcToken
import no.nav.syfo.client.StsOidcClient
import no.nav.syfo.client.legesuspensjon.LegeSuspensjonClient
import no.nav.syfo.client.legesuspensjon.model.Suspendert
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

@KtorExperimentalAPI
class LegeSuspensjonClientTest : Spek({

    val stsOidcClient = mockkClass(StsOidcClient::class)
    val clientSecret = "secret"

    mockkStatic("kotlinx.coroutines.DelayKt")
    coEvery { delay(any()) } returns Unit

    coEvery { stsOidcClient.oidcToken() } returns OidcToken("oidcToken", "tokentype", 100L)

    val httpClientMock = mockkClass(HttpClient::class)
    val httpClientCall = mockkClass(HttpClientCall::class)

    val legeSuspensjonClient = LegeSuspensjonClient(
        "https://localhost/url",
        VaultCredentials("username", "password", clientSecret),
        stsOidcClient, httpClientMock
    )

    beforeEachTest {
        clearMocks(httpClientMock, httpClientCall)
        coEvery { httpClientMock.execute(any()) } returns httpClientCall
    }

    describe("Test LegeSuspensjonClientTest") {
        it("Should get valid suspensjon == true") {
            coEvery { httpClientCall.receive(any()) } returns Suspendert(true)
            runBlocking {
                val suspensjon = legeSuspensjonClient.checkTherapist("1", "2", "2019-01-01")
                suspensjon shouldEqual Suspendert(true)
            }
        }
        it("Should get suspensjon == false") {
            coEvery { httpClientCall.receive(any()) } returns Suspendert(false)
            runBlocking {
                val suspensjon = legeSuspensjonClient.checkTherapist("1", "2", "2019-01-01")
                suspensjon shouldEqual Suspendert(false)
            }
        }
        it("Should retry") {
            coEvery { httpClientCall.receive(any()) } returns Suspendert(true)
            coEvery { httpClientMock.execute(any()) } throws IOException("Exception") andThen httpClientCall
            runBlocking {
                val suspensjon = legeSuspensjonClient.checkTherapist("1", "2", "2019-01-01")
                coVerify(exactly = 2) { httpClientMock.execute(any()) }
                suspensjon shouldEqual Suspendert(true)
            }
        }
        it("should throw exception when retries exceeds max retires") {
            coEvery { httpClientMock.execute(any()) } throws IOException("Exception")
            runBlocking {
                assertFailsWith<IOException> { legeSuspensjonClient.checkTherapist("1", "2", "2019-01-01") }
                coVerify(exactly = 3) { httpClientMock.execute(any()) }
            }
        }
    }
})
