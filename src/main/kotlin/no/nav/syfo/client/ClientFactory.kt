package no.nav.syfo.client

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.apache.ApacheEngineConfig
import io.ktor.client.features.json.JsonFeature
import io.ktor.util.KtorExperimentalAPI
import java.net.ProxySelector
import no.nav.syfo.Environment
import no.nav.syfo.VaultCredentials
import no.nav.syfo.accesstoken.service.AccessTokenService
import no.nav.syfo.client.diskresjonskode.DiskresjonskodeService
import no.nav.syfo.client.legesuspensjon.LegeSuspensjonClient
import no.nav.syfo.client.norskhelsenett.NorskHelsenettClient
import no.nav.syfo.client.syketilfelle.SyketilfelleClient
import no.nav.syfo.common.getSerializer
import no.nav.syfo.ws.createPort
import no.nav.tjeneste.pip.diskresjonskode.DiskresjonskodePortType
import org.apache.http.impl.conn.SystemDefaultRoutePlanner

class ClientFactory {
    @KtorExperimentalAPI
    companion object {
        fun createDiskresjonsKodeService(env: Environment, credentials: VaultCredentials): DiskresjonskodeService {
            val diskresjonskodePortType: DiskresjonskodePortType = createPort(env.diskresjonskodeEndpointUrl) {
                port {
                    withSTS(
                        credentials.serviceuserUsername,
                        credentials.serviceuserPassword,
                        env.securityTokenServiceURL
                    )
                }
            }
            return DiskresjonskodeService(diskresjonskodePortType)
        }

        fun createSyketilfelleClient(env: Environment): SyketilfelleClient {
            return createSyketilfelleClient(env)
        }

        @KtorExperimentalAPI
        fun createStsOidcClient(credentials: VaultCredentials): StsOidcClient {
            return StsOidcClient(credentials.serviceuserUsername, credentials.serviceuserPassword)
        }

        fun createHttpClient(): HttpClient {
            return HttpClient(Apache, getHttpClientConfig())
        }

        private fun getHttpClientConfig(): HttpClientConfig<ApacheEngineConfig>.() -> Unit {
            val config: HttpClientConfig<ApacheEngineConfig>.() -> Unit = {
                install(JsonFeature) {
                    serializer = getSerializer()
                }
                expectSuccess = false
            }
            return config
        }

        fun createHttpClientProxy(): HttpClient {
            val proxyConfig: HttpClientConfig<ApacheEngineConfig>.() -> Unit = {
                getHttpClientConfig()
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
            accessTokenService: AccessTokenService,
            httpClient: HttpClient
        ): NorskHelsenettClient {
            return NorskHelsenettClient(
                env.norskHelsenettEndpointURL,
                accessTokenService,
                env.helsenettproxyId,
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
