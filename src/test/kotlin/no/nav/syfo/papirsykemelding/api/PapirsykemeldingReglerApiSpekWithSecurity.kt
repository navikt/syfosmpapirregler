package no.nav.syfo.papirsykemelding.api

import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.application.install
import io.ktor.auth.authenticate
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
import java.nio.file.Path
import no.nav.syfo.Environment
import no.nav.syfo.application.authentication.setupAuth
import no.nav.syfo.generateReceivedSykemelding
import no.nav.syfo.genereateJWT
import no.nav.syfo.getStringValue
import no.nav.syfo.getValidResult
import no.nav.syfo.papirsykemelding.service.PapirsykemeldingRegelService
import org.amshove.kluent.shouldBe
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class PapirsykemeldingReglerApiSpekWithSecurity : Spek({
    val papirsykemeldingRegelService: PapirsykemeldingRegelService = mockk()
    io.mockk.coEvery { papirsykemeldingRegelService.validateSykemelding(any()) } returns getValidResult()
    fun withTestApplicationForApi(receiver: TestApplicationEngine, block: TestApplicationEngine.() -> Unit) {
        receiver.start()
        val environment = Environment(8080,
            jwtIssuer = "https://sts.issuer.net/myid",
            appIds = "2,3".split(","),
            clientId = "1",
            diskresjonskodeEndpointUrl = "",
            securityTokenServiceURL = "",
            helsenettproxyId = "",
            aadAccessTokenUrl = "")
        val path = "src/test/resources/jwkset.json"
        val uri = Path.of(path).toUri().toURL()
        val jwkProvider = JwkProviderBuilder(uri).build()
        receiver.application.install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
                registerKotlinModule()
            }
        }
        receiver.application.setupAuth(environment, jwkProvider)
        receiver.application.routing { authenticate { registerPapirsykemeldingsRegler(papirsykemeldingRegelService) } }

        return receiver.block()
    }

    describe("Validate papirsykemelding with authentication") {
        withTestApplicationForApi(TestApplicationEngine()) {
            it("Should return 401 Unauthorized") {
                with(handleRequest(HttpMethod.Post, "/v1/rules/validate") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(getStringValue(generateReceivedSykemelding()))
                }) {
                    response.status() shouldBe HttpStatusCode.Unauthorized
                }
            }

            it("should return 200 OK") {
                with(handleRequest(HttpMethod.Post, "/v1/rules/validate") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(getStringValue(generateReceivedSykemelding()))
                    addHeader(
                        "Authorization",
                        "Bearer ${genereateJWT("2", "1")}"
                    )
                }) {
                    response.status() shouldBe HttpStatusCode.OK
                }
            }

            it("Should return 401 Unauthorized when appId not allowed") {
                with(handleRequest(HttpMethod.Post, "/v1/rules/validate") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(getStringValue(generateReceivedSykemelding()))
                    addHeader(
                        "Authorization",
                        "Bearer ${genereateJWT("5", "1")}"
                    )
                }) {
                    response.status() shouldBe HttpStatusCode.Unauthorized
                }
            }
        }
    }
})
