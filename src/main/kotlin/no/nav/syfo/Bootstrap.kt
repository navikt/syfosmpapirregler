package no.nav.syfo

import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.module.kotlin.readValue
import java.net.URL
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import no.nav.syfo.application.ApplicationServer
import no.nav.syfo.client.ClientFactory
import no.nav.syfo.common.getObjectMapper
import no.nav.syfo.papirsykemelding.service.PapirsykemeldingRegelService

val CREDENTIALS_PATH = "/var/run/secrets/nais.io/vault/credentials.json"

fun main() {
    val env = Environment()
    val jwkProvider = JwkProviderBuilder(URL(env.jwkKeysUrl))
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    val credentials = getObjectMapper().readValue<VaultCredentials>(Paths.get(CREDENTIALS_PATH).toFile())
    val diskresjonskodeService = ClientFactory.createDiskresjonsKodeService(env, credentials)
    val papirsykemeldingRegelService = PapirsykemeldingRegelService(diskresjonskodeService = diskresjonskodeService)
    val applicationServer = ApplicationServer(env, jwkProvider, papirsykemeldingRegelService)
    applicationServer.start()
}
