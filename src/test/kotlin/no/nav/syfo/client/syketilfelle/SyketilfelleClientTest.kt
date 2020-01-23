package no.nav.syfo.client.syketilfelle

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import io.mockk.coEvery
import io.mockk.mockkClass
import io.mockk.mockkStatic
import java.net.ServerSocket
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.syfo.client.OidcToken
import no.nav.syfo.client.StsOidcClient
import no.nav.syfo.generateSyketilfeller
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

@KtorExperimentalAPI
class SyketilfelleClientTest : Spek({

    val aktorId = "123"

    val httpClient = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = JacksonSerializer {
                registerKotlinModule()
                registerModule(JavaTimeModule())
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }

    val mockHttpServerPort = ServerSocket(0).use { it.localPort }
    val mockHttpServerUrl = "http://localhost:$mockHttpServerPort"
    val mockServer = embeddedServer(Netty, mockHttpServerPort) {
        install(ContentNegotiation) {
            jackson {}
        }
        routing {
            post("/oppfolgingstilfelle/ernytttilfelle/$aktorId") {
                when {
                    call.request.headers["Authorization"] == "Bearer token" -> call.respond(true)
                    call.request.headers["Authorization"] == "Bearer 132" -> call.respond(false)
                    else -> call.respond(HttpStatusCode.InternalServerError, "Noe gikk galt")
                }
            }
        }
    }.start()

    val stsOidcClient = mockkClass(StsOidcClient::class)
    val syketilfelleClient = SyketilfelleClient(mockHttpServerUrl, stsOidcClient, httpClient)

    describe("Test SyketilfelleClient") {

        it("Should get syketilfelle") {
            runBlocking {
            coEvery { stsOidcClient.oidcToken() } returns OidcToken("token", "Bearer", 200L)
                val erNyttSyketilfelle = syketilfelleClient.fetchErNytttilfelle(generateSyketilfeller(), aktorId)
                erNyttSyketilfelle shouldEqual true
                mockServer.stop(TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(10))
            }
        }

        it("should retry") {
            coEvery { stsOidcClient.oidcToken() } returns OidcToken("132", "Bearer", 200L)

            runBlocking {
                val erNyttSyketilfelle = syketilfelleClient.fetchErNytttilfelle(generateSyketilfeller(), aktorId)
                erNyttSyketilfelle shouldEqual false
                mockServer.stop(TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(10))
            }
        }
    }
})
