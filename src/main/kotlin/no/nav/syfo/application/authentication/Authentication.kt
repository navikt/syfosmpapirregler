package no.nav.syfo.application.authentication

import com.auth0.jwk.JwkProvider
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.Principal
import io.ktor.auth.jwt.JWTCredential
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.Environment
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log: Logger = LoggerFactory.getLogger("no.nav.syfo.smpapirregler.authentication")

fun Application.setupAuth(
    environment: Environment,
    jwkProvider: JwkProvider,
    jwkProviderAadV2: JwkProvider
) {
    install(Authentication) {
        jwt("servicebrukerAADv1") {
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
        jwt(name = "servicebrukerAADv2") {
            verifier(jwkProviderAadV2, environment.jwtIssuerV2)
            validate { credentials ->
                when {
                    harTilgang(credentials, environment.clientIdV2) -> JWTPrincipal(credentials.payload)
                    else -> unauthorized(credentials)
                }
            }
        }
    }
}

fun harTilgang(credentials: JWTCredential, clientId: String): Boolean {
    val appid: String = credentials.payload.getClaim("azp").asString()
    log.debug("authorization attempt for $appid")
    return credentials.payload.audience.contains(clientId)
}

fun unauthorized(credentials: JWTCredential): Principal? {
    log.warn(
        "Auth: Unexpected audience for jwt {}, {}",
        StructuredArguments.keyValue("issuer", credentials.payload.issuer),
        StructuredArguments.keyValue("audience", credentials.payload.audience)
    )
    return null
}
