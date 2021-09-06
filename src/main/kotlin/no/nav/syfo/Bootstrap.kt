package no.nav.syfo

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.util.KtorExperimentalAPI
import io.prometheus.client.hotspot.DefaultExports
import java.net.URL
import java.util.concurrent.TimeUnit
import no.nav.syfo.application.ApplicationServer
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.createApplicationEngine
import no.nav.syfo.client.AccessTokenClientV2
import no.nav.syfo.client.ClientFactory
import no.nav.syfo.papirsykemelding.service.PapirsykemeldingRegelService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log: Logger = LoggerFactory.getLogger("no.nav.syfo.smpapirregler")

@KtorExperimentalAPI
fun main() {
    val env = Environment()
    val credentials = VaultCredentials()
    val applicationState = ApplicationState()
    DefaultExports.initialize()

    val jwkProvider = JwkProviderBuilder(URL(env.jwkKeysUrl))
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    val httpClient = ClientFactory.createHttpClient()
    val httpClientProxy = ClientFactory.createHttpClientProxy()
    val accessTokenClientV2 = AccessTokenClientV2(
            env.aadAccessTokenV2Url,
            env.clientIdV2,
            env.clientSecretV2,
            httpClientProxy
    )
    val stsClient = ClientFactory.createStsOidcClient(credentials, env)
    val syketilfelleClient = ClientFactory.createSyketilfelleClient(env, stsClient, httpClient)
    val legeSuspensjonClient = ClientFactory.createLegeSuspensjonClient(env, credentials, stsClient, httpClient)
    val norskHelsenettClient = ClientFactory.createNorskHelsenettClient(env, accessTokenClientV2, httpClient)

    val papirsykemeldingRegelService = PapirsykemeldingRegelService(
        legeSuspensjonClient = legeSuspensjonClient,
        norskHelsenettClient = norskHelsenettClient,
        syketilfelleClient = syketilfelleClient
    )
    val applicationEngine = createApplicationEngine(
        papirsykemeldingRegelService,
        jwkProvider,
        env,
        applicationState
    )

    val applicationServer = ApplicationServer(applicationEngine, applicationState)
    applicationServer.start()

    applicationState.ready = true
}
