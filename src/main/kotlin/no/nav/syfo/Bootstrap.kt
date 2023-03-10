package no.nav.syfo

import com.auth0.jwk.JwkProviderBuilder
import io.prometheus.client.hotspot.DefaultExports
import kotlinx.coroutines.DelicateCoroutinesApi
import no.nav.syfo.application.ApplicationServer
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.createApplicationEngine
import no.nav.syfo.client.AccessTokenClientV2
import no.nav.syfo.client.ClientFactory
import no.nav.syfo.common.JacksonKafkaSerializer
import no.nav.syfo.kafka.aiven.KafkaUtils
import no.nav.syfo.kafka.toProducerConfig
import no.nav.syfo.papirsykemelding.service.JuridiskVurderingService
import no.nav.syfo.papirsykemelding.service.PapirsykemeldingRegelService
import no.nav.syfo.pdl.FodselsdatoService
import no.nav.syfo.pdl.client.PdlClient
import org.apache.kafka.clients.producer.KafkaProducer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.concurrent.TimeUnit

val log: Logger = LoggerFactory.getLogger("no.nav.syfo.smpapirregler")

@DelicateCoroutinesApi
fun main() {
    val env = Environment()
    val applicationState = ApplicationState()
    DefaultExports.initialize()

    val jwkProviderAadV2 = JwkProviderBuilder(URL(env.jwkKeysUrlV2))
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    val httpClient = ClientFactory.createHttpClient()
    val accessTokenClientV2 = AccessTokenClientV2(
        env.aadAccessTokenV2Url,
        env.clientIdV2,
        env.clientSecretV2,
        httpClient
    )
    val syketilfelleClient = ClientFactory.createSyketilfelleClient(env, accessTokenClientV2, httpClient)
    val legeSuspensjonClient = ClientFactory.createLegeSuspensjonClient(env, accessTokenClientV2, httpClient)
    val norskHelsenettClient = ClientFactory.createNorskHelsenettClient(env, accessTokenClientV2, httpClient)

    val pdlClient = PdlClient(
        httpClient = httpClient,
        accessTokenClientV2 = accessTokenClientV2,
        pdlScope = env.pdlScope,
        basePath = env.pdlGraphqlPath,
        graphQlQuery = PdlClient::class.java.getResource("/graphql/getPerson.graphql")!!.readText().replace(Regex("[\n\t]"), "")
    )
    val fodselsdatoService = FodselsdatoService(pdlClient)

    val kafkaBaseConfig = KafkaUtils.getAivenKafkaConfig()
    val kafkaProperties = kafkaBaseConfig.toProducerConfig(
        env.applicationName,
        valueSerializer = JacksonKafkaSerializer::class
    )
    val juridiskVurderingService = JuridiskVurderingService(
        KafkaProducer(kafkaProperties),
        env.etterlevelsesTopic
    )
    val papirsykemeldingRegelService = PapirsykemeldingRegelService(
        legeSuspensjonClient = legeSuspensjonClient,
        norskHelsenettClient = norskHelsenettClient,
        syketilfelleClient = syketilfelleClient,
        juridiskVurderingService = juridiskVurderingService,
        fodselsdatoService = fodselsdatoService
    )
    val applicationEngine = createApplicationEngine(
        papirsykemeldingRegelService = papirsykemeldingRegelService,
        env = env,
        applicationState = applicationState,
        jwkProviderAadV2 = jwkProviderAadV2
    )

    val applicationServer = ApplicationServer(applicationEngine, applicationState)
    applicationServer.start()
}
