package no.nav.syfo.papirsykemelding.api

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.*
import io.ktor.utils.io.*
import io.mockk.mockk
import kotlinx.coroutines.DelicateCoroutinesApi
import no.nav.syfo.generateReceivedSykemelding
import no.nav.syfo.getInvalidResult
import no.nav.syfo.getStringValue
import no.nav.syfo.getValidResult
import no.nav.syfo.papirsykemelding.service.PapirsykemeldingRegelService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@DelicateCoroutinesApi
internal class PapirsykemeldingReglerApiSpek {

    @OptIn(InternalAPI::class)
    @Test
    internal fun `Validate papirsykemelding should validate papirsykemelding`() {
        testApplication {
            application {
                val papirsykemeldingRegelService: PapirsykemeldingRegelService = mockk()
                io.mockk.coEvery { papirsykemeldingRegelService.validateSykemelding(any()) } returns
                    getValidResult()

                install(ContentNegotiation) { jackson { registerModule(JavaTimeModule()) } }
                routing { registerPapirsykemeldingsRegler(papirsykemeldingRegelService) }
            }
            val response =
                client.post("/api/v2/rules/validate") {
                    headers {
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }
                    body = getStringValue(generateReceivedSykemelding())
                }

            assertEquals(getStringValue(getValidResult()), response.bodyAsText())
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @OptIn(InternalAPI::class)
    @Test
    internal fun `Validate papirsykemelding should not validate papirsykemelding`() {
        testApplication {
            application {
                val papirsykemeldingRegelService: PapirsykemeldingRegelService = mockk()
                io.mockk.coEvery { papirsykemeldingRegelService.validateSykemelding(any()) } returns
                    getInvalidResult()

                install(ContentNegotiation) { jackson { registerModule(JavaTimeModule()) } }
                routing { registerPapirsykemeldingsRegler(papirsykemeldingRegelService) }
            }

            val response =
                client.post("/api/v2/rules/validate") {
                    headers {
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }
                    body = getStringValue(generateReceivedSykemelding())
                }

            assertEquals(getStringValue(getInvalidResult()), response.bodyAsText())
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }
}
