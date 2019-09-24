package no.nav.syfo.client.diskresjonskode

import com.ctc.wstx.exc.WstxException
import java.io.IOException
import no.nav.syfo.helpers.retry
import no.nav.tjeneste.pip.diskresjonskode.DiskresjonskodePortType
import no.nav.tjeneste.pip.diskresjonskode.meldinger.WSHentDiskresjonskodeRequest

class DiskresjonskodeService(private val diskresjonskodePortType: DiskresjonskodePortType) {

    suspend fun hentDiskresjonskode(ident: String): String = retry(
            callName = "hent_diskresjonskode",
            retryIntervals = arrayOf(500L, 1000L),
            legalExceptions = *arrayOf(IOException::class, WstxException::class)
    ) {
        diskresjonskodePortType.hentDiskresjonskode(WSHentDiskresjonskodeRequest().withIdent(ident)).diskresjonskode
    }
}
