package no.nav.syfo

import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.util.KtorExperimentalAPI
import java.net.URL
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import no.nav.syfo.accesstoken.client.AccessTokenClient
import no.nav.syfo.accesstoken.service.AccessTokenService
import no.nav.syfo.application.ApplicationServer
import no.nav.syfo.client.ClientFactory
import no.nav.syfo.common.getObjectMapper
import no.nav.syfo.papirsykemelding.service.PapirsykemeldingRegelService

val CREDENTIALS_PATH = "/var/run/secrets/nais.io/vault/credentials.json"

@KtorExperimentalAPI
fun main() {
    val env = Environment()
    val jwkProvider = JwkProviderBuilder(URL(env.jwkKeysUrl))
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    val credentials = getObjectMapper().readValue<VaultCredentials>(Paths.get(CREDENTIALS_PATH).toFile())
    val httpClient = ClientFactory.createHttpClient()
    val httpClientProxy = ClientFactory.createHttpClientProxy()
    val accessTokenService = AccessTokenService(AccessTokenClient(env.aadAccessTokenUrl, env.clientId, credentials.clientsecret, httpClientProxy))
    val stsClient = ClientFactory.createStsOidcClient(credentials)
    val syketilfelleClient = ClientFactory.createSyketilfelleClient(env, stsClient, httpClient)
    val legeSuspensjonClient = ClientFactory.createLegeSuspensjonClient(env, credentials, stsClient, httpClient)
    val norskHelsenettClient = ClientFactory.createNorskHelsenettClient(env, accessTokenService, httpClient)
    val diskresjonskodeService = ClientFactory.createDiskresjonsKodeService(env, credentials)

    val papirsykemeldingRegelService = PapirsykemeldingRegelService(
        diskresjonskodeService = diskresjonskodeService,
        legeSuspensjonClient = legeSuspensjonClient,
        norskHelsenettClient = norskHelsenettClient,
        syketilfelleClient = syketilfelleClient
    )
    val applicationServer = ApplicationServer(env, jwkProvider, papirsykemeldingRegelService)
    applicationServer.start()
}
