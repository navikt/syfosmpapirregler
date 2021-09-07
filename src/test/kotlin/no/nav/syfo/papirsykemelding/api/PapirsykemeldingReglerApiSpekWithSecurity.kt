package no.nav.syfo.papirsykemelding.api

import io.ktor.auth.authenticate
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.KtorExperimentalAPI
import io.mockk.coEvery
import io.mockk.mockk
import java.net.ServerSocket
import java.util.concurrent.TimeUnit
import no.nav.syfo.fakeJWTApi
import no.nav.syfo.generateReceivedSykemelding
import no.nav.syfo.genereateJWT
import no.nav.syfo.getStringValue
import no.nav.syfo.getValidResult
import no.nav.syfo.papirsykemelding.service.PapirsykemeldingRegelService
import no.nav.syfo.setUpAuth
import no.nav.syfo.setUpTestApplication
import org.amshove.kluent.shouldBe
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

@KtorExperimentalAPI
class PapirsykemeldingReglerApiSpekWithSecurity : Spek({
    val papirsykemeldingRegelService: PapirsykemeldingRegelService = mockk()
    coEvery { papirsykemeldingRegelService.validateSykemelding(any()) } returns getValidResult()

    val randomPort = ServerSocket(0).use { it.localPort }
    val fakeApi = fakeJWTApi(randomPort)
    afterGroup {
        fakeApi.stop(TimeUnit.SECONDS.toMillis(0), TimeUnit.SECONDS.toMillis(0))
    }

    describe("Validate papirsykemelding with authentication") {
        with(TestApplicationEngine()) {
            setUpTestApplication()
            setUpAuth("http://localhost:$randomPort/fake.jwt", listOf("consumerClientId"))
            application.routing {
                route("/v2") {
                    authenticate("servicebrukerAADv2") { registerPapirsykemeldingsRegler(papirsykemeldingRegelService) }
                }
            }
            it("Should return 401 Unauthorized") {
                with(handleRequest(HttpMethod.Post, "/v2/rules/validate") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(getStringValue(generateReceivedSykemelding()))
                }) {
                    response.status() shouldBe HttpStatusCode.Unauthorized
                }
            }

            it("should return 200 OK") {
                with(handleRequest(HttpMethod.Post, "/v2/rules/validate") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(getStringValue(generateReceivedSykemelding()))
                    addHeader(
                        "Authorization",
                        "Bearer ${genereateJWT(audience = "regel-clientId-v2")}"
                    )
                }) {
                    response.status() shouldBe HttpStatusCode.OK
                }
            }

            it("Should return 401 Unauthorized when wrong audience") {
                with(handleRequest(HttpMethod.Post, "/v2/rules/validate") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(getStringValue(generateReceivedSykemelding()))
                    addHeader(
                        "Authorization",
                        "Bearer ${genereateJWT(audience = "my random app")}"
                    )
                }) {
                    response.status() shouldBe HttpStatusCode.Unauthorized
                }
            }
        }
    }
})
