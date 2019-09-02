package no.nav.syfo.authentication

import com.auth0.jwk.JwkProvider
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import no.nav.syfo.Environment

fun Application.setupAuth(environment: Environment, jwkProvider: JwkProvider) {
    install(Authentication) {
        jwt {
            verifier(jwkProvider, environment.jwtIssuer)
            validate { credentials ->
                val appId: String = credentials.payload.getClaim("appid").asString()
                if (appId in environment.appIds && environment.clientId in credentials.payload.audience) {
                    return@validate JWTPrincipal(credentials.payload)
                }
                return@validate null
            }
        }
    }
}
