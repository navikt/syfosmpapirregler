package no.nav.syfo.papirsykemelding.api

import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.testing.*
import io.ktor.utils.io.*
import io.mockk.coEvery
import io.mockk.mockk
import java.net.ServerSocket
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.DelicateCoroutinesApi
import no.nav.syfo.fakeJWTApi
import no.nav.syfo.generateReceivedSykemelding
import no.nav.syfo.genereateJWT
import no.nav.syfo.getStringValue
import no.nav.syfo.getValidResult
import no.nav.syfo.papirsykemelding.service.PapirsykemeldingRegelService
import no.nav.syfo.setUpAuth
import no.nav.syfo.setUpTestApplication
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@OptIn(InternalAPI::class)
@DelicateCoroutinesApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PapirsykemeldingReglerApiTestWithSecurity {
    private val papirsykemeldingRegelService: PapirsykemeldingRegelService = mockk()
    private val randomPort = ServerSocket(0).use { it.localPort }
    private val fakeApi = fakeJWTApi(randomPort)

    @BeforeAll
    internal fun setup() {
        coEvery { papirsykemeldingRegelService.validateSykemelding(any()) } returns getValidResult()
    }

    @AfterEach
    fun after() {
        fakeApi.stop(TimeUnit.SECONDS.toMillis(0), TimeUnit.SECONDS.toMillis(0))
    }

    @Test
    internal fun `Validate papirsykemelding with authentication should return 401 Unauthorized`() {
        testApplication {
            setUpTestApplication()
            setUpAuth("http://localhost:$randomPort/fake.jwt")
            application {
                routing {
                    route("/") {
                        authenticate("servicebrukerAADv2") {
                            registerPapirsykemeldingsRegler(papirsykemeldingRegelService)
                        }
                    }
                }
            }
            val response =
                client.post("/api/v2/rules/validate") {
                    headers {
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }
                    body = getStringValue(generateReceivedSykemelding())
                }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    @Test
    internal fun `Validate papirsykemelding with authentication should return 200 OK`() {
        testApplication {
            setUpTestApplication()
            setUpAuth("http://localhost:$randomPort/fake.jwt")
            application {
                routing {
                    route("/") {
                        authenticate("servicebrukerAADv2") {
                            registerPapirsykemeldingsRegler(papirsykemeldingRegelService)
                        }
                    }
                }
            }
            val response =
                client.post("/api/v2/rules/validate") {
                    headers {
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        append(
                            "Authorization",
                            "Bearer ${genereateJWT(audience = "regel-clientId-v2")}",
                        )
                    }
                    body = getStringValue(generateReceivedSykemelding())
                }

            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    internal fun `Validate papirsykemelding with authentication should return 401 Unauthorized when wrong audience`() {
        testApplication {
            setUpTestApplication()
            setUpAuth("http://localhost:$randomPort/fake.jwt")
            application {
                routing {
                    route("/") {
                        authenticate("servicebrukerAADv2") {
                            registerPapirsykemeldingsRegler(papirsykemeldingRegelService)
                        }
                    }
                }
            }
            val response =
                client.post("/api/v2/rules/validate") {
                    headers {
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        append(
                            "Authorization",
                            "Bearer ${genereateJWT(audience = "my random app")}"
                        )
                    }
                    body = getStringValue(generateReceivedSykemelding())
                }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }
}
