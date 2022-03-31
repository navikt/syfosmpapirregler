package no.nav.syfo.papirsykemelding.rules

import no.nav.syfo.model.AnnenFraverGrunn
import no.nav.syfo.model.Rule
import no.nav.syfo.model.Status
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.model.juridisk.JuridiskHenvisning
import no.nav.syfo.model.juridisk.Lovverk
import no.nav.syfo.papirsykemelding.model.RuleChain
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.papirsykemelding.model.sortedTOMDate
import no.nav.syfo.sm.isICD10
import no.nav.syfo.sm.isICPC2
import java.time.LocalDate

class SyketilfelleRuleChain(
    private val sykmelding: Sykmelding,
    private val ruleMetadataSykmelding: RuleMetadataAndForstegangsSykemelding,
) : RuleChain {
    override val rules: List<Rule<*>> = listOf(
        // §8-7 Legeerklæring kan ikke godtas for tidsrom før medlemmet ble undersøkt av lege.
        // En legeerklæring for tidsrom før medlemmet søkte lege kan likevel godtas dersom medlemmet har vært
        // forhidret fra å søke lege og det er godtgjort at han eller hun har vært arbeidsufør fra et tidligere tidspunkt.
        //
        // Dersom sykmeldingen er tilbakedatert mer enn 8 dager uten begrunnelse blir den avvist.
        //
        // Første gangs sykmelding er tilbakedatert mer enn 8 dager.
        Rule(
            name = "TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING",
            ruleId = 1204,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "Sykmeldingen er tilbakedatert uten begrunnelse fra den som sykmeldte deg.",
            messageForSender = "Første sykmelding er tilbakedatert mer enn det som er tillatt, eller felt 11.1 er ikke utfylt",
            juridiskHenvisning = JuridiskHenvisning(
                lovverk = Lovverk.FOLKETRYGDLOVEN,
                paragraf = "8-7",
                ledd = 2,
                punktum = null,
                bokstav = null
            ),
            input = object {
                val erNyttSyketilfelle = ruleMetadataSykmelding.erNyttSyketilfelle
                val behandletTidspunkt = ruleMetadataSykmelding.ruleMetadata.behandletTidspunkt
                val forsteFomDato = sykmelding.perioder.sortedFOMDate().first()
                val begrunnelseIkkeKontakt = sykmelding.kontaktMedPasient.begrunnelseIkkeKontakt
                val erFraSpesialisthelsetjenesten = kommerFraSpesialisthelsetjenesten(sykmelding)
                val erCoronaRelatert = erCoronaRelatert(sykmelding)
            },
            predicate = {
                it.erNyttSyketilfelle &&
                    (it.behandletTidspunkt.toLocalDate() > it.forsteFomDato.plusDays(8) && it.begrunnelseIkkeKontakt.isNullOrEmpty()) &&
                    !it.erFraSpesialisthelsetjenesten &&
                    !it.erCoronaRelatert
            }
        ),

        // §8-7 Legeerklæring kan ikke godtas for tidsrom før medlemmet ble undersøkt av lege.
        // En legeerklæring for tidsrom før medlemmet søkte lege kan likevel godtas dersom medlemmet har vært
        // forhidret fra å søke lege og det er godtgjort at han eller hun har vært arbeidsufør fra et tidligere tidspunkt.
        //
        // Tilbakedatert sykmelding med begrunnelse sendes til manuell vurdering.
        // Unntak dersom sykmeldingen kommer fra spesialisthelsetjenesten, erstatter en tidligere sykmelding, kun gjelder arbeidsgiverperioden eller er koronarelatert.
        //
        // Første gangs sykmelding er tilbakedatert mer enn 8 dager med begrunnelse.
        Rule(
            name = "TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING_MED_BEGRUNNELSE",
            ruleId = 1207,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "Første sykmelding er tilbakedatert og årsak for tilbakedatering er angitt.",
            messageForSender = "Første sykmelding er tilbakedatert og felt 11.2 (begrunnelseIkkeKontakt) er utfylt",
            juridiskHenvisning = JuridiskHenvisning(
                lovverk = Lovverk.FOLKETRYGDLOVEN,
                paragraf = "8-7",
                ledd = 2,
                punktum = null,
                bokstav = null
            ),
            input = object {
                val erNyttSyketilfelle = ruleMetadataSykmelding.erNyttSyketilfelle
                val behandletTidspunkt = ruleMetadataSykmelding.ruleMetadata.behandletTidspunkt
                val forsteFomDato = sykmelding.perioder.sortedFOMDate().first()
                val begrunnelseIkkeKontakt = sykmelding.kontaktMedPasient.begrunnelseIkkeKontakt
                val erFraSpesialisthelsetjenesten = kommerFraSpesialisthelsetjenesten(sykmelding)
                val erCoronaRelatert = erCoronaRelatert(sykmelding)
            },
            predicate = {
                it.erNyttSyketilfelle &&
                    it.behandletTidspunkt.toLocalDate() > it.forsteFomDato.plusDays(8) &&
                    !it.begrunnelseIkkeKontakt.isNullOrEmpty() &&
                    !it.erFraSpesialisthelsetjenesten &&
                    !it.erCoronaRelatert
            }
        ),

        // §8-7 Legeerklæring kan ikke godtas for tidsrom før medlemmet ble undersøkt av lege.
        // En legeerklæring for tidsrom før medlemmet søkte lege kan likevel godtas dersom medlemmet har vært
        // forhidret fra å søke lege og det er godtgjort at han eller hun har vært arbeidsufør fra et tidligere tidspunkt.
        //
        // Tilbakedateringer mellom 4 og 8 dager godtas ikke dersom sykmeldingen ikke inneholder dato for kontakt eller begrunnelse for tilbakedateringen.
        //
        // Første gangs sykmelding er tilbakedatert mindre enn 8 dager uten begrunnelse og kontaktdato.
        Rule(
            name = "TILBAKEDATERT_INNTIL_8_DAGER_UTEN_KONTAKTDATO_OG_BEGRUNNELSE",
            ruleId = 1204,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "Sykmeldingen er tilbakedatert uten begrunnelse eller uten at det er opplyst når du kontaktet den som sykmeldte deg.",
            messageForSender = "Første sykmelding er tilbakedatert uten at dato for kontakt (felt 11.1) eller at begrunnelse (felt 11.2) er utfylt",
            juridiskHenvisning = JuridiskHenvisning(
                lovverk = Lovverk.FOLKETRYGDLOVEN,
                paragraf = "8-7",
                ledd = 2,
                punktum = null,
                bokstav = null
            ),
            input = object {
                val erNyttSyketilfelle = ruleMetadataSykmelding.erNyttSyketilfelle
                val behandletTidspunkt = ruleMetadataSykmelding.ruleMetadata.behandletTidspunkt
                val forsteFomDato = sykmelding.perioder.sortedFOMDate().first()
                val sisteTomDato = sykmelding.perioder.sortedTOMDate().last()
                val begrunnelseIkkeKontakt = sykmelding.kontaktMedPasient.begrunnelseIkkeKontakt
                val kontaktMedPasientDato = sykmelding.kontaktMedPasient.kontaktDato
                val erFraSpesialisthelsetjenesten = kommerFraSpesialisthelsetjenesten(sykmelding)
                val erCoronaRelatert = erCoronaRelatert(sykmelding)
            },
            predicate = {
                it.erNyttSyketilfelle &&
                    it.behandletTidspunkt.toLocalDate() > it.forsteFomDato.plusDays(4) &&
                    it.behandletTidspunkt.toLocalDate() <= it.sisteTomDato.plusDays(8) &&
                    (it.kontaktMedPasientDato == null && it.begrunnelseIkkeKontakt.isNullOrEmpty()) &&
                    !it.erFraSpesialisthelsetjenesten &&
                    !it.erCoronaRelatert
            }
        ),

        // §8-7 Legeerklæring kan ikke godtas for tidsrom før medlemmet ble undersøkt av lege.
        // En legeerklæring for tidsrom før medlemmet søkte lege kan likevel godtas dersom medlemmet har vært
        // forhidret fra å søke lege og det er godtgjort at han eller hun har vært arbeidsufør fra et tidligere tidspunkt.
        //
        // Ved forelengelser er det også behov for begrunnelse dersom sykmeldingen er tilbakedatert mer enn 1 mnd. Ved manglende begrunnelse avvises sykmeldingen.
        //
        // Fom-dato i ny sykmelding som er en forlengelse kan maks være tilbakedatert 1 mnd fra behandlet-tidspunkt. Skal telles.
        Rule(
            name = "TILBAKEDATERT_FORLENGELSE_OVER_1_MND",
            ruleId = null,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "Sykmeldingen er tilbakedatert uten at det er opplyst når du kontaktet den som sykmeldte deg.",
            messageForSender = "Fom-dato i ny sykmelding som er en forlengelse kan maks være tilbakedatert 1 mnd fra behandlingstidspunkt og felt 11.1 er ikke utfylt",
            juridiskHenvisning = JuridiskHenvisning(
                lovverk = Lovverk.FOLKETRYGDLOVEN,
                paragraf = "8-7",
                ledd = 2,
                punktum = null,
                bokstav = null
            ),
            input = object {
                val erNyttSyketilfelle = ruleMetadataSykmelding.erNyttSyketilfelle
                val behandletTidspunkt = ruleMetadataSykmelding.ruleMetadata.behandletTidspunkt
                val forsteFomDato = sykmelding.perioder.sortedFOMDate().first()
                val begrunnelseIkkeKontakt = sykmelding.kontaktMedPasient.begrunnelseIkkeKontakt
                val erFraSpesialisthelsetjenesten = kommerFraSpesialisthelsetjenesten(sykmelding)
                val erCoronaRelatert = erCoronaRelatert(sykmelding)
            },
            predicate = {
                !it.erNyttSyketilfelle &&
                    it.forsteFomDato < it.behandletTidspunkt.toLocalDate().minusMonths(1) &&
                    it.begrunnelseIkkeKontakt.isNullOrEmpty() &&
                    !it.erFraSpesialisthelsetjenesten &&
                    !it.erCoronaRelatert
            }
        ),

        // §8-7 Legeerklæring kan ikke godtas for tidsrom før medlemmet ble undersøkt av lege.
        // En legeerklæring for tidsrom før medlemmet søkte lege kan likevel godtas dersom medlemmet har vært
        // forhidret fra å søke lege og det er godtgjort at han eller hun har vært arbeidsufør fra et tidligere tidspunkt.
        //
        // En tilbakedatert sykmelding må inneholde dato for kontakt eller begrunnelse for at den skal godkjennes.
        //
        // Sykmelding som er forlengelse er tilbakedatert mindre enn 30 dager uten begrunnelse og kontaktdato.
        Rule(
            name = "TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE",
            ruleId = 1207,
            status = Status.MANUAL_PROCESSING,
            messageForUser = "Sykmeldingen er tilbakedatert og årsak for tilbakedatering er angit",
            messageForSender = "Sykmeldingen er tilbakedatert og årsak for tilbakedatering er angit",
            juridiskHenvisning = JuridiskHenvisning(
                lovverk = Lovverk.FOLKETRYGDLOVEN,
                paragraf = "8-7",
                ledd = 2,
                punktum = null,
                bokstav = null
            ),
            input = object {
                val erNyttSyketilfelle = ruleMetadataSykmelding.erNyttSyketilfelle
                val behandletTidspunkt = ruleMetadataSykmelding.ruleMetadata.behandletTidspunkt
                val forsteFomDato = sykmelding.perioder.sortedFOMDate().first().atStartOfDay()
                val begrunnelseIkkeKontakt = sykmelding.kontaktMedPasient.begrunnelseIkkeKontakt
                val erFraSpesialisthelsetjenesten = kommerFraSpesialisthelsetjenesten(sykmelding)
                val erCoronaRelatert = erCoronaRelatert(sykmelding)
            },
            predicate = { input ->
                !input.erNyttSyketilfelle &&
                    input.behandletTidspunkt > input.forsteFomDato.plusDays(30) && !input.begrunnelseIkkeKontakt.isNullOrEmpty() &&
                    !input.erFraSpesialisthelsetjenesten &&
                    !input.erCoronaRelatert
            }
        )
    )
}

data class RuleMetadataAndForstegangsSykemelding(
    val ruleMetadata: RuleMetadata,
    val erNyttSyketilfelle: Boolean
)

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
        sykmelding.perioder.any { it.fom.isAfter(LocalDate.of(2020, 2, 24)) }
}

fun kommerFraSpesialisthelsetjenesten(sykmelding: Sykmelding): Boolean {
    return sykmelding.medisinskVurdering.hovedDiagnose?.isICD10() ?: false
}
