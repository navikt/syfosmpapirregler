package no.nav.syfo.client.norskhelsenett

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.kotest.core.spec.style.FunSpec
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.call
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
import no.nav.syfo.client.AccessTokenClientV2
import no.nav.syfo.papirsykemelding.model.LoggingMeta
import org.amshove.kluent.shouldBeEqualTo

private const val fnr = "12345647981"

class NorskHelsenettClientTest :
    FunSpec({
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
                    install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                        jackson {}
                    }
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
                                        "Noe gikk galt"
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
                httpClient
            )

        afterSpec { mockServer.stop(TimeUnit.SECONDS.toMillis(1), TimeUnit.SECONDS.toMillis(10)) }

        beforeSpec { coEvery { accessTokenClientV2.getAccessTokenV2(any()) } returns "token" }

        context("Test NorskHelsenettClient") {
            test("Should get behandler ") {
                val behandler = norskHelsenettClient.finnBehandler(fnr, "1", loggingMeta)
                behandler shouldBeEqualTo Behandler(listOf(Godkjenning()))
            }

            test("Should get null when 404") {
                val behandler =
                    norskHelsenettClient.finnBehandler("behandlerFinnesIkke", "1", loggingMeta)
                behandler shouldBeEqualTo null
            }
        }
    })
