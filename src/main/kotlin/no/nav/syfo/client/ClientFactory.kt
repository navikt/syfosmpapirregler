package no.nav.syfo.client

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.CIOEngineConfig
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.network.sockets.SocketTimeoutException
import no.nav.syfo.Environment
import no.nav.syfo.application.exceptions.ServiceUnavailableException
import no.nav.syfo.client.legesuspensjon.LegeSuspensjonClient
import no.nav.syfo.client.norskhelsenett.NorskHelsenettClient
import no.nav.syfo.client.syketilfelle.SyketilfelleClient
import no.nav.syfo.common.getSerializer
import no.nav.syfo.log

class ClientFactory {
    companion object {
        fun createSyketilfelleClient(
            env: Environment,
            accessTokenClientV2: AccessTokenClientV2,
            httpClient: HttpClient
        ): SyketilfelleClient {
            return SyketilfelleClient(env.syketilfelleEndpointURL, accessTokenClientV2, env.syketilfelleScope, httpClient)
        }

        fun createHttpClient(): HttpClient {
            return HttpClient(CIO, getHttpClientConfig())
        }

        private fun getHttpClientConfig(): HttpClientConfig<CIOEngineConfig>.() -> Unit {
            val config: HttpClientConfig<CIOEngineConfig>.() -> Unit = {
                install(ContentNegotiation) {
                    getSerializer()
                }
                HttpResponseValidator {
                    handleResponseExceptionWithRequest { exception, _ ->
                        when (exception) {
                            is SocketTimeoutException -> throw ServiceUnavailableException(exception.message)
                        }
                    }
                }
                install(HttpRequestRetry) {
                    constantDelay(100, 0, false)
                    retryOnExceptionIf(3) { request, throwable ->
                        log.warn("Caught exception ${throwable.message}, for url ${request.url}")
                        true
                    }
                    retryIf(maxRetries) { request, response ->
                        if (response.status.value.let { it in 500..599 }) {
                            log.warn("Retrying for statuscode ${response.status.value}, for url ${request.url}")
                            true
                        } else {
                            false
                        }
                    }
                }
                expectSuccess = false
            }
            return config
        }

        fun createNorskHelsenettClient(
            env: Environment,
            accessTokenClientV2: AccessTokenClientV2,
            httpClient: HttpClient
        ): NorskHelsenettClient {
            return NorskHelsenettClient(
                env.norskHelsenettEndpointURL,
                accessTokenClientV2,
                env.helsenettproxyScope,
                httpClient
            )
        }

        fun createLegeSuspensjonClient(
            env: Environment,
            accessTokenClientV2: AccessTokenClientV2,
            httpClient: HttpClient
        ): LegeSuspensjonClient {
            return LegeSuspensjonClient(env.legeSuspensjonProxyEndpointURL, accessTokenClientV2, httpClient, env.legeSuspensjonProxyScope)
        }
    }
}
