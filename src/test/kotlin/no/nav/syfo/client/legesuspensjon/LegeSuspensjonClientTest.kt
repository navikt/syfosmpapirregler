package no.nav.syfo.client.legesuspensjon

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
import io.mockk.mockkClass
import java.net.ServerSocket
import java.util.concurrent.TimeUnit
import no.nav.syfo.client.AccessTokenClientV2
import no.nav.syfo.client.legesuspensjon.model.Suspendert
import org.amshove.kluent.shouldBeEqualTo

class LegeSuspensjonClientTest :
    FunSpec({
        val fnr = "1"
        val httpClient =
            HttpClient(CIO) {
                install(ContentNegotiation) {
                    jackson {
                        registerModule(JavaTimeModule())
                        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    }
                }
            }

        val accessTokenClientV2 = mockkClass(AccessTokenClientV2::class)
        val mockHttpServerPort = ServerSocket(0).use { it.localPort }
        val mockHttpServerUrl = "http://localhost:$mockHttpServerPort"
        val mockServer =
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
                                        "Noe gikk galt"
                                    )
                            }
                        }
                    }
                }
                .start()

        val legeSuspensjonClient =
            LegeSuspensjonClient(
                "$mockHttpServerUrl/legeSuspensjonClient",
                accessTokenClientV2,
                httpClient,
                "scope",
            )
        afterSpec { mockServer.stop(TimeUnit.SECONDS.toMillis(1), TimeUnit.SECONDS.toMillis(10)) }

        beforeSpec { coEvery { accessTokenClientV2.getAccessTokenV2(any()) } returns "token" }

        context("Test no.nav.syfo.client.legesuspensjon.LegeSuspensjonClientTest") {
            test("Should get valid suspensjon == true") {
                val suspensjon = legeSuspensjonClient.checkTherapist(fnr, "2", "2019-01-01")
                suspensjon shouldBeEqualTo Suspendert(true)
            }
            test("Should get suspensjon == false") {
                val suspensjon = legeSuspensjonClient.checkTherapist("2", "3", "2019-01-01")
                suspensjon shouldBeEqualTo Suspendert(false)
            }
        }
    })
