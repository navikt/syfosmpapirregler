package no.nav.syfo.papirsykemelding.service

import io.ktor.util.KtorExperimentalAPI
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.prometheus.client.Counter
import kotlinx.coroutines.runBlocking
import no.nav.syfo.client.legesuspensjon.LegeSuspensjonClient
import no.nav.syfo.client.legesuspensjon.model.Suspendert
import no.nav.syfo.client.norskhelsenett.NorskHelsenettClient
import no.nav.syfo.client.syketilfelle.SyketilfelleClient
import no.nav.syfo.generatePeriode
import no.nav.syfo.generatePerioder
import no.nav.syfo.generateReceivedSykemelding
import no.nav.syfo.getBehandlerNotInHPRRule
import no.nav.syfo.getGyldigBehandler
import no.nav.syfo.model.Status
import no.nav.syfo.model.ValidationResult
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

@KtorExperimentalAPI
class PapirsykemeldingRegelServiceTest : Spek({

    val ruleHitCounter = mockk<Counter>()
    val ruleHitCounterChild = mockk<Counter.Child>()
    val legeSuspensjonsClient = mockk<LegeSuspensjonClient>()
    val syketilfelleClient = mockk<SyketilfelleClient>()
    val norskHelsenettClient = mockk<NorskHelsenettClient>()
    val service = PapirsykemeldingRegelService(
        ruleHitCounter,
        legeSuspensjonsClient,
        syketilfelleClient,
        norskHelsenettClient
    )

    beforeEachTest {
        io.mockk.clearMocks(ruleHitCounter, ruleHitCounterChild)
        every { ruleHitCounter.labels(any()) } returns ruleHitCounterChild
        every { ruleHitCounterChild.inc() } returns Unit
        coEvery { norskHelsenettClient.finnBehandler(any(), any(), any()) } returns getGyldigBehandler()
        coEvery { syketilfelleClient.finnStartdatoForSammenhengendeSyketilfelle(any(), any(), any()) } returns null
        coEvery { legeSuspensjonsClient.checkTherapist(any(), any(), any()) } returns Suspendert(false)
    }

    describe("Validate papirsykemelding") {
        it("Should validate papirsykemelding to be valid") {
            runBlocking {
                val result = service.validateSykemelding(generateReceivedSykemelding(generatePerioder()))
                result shouldEqual ValidationResult(Status.OK, emptyList())
                verify(exactly = 1) { ruleHitCounter.labels(any()) }
                verify(exactly = 1) { ruleHitCounter.labels("OK") }
                verify(exactly = 1) { ruleHitCounterChild.inc() }
            }
        }

        it("Should not validate sykemelding when behandler is null") {
            coEvery { norskHelsenettClient.finnBehandler(any(), any(), any()) } returns null
            runBlocking {
                val result = service.validateSykemelding(generateReceivedSykemelding())
                result shouldEqual getBehandlerNotInHPRRule()
                verify(exactly = 1) { ruleHitCounter.labels(any()) }
                verify(exactly = 1) { ruleHitCounter.labels("MANUAL_PROCESSING") }
                verify(exactly = 1) { ruleHitCounterChild.inc() }
            }
        }

        it("Should not validate sykmelding") {
            runBlocking {
                val result = service.validateSykemelding(
                    generateReceivedSykemelding(
                        listOf(
                            generatePeriode(
                                avventendeInnspillTilArbeidsgiver = "JA",
                                reisetilskudd = true
                            )
                        )
                    )
                )
                result.status shouldEqual Status.MANUAL_PROCESSING
                verify(exactly = 1) { ruleHitCounter.labels(any()) }
            }
        }
    }
})
