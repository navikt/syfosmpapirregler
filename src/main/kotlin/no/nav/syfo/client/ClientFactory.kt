package no.nav.syfo.client

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.apache.ApacheEngineConfig
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.network.sockets.SocketTimeoutException
import no.nav.syfo.Environment
import no.nav.syfo.VaultCredentials
import no.nav.syfo.application.exceptions.ServiceUnavailableException
import no.nav.syfo.client.legesuspensjon.LegeSuspensjonClient
import no.nav.syfo.client.norskhelsenett.NorskHelsenettClient
import no.nav.syfo.client.syketilfelle.SyketilfelleClient
import no.nav.syfo.common.getSerializer
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import java.net.ProxySelector

class ClientFactory {
    companion object {
        fun createSyketilfelleClient(
            env: Environment,
            accessTokenClientV2: AccessTokenClientV2,
            httpClient: HttpClient
        ): SyketilfelleClient {
            return SyketilfelleClient(env.syketilfelleEndpointURL, accessTokenClientV2, env.syketilfelleScope, httpClient)
        }

        fun createStsOidcClient(credentials: VaultCredentials, env: Environment): StsOidcClient {
            return StsOidcClient(credentials.serviceuserUsername, credentials.serviceuserPassword, env.securityTokenServiceURL)
        }

        fun createHttpClient(): HttpClient {
            return HttpClient(Apache, getHttpClientConfig())
        }

        private fun getHttpClientConfig(): HttpClientConfig<ApacheEngineConfig>.() -> Unit {
            val config: HttpClientConfig<ApacheEngineConfig>.() -> Unit = {
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
                expectSuccess = false
            }
            return config
        }

        fun createHttpClientProxy(): HttpClient {
            val proxyConfig: HttpClientConfig<ApacheEngineConfig>.() -> Unit = {
                getHttpClientConfig()()
                engine {
                    customizeClient {
                        setRoutePlanner(SystemDefaultRoutePlanner(ProxySelector.getDefault()))
                    }
                }
            }
            return HttpClient(Apache, proxyConfig)
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
            credentials: VaultCredentials,
            stsClient: StsOidcClient,
            httpClient: HttpClient
        ): LegeSuspensjonClient {
            return LegeSuspensjonClient(env.legeSuspensjonEndpointURL, credentials, stsClient, httpClient)
        }
    }
}
