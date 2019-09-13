package no.nav.syfo.accesstoken.service

import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkClass
import java.time.Instant
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import no.nav.syfo.accesstoken.client.AccessTokenClient
import no.nav.syfo.accesstoken.model.AadAccessToken
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class AccessTokenServiceTest : Spek({
    val accessTokenClient = mockkClass(AccessTokenClient::class)
    var accessTokenService = AccessTokenService(accessTokenClient)
    describe("Testing AccessTokenService") {
        beforeEach {
            clearMocks(accessTokenClient)
            accessTokenService = AccessTokenService(accessTokenClient)
        }

        it("Should get accesstoken from AccessTokenService") {
            coEvery { accessTokenClient.hentAccessToken("resource1") } returns AadAccessToken(
                "token1",
                Instant.now()
            )
            runBlocking {

                val token = accessTokenService.getAccessToken("resource1")
                token shouldEqual "token1"
            }
        }

        it("Should get same accesstoken only one time for multiple requests") {
            coEvery { accessTokenClient.hentAccessToken("resource1") } returns AadAccessToken(
                "token1",
                Instant.now().plusSeconds(200L)
            )
            runBlocking {
                0.until(20).map {
                    GlobalScope.launch {
                        accessTokenService.getAccessToken("resource1")
                    }
                }.toList().forEach { it.join() }
            }
            coVerify(exactly = 1) { accessTokenClient.hentAccessToken("resource1") }
        }

        it("Should get new access token when it is about to expire") {
            coEvery { accessTokenClient.hentAccessToken("resource1") } returns AadAccessToken(
                "token1",
                Instant.now()
            )
            runBlocking {
                0.until(20).map {
                    GlobalScope.launch {
                        accessTokenService.getAccessToken("resource1")
                    }
                }.toList().joinAll()
            }
            coVerify(exactly = 20) { accessTokenClient.hentAccessToken("resource1") }
        }

        it("Should get token for different resources only one time") {
            coEvery { accessTokenClient.hentAccessToken("resource0") } returns AadAccessToken(
                "token",
                Instant.now().plusSeconds(200)
            )
            coEvery { accessTokenClient.hentAccessToken("resource1") } returns AadAccessToken(
                "token2",
                Instant.now().plusSeconds(200)
            )
            runBlocking {
                0.until(20).map {
                    GlobalScope.launch {
                        accessTokenService.getAccessToken("resource" + it % 2)
                    }
                }.toList().joinAll()
            }
            coVerify(exactly = 2) { accessTokenClient.hentAccessToken(any()) }
            coVerify(exactly = 1) { accessTokenClient.hentAccessToken("resource0") }
            coVerify(exactly = 1) { accessTokenClient.hentAccessToken("resource1") }
        }
    }
})
