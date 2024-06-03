package no.nav.syfo

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.prometheus.client.hotspot.DefaultExports
import java.net.URI
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.DelicateCoroutinesApi
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.client.AccessTokenClientV2
import no.nav.syfo.client.ClientFactory
import no.nav.syfo.client.SmregisterClient
import no.nav.syfo.common.JacksonKafkaSerializer
import no.nav.syfo.kafka.aiven.KafkaUtils
import no.nav.syfo.kafka.toProducerConfig
import no.nav.syfo.metrics.monitorHttpRequests
import no.nav.syfo.nais.isalive.naisIsAliveRoute
import no.nav.syfo.nais.isready.naisIsReadyRoute
import no.nav.syfo.nais.prometheus.naisPrometheusRoute
import no.nav.syfo.papirsykemelding.api.registerPapirsykemeldingsRegler
import no.nav.syfo.papirsykemelding.service.JuridiskVurderingService
import no.nav.syfo.papirsykemelding.service.PapirsykemeldingRegelService
import no.nav.syfo.papirsykemelding.service.RuleExecutionService
import no.nav.syfo.papirsykemelding.service.SykmeldingService
import no.nav.syfo.pdl.FodselsdatoService
import no.nav.syfo.pdl.client.PdlClient
import org.apache.kafka.clients.producer.KafkaProducer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("no.nav.syfo.smpapirregler")

fun main() {

    val embeddedServer =
        embeddedServer(
            Netty,
            port = EnvironmentVariables().applicationPort,
            module = Application::module,
        )
    Runtime.getRuntime()
        .addShutdownHook(
            Thread {
                embeddedServer.stop(TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(10))
            },
        )
    embeddedServer.start(true)
}

@OptIn(DelicateCoroutinesApi::class)
fun Application.configureRouting(
    applicationState: ApplicationState,
    environmentVariables: EnvironmentVariables,
    jwkProviderAadV2: JwkProvider,
    papirsykemeldingRegelService: PapirsykemeldingRegelService,
) {

    setupAuth(
        environmentVariables = environmentVariables,
        jwkProviderAadV2 = jwkProviderAadV2,
    )
    routing {
        naisIsAliveRoute(applicationState)
        naisIsReadyRoute(applicationState)
        naisPrometheusRoute()
        authenticate("servicebrukerAADv2") {
            registerPapirsykemeldingsRegler(papirsykemeldingRegelService)
        }
    }
    install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
        jackson {
            registerKotlinModule()
            registerModule(JavaTimeModule())
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Unknown error")

            logger.error("Caught exception", cause)
            throw cause
        }
    }
    intercept(ApplicationCallPipeline.Monitoring, monitorHttpRequests())
}

@OptIn(DelicateCoroutinesApi::class)
fun Application.module() {

    val environmentVariables = EnvironmentVariables()
    val applicationState = ApplicationState()

    environment.monitor.subscribe(ApplicationStopped) {
        applicationState.ready = false
        applicationState.alive = false
    }

    val jwkProviderAadV2 =
        JwkProviderBuilder(URI.create(environmentVariables.jwkKeysUrlV2).toURL())
            .cached(10, Duration.ofHours(24))
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()

    val httpClient = ClientFactory.createHttpClient()
    val accessTokenClientV2 =
        AccessTokenClientV2(
            environmentVariables.aadAccessTokenV2Url,
            environmentVariables.clientIdV2,
            environmentVariables.clientSecretV2,
            httpClient,
        )
    val syketilfelleClient =
        ClientFactory.createSyketilfelleClient(
            environmentVariables,
            accessTokenClientV2,
            httpClient
        )
    val legeSuspensjonClient =
        ClientFactory.createLegeSuspensjonClient(
            environmentVariables,
            accessTokenClientV2,
            httpClient
        )
    val norskHelsenettClient =
        ClientFactory.createNorskHelsenettClient(
            environmentVariables,
            accessTokenClientV2,
            httpClient
        )

    val pdlClient =
        PdlClient(
            httpClient = httpClient,
            accessTokenClientV2 = accessTokenClientV2,
            pdlScope = environmentVariables.pdlScope,
            basePath = environmentVariables.pdlGraphqlPath,
            graphQlQuery =
                PdlClient::class
                    .java
                    .getResource("/graphql/getPerson.graphql")!!
                    .readText()
                    .replace(Regex("[\n\t]"), ""),
        )
    val fodselsdatoService = FodselsdatoService(pdlClient)

    val kafkaBaseConfig = KafkaUtils.getAivenKafkaConfig("juridisk-producer")
    val kafkaProperties =
        kafkaBaseConfig.toProducerConfig(
            environmentVariables.applicationName,
            valueSerializer = JacksonKafkaSerializer::class,
        )
    val juridiskVurderingService =
        JuridiskVurderingService(
            KafkaProducer(kafkaProperties),
            environmentVariables.etterlevelsesTopic,
        )
    val papirsykemeldingRegelService =
        PapirsykemeldingRegelService(
            legeSuspensjonClient = legeSuspensjonClient,
            norskHelsenettClient = norskHelsenettClient,
            syketilfelleClient = syketilfelleClient,
            juridiskVurderingService = juridiskVurderingService,
            fodselsdatoService = fodselsdatoService,
            ruleExecutionService = RuleExecutionService(),
            sykmeldingService =
                SykmeldingService(
                    SmregisterClient(
                        environmentVariables.smregisterEndpointURL,
                        accessTokenClientV2,
                        environmentVariables.smregisterAudience,
                        httpClient
                    )
                ),
        )

    configureRouting(
        applicationState = applicationState,
        environmentVariables = environmentVariables,
        jwkProviderAadV2 = jwkProviderAadV2,
        papirsykemeldingRegelService = papirsykemeldingRegelService
    )

    DefaultExports.initialize()
}

fun Application.setupAuth(
    environmentVariables: EnvironmentVariables,
    jwkProviderAadV2: JwkProvider,
) {
    install(Authentication) {
        jwt(name = "servicebrukerAADv2") {
            verifier(jwkProviderAadV2, environmentVariables.jwtIssuerV2)
            validate { credentials ->
                when {
                    harTilgang(credentials, environmentVariables.clientIdV2) ->
                        JWTPrincipal(credentials.payload)
                    else -> unauthorized(credentials)
                }
            }
        }
    }
}

fun harTilgang(credentials: JWTCredential, clientId: String): Boolean {
    val appid: String = credentials.payload.getClaim("azp").asString()
    logger.debug("authorization attempt for $appid")
    return credentials.payload.audience.contains(clientId)
}

fun unauthorized(credentials: JWTCredential): Principal? {
    logger.warn(
        "Auth: Unexpected audience for jwt {}, {}",
        StructuredArguments.keyValue("issuer", credentials.payload.issuer),
        StructuredArguments.keyValue("audience", credentials.payload.audience),
    )
    return null
}

data class ApplicationState(
    var alive: Boolean = true,
    var ready: Boolean = true,
)

class ServiceUnavailableException(message: String?) : Exception(message)
