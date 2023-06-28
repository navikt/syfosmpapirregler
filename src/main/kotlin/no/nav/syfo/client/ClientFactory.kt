package no.nav.syfo.client

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.apache.ApacheEngineConfig
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.network.sockets.SocketTimeoutException
import no.nav.syfo.EnvironmentVariables
import no.nav.syfo.ServiceUnavailableException
import no.nav.syfo.client.legesuspensjon.LegeSuspensjonClient
import no.nav.syfo.client.norskhelsenett.NorskHelsenettClient
import no.nav.syfo.client.syketilfelle.SyketilfelleClient
import no.nav.syfo.common.getSerializer
import no.nav.syfo.logger

class ClientFactory {
    companion object {
        fun createSyketilfelleClient(
            env: EnvironmentVariables,
            accessTokenClientV2: AccessTokenClientV2,
            httpClient: HttpClient,
        ): SyketilfelleClient {
            return SyketilfelleClient(
                env.syketilfelleEndpointURL,
                accessTokenClientV2,
                env.syketilfelleScope,
                httpClient
            )
        }

        fun createHttpClient(): HttpClient {
            return HttpClient(Apache, getHttpClientConfig())
        }

        private fun getHttpClientConfig(): HttpClientConfig<ApacheEngineConfig>.() -> Unit {
            val config: HttpClientConfig<ApacheEngineConfig>.() -> Unit = {
                install(ContentNegotiation) { getSerializer() }
                HttpResponseValidator {
                    handleResponseExceptionWithRequest { exception, _ ->
                        when (exception) {
                            is SocketTimeoutException ->
                                throw ServiceUnavailableException(exception.message)
                        }
                    }
                }
                install(HttpRequestRetry) {
                    constantDelay(100, 0, false)
                    retryOnExceptionIf(3) { request, throwable ->
                        logger.warn("Caught exception ${throwable.message}, for url ${request.url}")
                        true
                    }
                    retryIf(maxRetries) { request, response ->
                        if (response.status.value.let { it in 500..599 }) {
                            logger.warn(
                                "Retrying for statuscode ${response.status.value}, for url ${request.url}"
                            )
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
            env: EnvironmentVariables,
            accessTokenClientV2: AccessTokenClientV2,
            httpClient: HttpClient,
        ): NorskHelsenettClient {
            return NorskHelsenettClient(
                env.norskHelsenettEndpointURL,
                accessTokenClientV2,
                env.helsenettproxyScope,
                httpClient,
            )
        }

        fun createLegeSuspensjonClient(
            env: EnvironmentVariables,
            accessTokenClientV2: AccessTokenClientV2,
            httpClient: HttpClient,
        ): LegeSuspensjonClient {
            return LegeSuspensjonClient(
                env.legeSuspensjonProxyEndpointURL,
                accessTokenClientV2,
                httpClient,
                env.legeSuspensjonProxyScope
            )
        }
    }
}
