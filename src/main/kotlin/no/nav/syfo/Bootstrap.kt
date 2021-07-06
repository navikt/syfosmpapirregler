package no.nav.syfo

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.util.KtorExperimentalAPI
import io.prometheus.client.hotspot.DefaultExports
import java.net.URL
import java.util.concurrent.TimeUnit
import no.nav.syfo.accesstoken.client.AccessTokenClient
import no.nav.syfo.accesstoken.service.AccessTokenService
import no.nav.syfo.application.ApplicationServer
import no.nav.syfo.client.ClientFactory
import no.nav.syfo.papirsykemelding.service.PapirsykemeldingRegelService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log: Logger = LoggerFactory.getLogger("no.nav.syfo.smpapirregler")

@KtorExperimentalAPI
fun main() {

    DefaultExports.initialize()

    val env = Environment()

    val jwkProvider = JwkProviderBuilder(URL(env.jwkKeysUrl))
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    val credentials = VaultCredentials()
    val httpClient = ClientFactory.createHttpClient()
    val httpClientProxy = ClientFactory.createHttpClientProxy()
    val accessTokenService = AccessTokenService(
        AccessTokenClient(
            env.aadAccessTokenUrl,
            env.clientId,
            credentials.clientsecret,
            httpClientProxy
        )
    )
    val stsClient = ClientFactory.createStsOidcClient(credentials, env)
    val syketilfelleClient = ClientFactory.createSyketilfelleClient(env, stsClient, httpClient)
    val legeSuspensjonClient = ClientFactory.createLegeSuspensjonClient(env, credentials, stsClient, httpClient)
    val norskHelsenettClient = ClientFactory.createNorskHelsenettClient(env, accessTokenService, httpClient)

    val papirsykemeldingRegelService = PapirsykemeldingRegelService(
        legeSuspensjonClient = legeSuspensjonClient,
        norskHelsenettClient = norskHelsenettClient,
        syketilfelleClient = syketilfelleClient
    )
    val applicationServer = ApplicationServer(env, jwkProvider, papirsykemeldingRegelService)
    applicationServer.start()
}
