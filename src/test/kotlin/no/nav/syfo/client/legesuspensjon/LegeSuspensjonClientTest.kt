package no.nav.syfo.client.legesuspensjon

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
import io.mockk.mockkClass
import java.net.ServerSocket
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import no.nav.syfo.client.AccessTokenClientV2
import no.nav.syfo.client.legesuspensjon.model.Suspendert
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LegeSuspensjonClientTest {
    private val fnr = "1"
    private val httpClient =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                jackson {
                    registerModule(JavaTimeModule())
                    configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
            }
        }

    private val accessTokenClientV2 = mockkClass(AccessTokenClientV2::class)
    private val mockHttpServerPort = ServerSocket(0).use { it.localPort }
    private val mockHttpServerUrl = "http://localhost:$mockHttpServerPort"
    private val mockServer =
        embeddedServer(Netty, mockHttpServerPort) {
                install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                    jackson {
                        registerModule(JavaTimeModule())
                        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    }
                }
                routing {
                    get("/legeSuspensjonClient/api/v1/suspensjon/status") {
                        when {
                            call.request.headers["Nav-Personident"] == fnr ->
                                call.respond(Suspendert(true))
                            call.request.headers["Nav-Personident"] == "2" ->
                                call.respond(Suspendert(false))
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

    private val legeSuspensjonClient =
        LegeSuspensjonClient(
            "$mockHttpServerUrl/legeSuspensjonClient",
            accessTokenClientV2,
            httpClient,
            "scope",
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
    internal fun `LegeSuspensjonClientTest Should get valid suspensjon == true`() {
        runBlocking {
            val suspensjon = legeSuspensjonClient.checkTherapist(fnr, "2", "2019-01-01")
            suspensjon shouldBeEqualTo Suspendert(true)
        }
    }

    @Test
    internal fun `LegeSuspensjonClientTest Should get valid suspensjon == false`() {
        runBlocking {
            val suspensjon = legeSuspensjonClient.checkTherapist("2", "3", "2019-01-01")
            suspensjon shouldBeEqualTo Suspendert(false)
        }
    }
}
