package no.nav.syfo.client.syketilfelle.model

import java.time.LocalDateTime
import no.nav.syfo.model.Periode

data class Syketilfelle(
    val aktorId: String,
    val orgnummer: String? = null,
    val inntruffet: LocalDateTime,
    val tags: String,
    val ressursId: String,
    val fom: LocalDateTime,
    val tom: LocalDateTime
)

fun List<Periode>.intoSyketilfelle(aktoerId: String, received: LocalDateTime, resourceId: String): List<Syketilfelle> =
    map {
        Syketilfelle(
            aktorId = aktoerId,
            orgnummer = null,
            inntruffet = received,
            tags = listOf(
                SyketilfelleTag.SYKMELDING, SyketilfelleTag.NY, SyketilfelleTag.PERIODE, when {
                    it.aktivitetIkkeMulig != null -> SyketilfelleTag.INGEN_AKTIVITET
                    it.reisetilskudd -> SyketilfelleTag.FULL_AKTIVITET
                    it.gradert != null -> SyketilfelleTag.GRADERT_AKTIVITET
                    it.behandlingsdager != null -> SyketilfelleTag.BEHANDLINGSDAGER
                    it.avventendeInnspillTilArbeidsgiver != null -> SyketilfelleTag.FULL_AKTIVITET
                    else -> throw RuntimeException("Could not find aktivitetstype, this should never happen")
                }
            ).joinToString(",") { tag -> tag.name },
            ressursId = resourceId,
            fom = it.fom.atStartOfDay(),
            tom = it.tom.atStartOfDay()
        )
    }
