package no.nav.syfo.pdl

import io.kotest.core.spec.style.FunSpec
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.syfo.papirsykemelding.model.LoggingMeta
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.pdl.client.model.Foedsel
import no.nav.syfo.pdl.client.model.GraphQLResponse
import no.nav.syfo.pdl.client.model.HentPerson
import no.nav.syfo.pdl.client.model.PdlResponse
import org.amshove.kluent.shouldBeEqualTo
import java.time.LocalDate
import kotlin.test.assertFailsWith

class FodselsdatoServiceTest : FunSpec({
    val pdlClient = mockk<PdlClient>()
    val loggingMeta = LoggingMeta("mottakId", "orgNr", "msgId", "sykmeldingId")
    val fnr = "01038211111"

    context("FodselsdatoService") {
        test("Henter fødselsdato fra PDL") {
            coEvery { pdlClient.getPerson(any()) } returns GraphQLResponse(
                PdlResponse(hentPerson = HentPerson(listOf(Foedsel("1980-01-02")))),
                errors = null,
            )

            val fodselsdato = FodselsdatoService(pdlClient).getFodselsdato(fnr, loggingMeta)

            fodselsdato shouldBeEqualTo LocalDate.of(1980, 1, 2)
        }
        test("Utleder fødselsdato fra fnr hvis fødselsdato mangler i PDL") {
            coEvery { pdlClient.getPerson(any()) } returns GraphQLResponse(
                PdlResponse(hentPerson = HentPerson(emptyList())),
                errors = null,
            )
            val fodselsdato = FodselsdatoService(pdlClient).getFodselsdato(fnr, loggingMeta)

            fodselsdato shouldBeEqualTo LocalDate.of(1982, 3, 1)
        }
        test("Feiler hvis personen ikke finnes i PDL") {
            coEvery { pdlClient.getPerson(any()) } returns GraphQLResponse(
                PdlResponse(hentPerson = null),
                errors = null,
            )

            assertFailsWith<RuntimeException> {
                FodselsdatoService(pdlClient).getFodselsdato(fnr, loggingMeta)
            }
        }
    }
})
