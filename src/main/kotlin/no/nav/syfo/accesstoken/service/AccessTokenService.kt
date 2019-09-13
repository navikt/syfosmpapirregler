package no.nav.syfo.accesstoken.service

import java.time.Instant
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.nav.syfo.accesstoken.client.AccessTokenClient
import no.nav.syfo.accesstoken.model.AadAccessToken
import org.slf4j.LoggerFactory

class AccessTokenService(private val accessTokenClient: AccessTokenClient) {
    private val log = LoggerFactory.getLogger(AccessTokenService::class.java)

    private val mutex = Mutex()

    @Volatile
    private var tokenMap = HashMap<String, AadAccessToken>()

    suspend fun getAccessToken(resource: String): String {
        val omToMinutter = Instant.now().plusMillis(120L)
        return mutex.withLock {
            (tokenMap[resource]
                ?.takeUnless { it.expires_on.isBefore(omToMinutter) }
                ?: run {
                    log.info("Henter nytt token fra Azure AD")
                    val response = accessTokenClient.hentAccessToken(resource)
                    tokenMap[resource] = response
                    log.debug("Har hentet accesstoken")
                    return@run response
                }).access_token
        }
    }
}
