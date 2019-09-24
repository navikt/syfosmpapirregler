package no.nav.syfo.client.diskresjonskode

import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import java.io.IOException
import kotlin.test.assertFailsWith
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.tjeneste.pip.diskresjonskode.DiskresjonskodePortType
import no.nav.tjeneste.pip.diskresjonskode.meldinger.WSHentDiskresjonskodeResponse
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class DiskresjonskodeServiceTest : Spek({

    val diskresjonskodePortType = mockk<DiskresjonskodePortType>()
    val diskresjonskodeService = DiskresjonskodeService(diskresjonskodePortType)

    mockkStatic("kotlinx.coroutines.DelayKt")
    coEvery { delay(any()) } returns Unit

    beforeEachTest {
        clearMocks(diskresjonskodePortType)
    }
    describe("Test DiskresjonsKodeService") {
        runBlocking {
            every { diskresjonskodePortType.hentDiskresjonskode(any()) } returns WSHentDiskresjonskodeResponse().withDiskresjonskode("6")
            val diskresjonskode = diskresjonskodeService.hentDiskresjonskode("ident")
            diskresjonskode shouldBeEqualTo "6"
            verify(exactly = 1) { diskresjonskodePortType.hentDiskresjonskode(any()) }
        }
    }

    describe("Test retry of hentDiskresjonsKode") {
        it("Should get diskresjonskode when first call fails") {
            every { diskresjonskodePortType.hentDiskresjonskode(any()) } throws IOException("Exception")andThen
                    WSHentDiskresjonskodeResponse().withDiskresjonskode("2")

            runBlocking {
                val diskresjonsKode = diskresjonskodeService.hentDiskresjonskode("ident")
                diskresjonsKode shouldBeEqualTo "2"
                verify(exactly = 2) { diskresjonskodePortType.hentDiskresjonskode(any()) }
            }
        }
        it("Should throw Exception when retry max") {
            every { diskresjonskodePortType.hentDiskresjonskode(any()) } throws IOException("Exception")
            runBlocking {
                assertFailsWith<IOException> { diskresjonskodeService.hentDiskresjonskode("ident") }
                verify(exactly = 3) { diskresjonskodePortType.hentDiskresjonskode(any()) }
            }
        }
    }
})
