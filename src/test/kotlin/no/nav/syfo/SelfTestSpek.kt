package no.nav.syfo

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.routing.routing
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import no.nav.syfo.api.registerNaisApi
import no.nav.syfo.application.ApplicationState
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object SelfTestSpek : Spek({
    val applicationState = ApplicationState()
    describe("Successfull liveness and readyness tests") {
        with(TestApplicationEngine()) {
            start()
            application.routing { registerNaisApi(readynessCheck = { true }, livenessCheck = { applicationState.running }) }

            it("Returns ok on is_alive") {
                with(handleRequest(HttpMethod.Get, "/is_alive")) {
                    response.status() shouldEqual HttpStatusCode.OK
                    response.content shouldEqual "I'm alive! :)"
                }
            }
            it("Returns ok in is_ready") {
                with(handleRequest(HttpMethod.Get, "/is_ready")) {
                    response.status() shouldEqual HttpStatusCode.OK
                    response.content shouldEqual "I'm ready! :)"
                }
            }
        }
    }
    describe("Unsuccessful liveness and readyness") {
        with(TestApplicationEngine()) {
            start()
            application.routing { registerNaisApi(readynessCheck = { false }, livenessCheck = { false }) }

            it("Returns internal server error when liveness check fails") {
                with(handleRequest(HttpMethod.Get, "/is_alive")) {
                    response.status() shouldEqual HttpStatusCode.InternalServerError
                    response.content shouldEqual "I'm dead x_x"
                }
            }

            it("Returns internal server error when readyness check fails") {
                with(handleRequest(HttpMethod.Get, "/is_ready")) {
                    response.status() shouldEqual HttpStatusCode.InternalServerError
                    response.content shouldEqual "Please wait! I'm not ready :("
                }
            }
        }
    }
})
