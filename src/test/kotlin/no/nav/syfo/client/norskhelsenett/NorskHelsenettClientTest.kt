package no.nav.syfo.client.norskhelsenett

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.mockk.coEvery
import io.mockk.mockk
import java.net.ServerSocket
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import no.nav.syfo.client.AccessTokenClientV2
import no.nav.syfo.papirsykemelding.model.LoggingMeta
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val fnr = "12345647981"

class NorskHelsenettClientTest {

    val accessTokenClientV2 = mockk<AccessTokenClientV2>()
    val httpClient =
        HttpClient(CIO) {
            expectSuccess = false
            install(ContentNegotiation) {
                jackson {
                    registerModule(JavaTimeModule())
                    configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
            }
        }

    val loggingMeta = LoggingMeta("23", "900323", "1231", "31311-31312313-13")
    val mockHttpServerPort = ServerSocket(0).use { it.localPort }
    val mockHttpServerUrl = "http://localhost:$mockHttpServerPort"
    val mockServer =
        embeddedServer(Netty, mockHttpServerPort) {
                install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) { jackson {} }
                routing {
                    get("/syfohelsenettproxy/api/v2/behandler") {
                        when {
                            call.request.headers["behandlerFnr"] == fnr ->
                                call.respond(Behandler(listOf(Godkjenning())))
                            call.request.headers["behandlerFnr"] == "behandlerFinnesIkke" ->
                                call.respond(HttpStatusCode.NotFound, "Behandler finnes ikke")
                            else ->
                                call.respond(
                                    HttpStatusCode.InternalServerError,
                                    "Noe gikk galt",
                                )
                        }
                    }
                }
            }
            .start()

    val norskHelsenettClient =
        NorskHelsenettClient(
            "$mockHttpServerUrl/syfohelsenettproxy",
            accessTokenClientV2,
            "resourceId",
            httpClient,
        )

    @AfterEach
    fun cleanup() {
        mockServer.stop(TimeUnit.SECONDS.toMillis(1), TimeUnit.SECONDS.toMillis(10))
    }

    @BeforeEach
    fun setup() {
        coEvery { accessTokenClientV2.getAccessTokenV2(any()) } returns "token"
    }

    @Test
    internal fun `NorskHelsenettClient should get behandler`() {
        runBlocking {
            val behandler = norskHelsenettClient.finnBehandler(fnr, "1", loggingMeta)
            assertEquals(Behandler(listOf(Godkjenning())), behandler)
        }
    }

    @Test
    internal fun `NorskHelsenettClient should get null when 404`() {
        runBlocking {
            val behandler =
                norskHelsenettClient.finnBehandler("behandlerFinnesIkke", "1", loggingMeta)
            assertEquals(null, behandler)
        }
    }
}
