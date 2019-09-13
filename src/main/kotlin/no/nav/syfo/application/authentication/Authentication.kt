package no.nav.syfo.application.authentication

import com.auth0.jwk.JwkProvider
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import no.nav.syfo.Environment
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log: Logger = LoggerFactory.getLogger("no.nav.syfo.smpapirregler.authentication")

fun Application.setupAuth(environment: Environment, jwkProvider: JwkProvider) {
    install(Authentication) {
        jwt {
            verifier(jwkProvider, environment.jwtIssuer)
            validate { credentials ->
                val appId: String = credentials.payload.getClaim("appid").asString()
                log.info("authorization attempt for $appId")
                if (appId in environment.appIds && environment.clientId in credentials.payload.audience) {
                    log.info("authorization ok")
                    return@validate JWTPrincipal(credentials.payload)
                }
                log.info("authorization failed")
                return@validate null
            }
        }
    }
}
