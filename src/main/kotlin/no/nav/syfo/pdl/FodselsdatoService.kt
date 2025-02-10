package no.nav.syfo.pdl

import com.github.benmanes.caffeine.cache.Caffeine
import java.time.Duration
import java.time.LocalDate
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.logger
import no.nav.syfo.metrics.FODSELSDATO_FRA_IDENT_COUNTER
import no.nav.syfo.metrics.FODSELSDATO_FRA_PDL_COUNTER
import no.nav.syfo.papirsykemelding.model.LoggingMeta
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.validering.extractBornDate

class FodselsdatoService(
    private val pdlClient: PdlClient,
) {
    private val cache =
        Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofHours(12))
            .maximumSize(500)
            .build<String, LocalDate>()

    suspend fun getFodselsdato(fnr: String, loggingMeta: LoggingMeta): LocalDate {
        val cachedFodselsdato = cache.getIfPresent(fnr)
        if (cachedFodselsdato != null) {
            logger.info("Fant fødselsdato i cache")
            return cachedFodselsdato
        }

        val pdlResponse = pdlClient.getPerson(fnr)
        if (pdlResponse.errors != null) {
            pdlResponse.errors.forEach {
                logger.error(
                    "PDL kastet error: ${it.message}, {}",
                    StructuredArguments.fields(loggingMeta)
                )
            }
        }
        if (pdlResponse.data.hentPerson == null) {
            logger.error(
                "Klarte ikke hente ut person fra PDL {}",
                StructuredArguments.fields(loggingMeta)
            )
            throw RuntimeException("Klarte ikke hente ut person fra PDL")
        }

        val fodselsdato =
            if (
                pdlResponse.data.hentPerson.foedselsdato?.firstOrNull()?.foedselsdato?.isNotEmpty() ==
                    true
            ) {
                logger.info(
                    "Bruker fødselsdato fra PDL {}",
                    StructuredArguments.fields(loggingMeta)
                )
                FODSELSDATO_FRA_PDL_COUNTER.inc()
                LocalDate.parse(pdlResponse.data.hentPerson.foedselsdato.first().foedselsdato)
            } else {
                logger.info(
                    "Utleder fødselsdato fra fnr {}",
                    StructuredArguments.fields(loggingMeta)
                )
                FODSELSDATO_FRA_IDENT_COUNTER.inc()
                extractBornDate(fnr)
            }
        cache.put(fnr, fodselsdato)
        return fodselsdato
    }
}
