package no.nav.syfo.client

import no.nav.syfo.Environment
import no.nav.syfo.VaultCredentials
import no.nav.syfo.ws.createPort
import no.nav.tjeneste.pip.diskresjonskode.DiskresjonskodePortType

class ClientFactory {
    companion object {
        fun createDiskresjonsKodeService(env: Environment, credentials: VaultCredentials): DiskresjonskodeService {
            val diskresjonskodePortType: DiskresjonskodePortType = createPort(env.diskresjonskodeEndpointUrl) {
                port { withSTS(credentials.serviceuserUsername, credentials.serviceuserPassword, env.securityTokenServiceURL) }
            }
            return DiskresjonskodeService(diskresjonskodePortType)
        }
    }
}
