package no.nav.syfo.papirsykemelding.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.prometheus.client.Counter
import no.nav.syfo.generatePerioder
import no.nav.syfo.generateReceivedSykemelding
import no.nav.syfo.getInvalidResult
import no.nav.syfo.model.Status
import no.nav.syfo.model.ValidationResult
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class PapirsykemeldingRegelServiceTest : Spek({

    val ruleHitCounter = mockk<Counter>()
    val ruleHitCounterChild = mockk<Counter.Child>()
    val service = PapirsykemeldingRegelService(ruleHitCounter)

    fun resetMocks() {
        io.mockk.clearMocks(ruleHitCounter, ruleHitCounterChild)
        every { ruleHitCounter.labels(any()) } returns ruleHitCounterChild
        every { ruleHitCounterChild.inc() } returns Unit
    }

    describe("Validate papirsykemelding") {
        it("Should validate papirsykemelding to be valid") {
            resetMocks()
            val result = service.validateSykemelding(generateReceivedSykemelding(generatePerioder()))
            result shouldEqual ValidationResult(Status.OK, emptyList())
            verify(exactly = 1) { ruleHitCounter.labels(any()) }
            verify(exactly = 1) { ruleHitCounter.labels("OK") }
            verify(exactly = 1) { ruleHitCounterChild.inc() }
        }

        it("Should validate papirsykemelding to be not valid") {
            resetMocks()
            val result = service.validateSykemelding(generateReceivedSykemelding())
            result shouldEqual getInvalidResult()
            verify(exactly = 1) { ruleHitCounter.labels(any()) }
            verify(exactly = 1) { ruleHitCounter.labels("INVALID") }
            verify(exactly = 1) { ruleHitCounterChild.inc() }
        }
    }
})
