package no.nav.syfo.pdl

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.syfo.papirsykemelding.model.LoggingMeta
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.pdl.client.model.Foedsel
import no.nav.syfo.pdl.client.model.GraphQLResponse
import no.nav.syfo.pdl.client.model.HentPerson
import no.nav.syfo.pdl.client.model.PdlResponse
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.LocalDate
import kotlin.test.assertFailsWith

class FodselsdatoServiceTest : Spek({
    val pdlClient = mockk<PdlClient>()
    val loggingMeta = LoggingMeta("mottakId", "orgNr", "msgId", "sykmeldingId")
    val fnr = "01038211111"

    describe("FodselsdatoService") {
        it("Henter fødselsdato fra PDL") {
            coEvery { pdlClient.getPerson(any()) } returns GraphQLResponse(
                PdlResponse(hentPerson = HentPerson(listOf(Foedsel("1980-01-02")))),
                errors = null
            )

            runBlocking {
                val fodselsdato = FodselsdatoService(pdlClient).getFodselsdato(fnr, loggingMeta)

                fodselsdato shouldBeEqualTo LocalDate.of(1980, 1, 2)
            }
        }
        it("Utleder fødselsdato fra fnr hvis fødselsdato mangler i PDL") {
            coEvery { pdlClient.getPerson(any()) } returns GraphQLResponse(
                PdlResponse(hentPerson = HentPerson(emptyList())),
                errors = null
            )

            runBlocking {
                val fodselsdato = FodselsdatoService(pdlClient).getFodselsdato(fnr, loggingMeta)

                fodselsdato shouldBeEqualTo LocalDate.of(1982, 3, 1)
            }
        }
        it("Feiler hvis personen ikke finnes i PDL") {
            coEvery { pdlClient.getPerson(any()) } returns GraphQLResponse(
                PdlResponse(hentPerson = null),
                errors = null
            )

            runBlocking {
                assertFailsWith<RuntimeException> {
                    FodselsdatoService(pdlClient).getFodselsdato(fnr, loggingMeta)
                }
            }
        }
    }
})
