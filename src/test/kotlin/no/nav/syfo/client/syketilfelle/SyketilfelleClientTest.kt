package no.nav.syfo.client.syketilfelle

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkClass
import java.net.ServerSocket
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import no.nav.syfo.accesstoken.service.AccessTokenService
import no.nav.syfo.client.OidcToken
import no.nav.syfo.client.StsOidcClient
import no.nav.syfo.generateSyketilfeller
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

@KtorExperimentalAPI
class SyketilfelleClientTest : Spek({

    val fnr = "1"
    val accessTokenService = mockk<AccessTokenService>()
    val httpClient = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = JacksonSerializer {
                registerKotlinModule()
                registerModule(JavaTimeModule())
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }

    val stsOidcClient = mockkClass(StsOidcClient::class)
    val mockHttpServerPort = ServerSocket(0).use { it.localPort }
    val mockHttpServerUrl = "http://localhost:$mockHttpServerPort"
    val mockServer = embeddedServer(Netty, mockHttpServerPort) {
        install(ContentNegotiation) {
            jackson {}
        }
        routing {
            post("/oppfolgingstilfelle/ernytttilfelle/123") {
                call.respond(true)
            }
            post("/oppfolgingstilfelle/ernytttilfelle/213") {
                call.respond(false)
            }
        }
    }.start()

    val syketilfelleClient = SyketilfelleClient(mockHttpServerUrl, stsOidcClient, httpClient)

    afterGroup {
        mockServer.stop(TimeUnit.SECONDS.toMillis(1), TimeUnit.SECONDS.toMillis(10))
    }

    beforeGroup {
        coEvery { accessTokenService.getAccessToken(any()) } returns "token"
        coEvery { stsOidcClient.oidcToken() } returns OidcToken("oidcToken", "tokentype", 100L)
    }

    describe("Test SyketilfelleClient") {

        it("Should get syketilfelle true") {
            runBlocking {
                val erNyttSyketilfelle = syketilfelleClient.fetchErNytttilfelle(generateSyketilfeller(), "123")
                erNyttSyketilfelle shouldEqual true
            }
        }

        it("Should get syketilfelle false") {
            runBlocking {
                val erNyttSyketilfelle = syketilfelleClient.fetchErNytttilfelle(generateSyketilfeller(), "213")
                erNyttSyketilfelle shouldEqual false
            }
        }
    }
})
