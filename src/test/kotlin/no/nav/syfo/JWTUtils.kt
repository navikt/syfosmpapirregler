package no.nav.syfo

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.text.ParseException
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.UUID

const val keyId = "localhost-signer"

/* Brukes for til å hente ut pubkeyen som brukes til å validere tokens. Denne er noe som tilbyder av tokens (AzureAd)
   normalt tilbyr.
 */
fun fakeJWTApi(randomPort: Int): NettyApplicationEngine {
    return embeddedServer(Netty, randomPort) {
        routing {
            get("/fake.jwt") {
                call.respond(getFileAsString("src/test/resources/jwkset.json"))
            }
        }
    }.start(wait = false)
}

/* Utsteder en Bearer-token (En slik vi ber AzureAd om). OBS: Det er viktig at KeyId matcher kid i jwkset.json
 */
fun genereateJWT(
    consumerClientId: String? = "consumerClientId",
    audience: String? = "regel-clientId",
    expiry: LocalDateTime? = LocalDateTime.now().plusHours(1),
): String? {
    val now = Date()
    val key = getDefaultRSAKey()
    val alg = Algorithm.RSA256(key.toRSAPublicKey(), key.toRSAPrivateKey())

    return JWT.create()
        .withKeyId(keyId)
        .withSubject("subject")
        .withIssuer("https://sts.issuer.net/myidV2")
        .withAudience(audience)
        .withJWTId(UUID.randomUUID().toString())
        .withClaim("ver", "1.0")
        .withClaim("nonce", "myNonce")
        .withClaim("auth_time", now)
        .withClaim("nbf", now)
        .withClaim("iat", now)
        .withClaim("appid", consumerClientId)
        .withClaim("azp", consumerClientId)
        .withClaim("exp", Date.from(expiry?.atZone(ZoneId.systemDefault())?.toInstant()))
        .sign(alg)
}

private fun getDefaultRSAKey(): RSAKey {
    return getJWKSet().getKeyByKeyId(keyId) as RSAKey
}

private fun getJWKSet(): JWKSet {
    try {
        return JWKSet.parse(String(Files.readAllBytes(Paths.get("src/test/resources/jwkset.json")), StandardCharsets.UTF_8))
    } catch (io: IOException) {
        throw RuntimeException(io)
    } catch (io: ParseException) {
        throw RuntimeException(io)
    }
}
