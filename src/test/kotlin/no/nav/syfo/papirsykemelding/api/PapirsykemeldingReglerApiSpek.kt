package no.nav.syfo.papirsykemelding.api

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.kotest.core.spec.style.FunSpec
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
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

@DelicateCoroutinesApi
class PapirsykemeldingReglerApiSpek :
    FunSpec({
        context("Validate papirsykemelding") {
            with(TestApplicationEngine()) {
                start()

                val papirsykemeldingRegelService: PapirsykemeldingRegelService = mockk()
                io.mockk.coEvery { papirsykemeldingRegelService.validateSykemelding(any()) } returns
                    getValidResult()
                application.install(ContentNegotiation) {
                    jackson { registerModule(JavaTimeModule()) }
                }
                application.routing {
                    registerPapirsykemeldingsRegler(papirsykemeldingRegelService)
                }

                test("Should validate papirsykemelding") {
                    with(
                        handleRequest(HttpMethod.Post, "/rules/validate") {
                            addHeader(
                                HttpHeaders.ContentType,
                                ContentType.Application.Json.toString()
                            )
                            setBody(
                                getStringValue(
                                    generateReceivedSykemelding(),
                                ),
                            )
                        },
                    ) {
                        response.content shouldBeEqualTo getStringValue(getValidResult())
                        response.status() shouldBeEqualTo HttpStatusCode.OK
                    }
                }
                test("Should not validate papirsykemelding") {
                    with(
                        handleRequest(HttpMethod.Post, "/rules/validate") {
                            addHeader(
                                HttpHeaders.ContentType,
                                ContentType.Application.Json.toString()
                            )
                            io.mockk.coEvery {
                                papirsykemeldingRegelService.validateSykemelding(any())
                            } returns getInvalidResult()
                            setBody(getStringValue(generateReceivedSykemelding()))
                        },
                    ) {
                        response.content shouldBeEqualTo getStringValue(getInvalidResult())
                        response.status() shouldBeEqualTo HttpStatusCode.OK
                    }
                }
            }
        }
    })
