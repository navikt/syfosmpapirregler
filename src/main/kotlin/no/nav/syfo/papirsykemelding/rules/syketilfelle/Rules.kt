package no.nav.syfo.papirsykemelding.rules.syketilfelle

import no.nav.syfo.model.AnnenFraverGrunn
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.papirsykemelding.model.sortedFOMDate
import no.nav.syfo.papirsykemelding.model.sortedTOMDate
import no.nav.syfo.papirsykemelding.rules.dsl.RuleResult
import no.nav.syfo.papirsykemelding.service.RuleMetadataSykmelding
import no.nav.syfo.sm.isICD10
import no.nav.syfo.sm.isICPC2
import java.time.LocalDate

typealias Rule<T> = (sykmelding: Sykmelding, metadata: RuleMetadataSykmelding) -> RuleResult<T>
typealias SyketilfelleRule = Rule<SyketilfelleRules>

val tilbakedatermerenn8dagerforstesykmelding: SyketilfelleRule = { sykmelding, metadata ->
    val erNyttSyketilfelle = metadata.erNyttSyketilfelle
    val behandletTidspunkt = metadata.ruleMetadata.behandletTidspunkt
    val forsteFomDato = sykmelding.perioder.sortedFOMDate().first()
    val begrunnelseIkkeKontakt = sykmelding.kontaktMedPasient.begrunnelseIkkeKontakt
    val erFraSpesialisthelsetjenesten = kommerFraSpesialisthelsetjenesten(sykmelding)
    val erCoronaRelatert = erCoronaRelatert(sykmelding)

    RuleResult(
        ruleInputs = mapOf(
            "erNyttSyketilfelle" to erNyttSyketilfelle,
            "behandletTidspunkt" to behandletTidspunkt
        ),
        rule = SyketilfelleRules.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING,
        ruleResult = erNyttSyketilfelle &&
            (behandletTidspunkt.toLocalDate() > forsteFomDato.plusDays(8) && begrunnelseIkkeKontakt.isNullOrEmpty()) &&
            !erFraSpesialisthelsetjenesten &&
            !erCoronaRelatert
    )
}

val tilbakedatermerenn8dagerforstesykmeldingmedbegrunnelse: SyketilfelleRule = { sykmelding, metadata ->
    val erNyttSyketilfelle = metadata.erNyttSyketilfelle
    val behandletTidspunkt = metadata.ruleMetadata.behandletTidspunkt
    val forsteFomDato = sykmelding.perioder.sortedFOMDate().first()
    val begrunnelseIkkeKontakt = sykmelding.kontaktMedPasient.begrunnelseIkkeKontakt
    val erFraSpesialisthelsetjenesten = kommerFraSpesialisthelsetjenesten(sykmelding)
    val erCoronaRelatert = erCoronaRelatert(sykmelding)

    RuleResult(
        ruleInputs = mapOf(
            "erNyttSyketilfelle" to erNyttSyketilfelle,
            "behandletTidspunkt" to behandletTidspunkt
        ),
        rule = SyketilfelleRules.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING_MED_BEGRUNNELSE,
        ruleResult = erNyttSyketilfelle &&
            behandletTidspunkt.toLocalDate() > forsteFomDato.plusDays(8) &&
            !begrunnelseIkkeKontakt.isNullOrEmpty() &&
            !erFraSpesialisthelsetjenesten &&
            !erCoronaRelatert
    )
}

val tilbakedateertintall8dagerutenkontakdaoogbegrunnelse: SyketilfelleRule = { sykmelding, metadata ->
    val erNyttSyketilfelle = metadata.erNyttSyketilfelle
    val behandletTidspunkt = metadata.ruleMetadata.behandletTidspunkt
    val forsteFomDato = sykmelding.perioder.sortedFOMDate().first()
    val sisteTomDato = sykmelding.perioder.sortedTOMDate().last()
    val begrunnelseIkkeKontakt = sykmelding.kontaktMedPasient.begrunnelseIkkeKontakt
    val kontaktMedPasientDato = sykmelding.kontaktMedPasient.kontaktDato
    val erFraSpesialisthelsetjenesten = kommerFraSpesialisthelsetjenesten(sykmelding)
    val erCoronaRelatert = erCoronaRelatert(sykmelding)

    RuleResult(
        ruleInputs = mapOf(
            "erNyttSyketilfelle" to erNyttSyketilfelle,
            "behandletTidspunkt" to behandletTidspunkt
        ),
        rule = SyketilfelleRules.TILBAKEDATERT_INNTIL_8_DAGER_UTEN_KONTAKTDATO_OG_BEGRUNNELSE,
        ruleResult = erNyttSyketilfelle &&
            behandletTidspunkt.toLocalDate() > forsteFomDato.plusDays(4) &&
            behandletTidspunkt.toLocalDate() <= sisteTomDato.plusDays(8) &&
            (kontaktMedPasientDato == null && begrunnelseIkkeKontakt.isNullOrEmpty()) &&
            !erFraSpesialisthelsetjenesten &&
            !erCoronaRelatert
    )
}

val tilbakedatertforlengelseover1mnd: SyketilfelleRule = { sykmelding, metadata ->
    val erNyttSyketilfelle = metadata.erNyttSyketilfelle
    val behandletTidspunkt = metadata.ruleMetadata.behandletTidspunkt
    val forsteFomDato = sykmelding.perioder.sortedFOMDate().first()
    val begrunnelseIkkeKontakt = sykmelding.kontaktMedPasient.begrunnelseIkkeKontakt
    val erFraSpesialisthelsetjenesten = kommerFraSpesialisthelsetjenesten(sykmelding)
    val erCoronaRelatert = erCoronaRelatert(sykmelding)

    RuleResult(
        ruleInputs = mapOf(
            "erNyttSyketilfelle" to erNyttSyketilfelle,
            "behandletTidspunkt" to behandletTidspunkt
        ),
        rule = SyketilfelleRules.TILBAKEDATERT_FORLENGELSE_OVER_1_MND,
        ruleResult = !erNyttSyketilfelle &&
            forsteFomDato < behandletTidspunkt.toLocalDate().minusMonths(1) &&
            begrunnelseIkkeKontakt.isNullOrEmpty() &&
            !erFraSpesialisthelsetjenesten &&
            !erCoronaRelatert
    )
}

val tilbakedertmedbegrunnelseforlengelse: SyketilfelleRule = { sykmelding, metadata ->
    val erNyttSyketilfelle = metadata.erNyttSyketilfelle
    val behandletTidspunkt = metadata.ruleMetadata.behandletTidspunkt
    val forsteFomDato = sykmelding.perioder.sortedFOMDate().first().atStartOfDay()
    val begrunnelseIkkeKontakt = sykmelding.kontaktMedPasient.begrunnelseIkkeKontakt
    val erFraSpesialisthelsetjenesten = kommerFraSpesialisthelsetjenesten(sykmelding)
    val erCoronaRelatert = erCoronaRelatert(sykmelding)

    RuleResult(
        ruleInputs = mapOf(
            "erNyttSyketilfelle" to erNyttSyketilfelle,
            "behandletTidspunkt" to behandletTidspunkt
        ),
        rule = SyketilfelleRules.TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE,
        ruleResult = !erNyttSyketilfelle &&
            behandletTidspunkt > forsteFomDato.plusDays(30) && !begrunnelseIkkeKontakt.isNullOrEmpty() &&
            !erFraSpesialisthelsetjenesten &&
            !erCoronaRelatert
    )
}

val koronaStartdato: LocalDate = LocalDate.of(2020, 2, 24)
val koronaSluttdato: LocalDate = LocalDate.of(2023, 1, 1)

fun erCoronaRelatert(sykmelding: Sykmelding): Boolean {
    return (
        (sykmelding.medisinskVurdering.hovedDiagnose?.isICPC2() ?: false && sykmelding.medisinskVurdering.hovedDiagnose?.kode == "R991") ||
            (sykmelding.medisinskVurdering.hovedDiagnose?.isICPC2() ?: false && sykmelding.medisinskVurdering.biDiagnoser.any { it.kode == "R991" }) ||
            (sykmelding.medisinskVurdering.hovedDiagnose?.isICPC2() ?: false && sykmelding.medisinskVurdering.hovedDiagnose?.kode == "R992") ||
            (sykmelding.medisinskVurdering.hovedDiagnose?.isICPC2() ?: false && sykmelding.medisinskVurdering.biDiagnoser.any { it.kode == "R992" }) ||
            (sykmelding.medisinskVurdering.hovedDiagnose?.isICD10() ?: false && sykmelding.medisinskVurdering.hovedDiagnose?.kode == "U071") ||
            (sykmelding.medisinskVurdering.hovedDiagnose?.isICD10() ?: false && sykmelding.medisinskVurdering.biDiagnoser.any { it.kode == "U071" }) ||
            (sykmelding.medisinskVurdering.hovedDiagnose?.isICD10() ?: false && sykmelding.medisinskVurdering.hovedDiagnose?.kode == "U072") ||
            (sykmelding.medisinskVurdering.hovedDiagnose?.isICD10() ?: false && sykmelding.medisinskVurdering.biDiagnoser.any { it.kode == "U072" }) ||
            sykmelding.medisinskVurdering.annenFraversArsak?.grunn?.any { it == AnnenFraverGrunn.SMITTEFARE } ?: false
        ) &&
        (sykmelding.perioder.any { it.fom.isAfter(koronaStartdato) } && sykmelding.perioder.any { it.fom.isBefore(koronaSluttdato) })
}

fun kommerFraSpesialisthelsetjenesten(sykmelding: Sykmelding): Boolean {
    return sykmelding.medisinskVurdering.hovedDiagnose?.isICD10() ?: false
}
