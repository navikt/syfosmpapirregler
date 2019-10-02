package no.nav.syfo.client.syketilfelle

import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.util.KtorExperimentalAPI
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkClass
import io.mockk.mockkStatic
import java.io.IOException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.syfo.client.OidcToken
import no.nav.syfo.client.StsOidcClient
import no.nav.syfo.generateSyketilfeller
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

@KtorExperimentalAPI
class SyketilfelleClientTest : Spek({
    val stsOidcClient = mockkClass(StsOidcClient::class)
    val httpClientCall = mockkClass(HttpClientCall::class)
    val httpClient = mockkClass(HttpClient::class)
    val syketilfelleClient = SyketilfelleClient("url", stsOidcClient, httpClient)

    mockkStatic("kotlinx.coroutines.DelayKt")
    coEvery { delay(any()) } returns Unit
    coEvery { stsOidcClient.oidcToken() } returns OidcToken("token", "Bearer", 200L)

    beforeEachTest {
        clearMocks(httpClient)
        coEvery { httpClient.execute(any()) } returns httpClientCall
    }

    describe("Test SyketilfelleClient") {

        it("Should get syketilfelle") {
            coEvery { httpClientCall.receive(any()) } returns true
            runBlocking {
                val erNyttSyketilfelle = syketilfelleClient.fetchErNytttilfelle(generateSyketilfeller(), "123")
                erNyttSyketilfelle shouldEqual true
            }
        }

        it("should retry") {
            coEvery { httpClient.execute(any()) } throws IOException("Exception") andThen httpClientCall
            coEvery { httpClientCall.receive(any()) } returns false
            runBlocking {
                val erNyttSyketilfelle = syketilfelleClient.fetchErNytttilfelle(generateSyketilfeller(), "213")
                erNyttSyketilfelle shouldEqual false
                coVerify(exactly = 2) { httpClient.execute(any()) }
            }
        }
    }
})
