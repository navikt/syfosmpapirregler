package no.nav.syfo.papirsykemelding.api

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.routing.routing
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.mockk.mockk
import kotlinx.coroutines.DelicateCoroutinesApi
import no.nav.syfo.generateReceivedSykemelding
import no.nav.syfo.getInvalidResult
import no.nav.syfo.getStringValue
import no.nav.syfo.getValidResult
import no.nav.syfo.papirsykemelding.service.PapirsykemeldingRegelService
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

@DelicateCoroutinesApi
class PapirsykemeldingReglerApiSpek : Spek({

    describe("Validate papirsykemelding") {
        with(TestApplicationEngine()) {
            start()

            val papirsykemeldingRegelService: PapirsykemeldingRegelService = mockk()
            io.mockk.coEvery { papirsykemeldingRegelService.validateSykemelding(any()) } returns getValidResult()
            application.install(ContentNegotiation) {
                jackson {
                    registerModule(JavaTimeModule())
                    registerKotlinModule()
                }
            }
            application.routing { registerPapirsykemeldingsRegler(papirsykemeldingRegelService) }

            it("Should validate papirsykemelding") {
                with(
                    handleRequest(HttpMethod.Post, "/rules/validate") {
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(
                            getStringValue(
                                generateReceivedSykemelding()
                            )
                        )
                    }
                ) {
                    response.content shouldBeEqualTo getStringValue(getValidResult())
                    response.status() shouldBeEqualTo HttpStatusCode.OK
                }
            }
            it("Should not validate papirsykemelding") {
                with(
                    handleRequest(HttpMethod.Post, "/rules/validate") {
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        io.mockk.coEvery { papirsykemeldingRegelService.validateSykemelding(any()) } returns getInvalidResult()
                        setBody(getStringValue(generateReceivedSykemelding()))
                    }
                ) {
                    response.content shouldBeEqualTo getStringValue(getInvalidResult())
                    response.status() shouldBeEqualTo HttpStatusCode.OK
                }
            }
        }
    }
})
