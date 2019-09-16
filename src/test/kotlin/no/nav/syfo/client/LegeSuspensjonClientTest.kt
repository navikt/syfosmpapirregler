import io.ktor.util.KtorExperimentalAPI
import io.mockk.coEvery
import io.mockk.mockkClass
import kotlinx.coroutines.runBlocking
import no.nav.syfo.ResponseData
import no.nav.syfo.ResponseHandler
import no.nav.syfo.VaultCredentials
import no.nav.syfo.client.LegeSuspensjonClient
import no.nav.syfo.client.OidcToken
import no.nav.syfo.client.StsOidcClient
import no.nav.syfo.client.Suspendert
import no.nav.syfo.getHttpClient
import no.nav.syfo.getStringValue
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

@KtorExperimentalAPI
class LegeSuspensjonClientTest : Spek({

    val responseHandler = ResponseHandler()
    val httpClient = getHttpClient(responseHandler)
    val stsOidcClient = mockkClass(StsOidcClient::class)

    coEvery { stsOidcClient.oidcToken() } returns OidcToken("oidcToken", "tokentype", 100L)

    val legeSuspensjonClient = LegeSuspensjonClient("url",
        VaultCredentials("username", "password"),
        stsOidcClient, httpClient)

    describe("Test LegeSuspensjonClientTest") {
        responseHandler.updateResponseData(ResponseData(getStringValue(Suspendert(true))))

        it("Should get valid suspensjon == true") {
            runBlocking {
                val suspensjon = legeSuspensjonClient.checkTherapist("1", "2", "2019-01-01")
                suspensjon shouldEqual Suspendert(true)
            }
        }
        it("Should get suspensjon == false") {
            responseHandler.updateResponseData(
                ResponseData(getStringValue(Suspendert(false)))
            )
            runBlocking {
                val suspensjon = legeSuspensjonClient.checkTherapist("1", "2", "2019-01-01")
                suspensjon shouldEqual Suspendert(false)
            }
        }
    }
})
