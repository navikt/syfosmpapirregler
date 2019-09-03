package no.nav.syfo

import com.auth0.jwk.JwkProviderBuilder
import java.net.URL
import java.util.concurrent.TimeUnit
import no.nav.syfo.application.ApplicationServer

fun main() {
    val env = Environment()
    val jwkProvider = JwkProviderBuilder(URL(env.jwkKeysUrl))
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    val applicationServer = ApplicationServer(env, jwkProvider)
    applicationServer.start()
}
