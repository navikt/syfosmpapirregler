
import io.ktor.util.KtorExperimentalAPI
import io.mockk.coEvery
import io.mockk.mockkClass
import kotlinx.coroutines.runBlocking
import no.nav.syfo.ResponseData
import no.nav.syfo.ResponseHandler
import no.nav.syfo.accesstoken.service.AccessTokenService
import no.nav.syfo.client.Behandler
import no.nav.syfo.client.Godkjenning
import no.nav.syfo.client.NorskHelsenettClient
import no.nav.syfo.getHttpClient
import no.nav.syfo.getStringValue
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

private val fnr = "26047792540"

@KtorExperimentalAPI
class NorskHelsenettClientTest : Spek({
    val accessTokenService = mockkClass(AccessTokenService::class)
    val responseHandler = ResponseHandler()
    val httpClient = getHttpClient(responseHandler)
    val norskHelsenettClient = NorskHelsenettClient("url", accessTokenService, "resource", httpClient)
    coEvery { accessTokenService.getAccessToken(any()) } returns "token"
    describe("Test NorskHelsenettClient") {
        it("Should get behandler ") {
            responseHandler.updateResponseData(
                ResponseData(
                    getStringValue(Behandler(listOf(Godkjenning())))
                )
            )
            runBlocking {
                val behandler = norskHelsenettClient.finnBehandler(fnr, "1")
                behandler shouldEqual Behandler(listOf(Godkjenning()))
            }
        }
    }
})
