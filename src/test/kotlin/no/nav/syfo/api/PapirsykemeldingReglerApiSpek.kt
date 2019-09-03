package no.nav.syfo.api

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.auth.authenticate
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.routing.routing
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.mockk.mockk
import java.nio.file.Path
import no.nav.syfo.Environment
import no.nav.syfo.authentication.setupAuth
import no.nav.syfo.genereateJWT
import no.nav.syfo.service.PapirsykemeldingRegelService
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object PapirsykemeldingReglerApiSpek : Spek({

    describe("Validate papirsykemelding") {
        with(TestApplicationEngine()) {
                start()
                val papirsykemeldingRegelService: PapirsykemeldingRegelService = mockk()
                io.mockk.coEvery { papirsykemeldingRegelService.validateSykemelding("test") } returns "test1"
                application.routing { registerPapirsykemeldingsRegler(papirsykemeldingRegelService)
            }

            it("Should validate papirsykemelding") {
                with(handleRequest(HttpMethod.Post, "/v1/rules/validate") {
                setBody("test")
                }) {
                    response.content shouldEqual "test1"
                    response.status() shouldEqual HttpStatusCode.OK
                }
            }
        }
    }

    describe("Validate papirsykemelding with authentication") {
        with(TestApplicationEngine()) {
            start()
            val papirsykemeldingRegelService: PapirsykemeldingRegelService = mockk()
            io.mockk.coEvery { papirsykemeldingRegelService.validateSykemelding("test") } returns "test1"
            val environment = Environment(8080,
                jwtIssuer = "https://sts.issuer.net/myid",
                appIds = "2,3".split(","),
                clientId = "1")
            val path = "src/test/resources/jwkset.json"
            val uri = Path.of(path).toUri().toURL()
            val jwkProvider = JwkProviderBuilder(uri).build()
            application.setupAuth(environment, jwkProvider)
            application.routing { authenticate { registerPapirsykemeldingsRegler(papirsykemeldingRegelService) } }

            it("Should return 401 Unauthorized") {
                with(handleRequest(HttpMethod.Post, "/v1/rules/validate") {
                    setBody("test")
                }) {
                    response.status() shouldBe HttpStatusCode.Unauthorized
                }
            }

            it("should return 200 OK") {
                with(handleRequest(HttpMethod.Post, "/v1/rules/validate") {
                    setBody("test")
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
                    setBody("test")
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
