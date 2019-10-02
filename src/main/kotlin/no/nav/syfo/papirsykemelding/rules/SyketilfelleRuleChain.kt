package no.nav.syfo.papirsykemelding.rules

import no.nav.syfo.model.Status
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.papirsykemelding.model.sortedFOMDate
import no.nav.syfo.rules.Description
import no.nav.syfo.rules.Rule
import no.nav.syfo.rules.RuleData

enum class SyketilfelleRuleChain(
    override val ruleId: Int?,
    override val status: Status,
    override val messageForUser: String,
    override val messageForSender: String,
    override val predicate: (RuleData<RuleMetadataAndForstegangsSykemelding>) -> Boolean
) : Rule<RuleData<RuleMetadataAndForstegangsSykemelding>> {
    @Description("Første gangs sykmelding er tilbakedatert mer enn 8 dager.")
    TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING(
        1204,
        Status.MANUAL_PROCESSING,
        "Sykmeldingen er tilbakedatert uten at det er opplyst når du kontaktet den som sykmeldte deg.",
        "Første sykmelding er tilbakedatert mer enn det som er tillatt, eller felt 11.1 er ikke utfylt",
        { (sykmelding, ruleMetadataAndForstegangsSykemelding) ->
            ruleMetadataAndForstegangsSykemelding.erNyttSyketilfelle &&
                    ruleMetadataAndForstegangsSykemelding.ruleMetadata.signatureDate > sykmelding.perioder.sortedFOMDate().first().atStartOfDay().plusDays(
                8
            ) &&
                    sykmelding.kontaktMedPasient.kontaktDato == null
        }),

    @Description("Første gangs sykmelding er tilbakedatert mindre enn 8 dager.")
    TILBAKEDATERT_INNTIL_8_DAGER_UTEN_KONTAKTDATO(
        1204,
        Status.MANUAL_PROCESSING,
        "Sykmeldingen er tilbakedatert uten at det er opplyst når du kontaktet den som sykmeldte deg.",
        "Første sykmelding er tilbakedatert uten at dato for kontakt er angitt eller felt 11.1 er ikke utfylt",
        { (sykmelding, ruleMetadataAndForstegangsSykemelding) ->
            ruleMetadataAndForstegangsSykemelding.erNyttSyketilfelle &&
                    ruleMetadataAndForstegangsSykemelding.ruleMetadata.signatureDate > sykmelding.perioder.sortedFOMDate().first().atStartOfDay().plusDays(
                4
            ) &&
                    ruleMetadataAndForstegangsSykemelding.ruleMetadata.signatureDate <= sykmelding.perioder.sortedFOMDate().first().atStartOfDay().plusDays(
                8
            ) &&
                    sykmelding.kontaktMedPasient.kontaktDato == null
        }),

    @Description("Fom-dato i ny sykmelding som er en forlengelse kan maks være tilbakedatert 1 mnd fra signaturdato. Skal telles.")
    TILBAKEDATERT_FORLENGELSE_OVER_1_MND(
        null,
        Status.MANUAL_PROCESSING,
        "Sykmeldingen er tilbakedatert uten at det er opplyst når du kontaktet den som sykmeldte deg.",
        "Fom-dato i ny sykmelding som er en forlengelse kan maks være tilbakedatert 1 mnd fra signaturdato og felt 11.1 er ikke utfylt",
        { (sykmelding, ruleMetadataAndForstegangsSykemelding) ->
            !ruleMetadataAndForstegangsSykemelding.erNyttSyketilfelle &&
                    sykmelding.perioder.sortedFOMDate().first().minusMonths(1).atStartOfDay() > ruleMetadataAndForstegangsSykemelding.ruleMetadata.signatureDate &&
                    sykmelding.kontaktMedPasient.kontaktDato == null
        }),

    @Description("Sykmeldingens fom-dato er inntil 3 år tilbake i tid og årsak for tilbakedatering er angitt.")
    TILBAKEDATERT_MED_BEGRUNNELSE_FORSTE_SYKMELDING(
        1207,
        Status.MANUAL_PROCESSING,
        "Sykmeldingen er tilbakedatert og årsak for tilbakedatering er angitt",
        "Sykmeldingen er tilbakedatert og årsak for tilbakedatering er angitt",
        { (sykemelding, ruleMetadataAndForstegangsSykemelding) ->
            ruleMetadataAndForstegangsSykemelding.erNyttSyketilfelle &&
                    ruleMetadataAndForstegangsSykemelding.ruleMetadata.signatureDate > sykemelding.perioder.sortedFOMDate().first().atStartOfDay().plusDays(
                8
            ) &&
                    !sykemelding.kontaktMedPasient.begrunnelseIkkeKontakt.isNullOrEmpty()
        }),

    @Description("Sykmeldingens fom-dato er inntil 3 år tilbake i tid og årsak for tilbakedatering er angitt.")
    TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE(
        1207,
        Status.MANUAL_PROCESSING,
        "Sykmeldingen er tilbakedatert og årsak for tilbakedatering er angitt",
        "Sykmeldingen er tilbakedatert og årsak for tilbakedatering er angitt",
        { (sykemelding, ruleMetadataAndForstegangsSykemelding) ->
            !ruleMetadataAndForstegangsSykemelding.erNyttSyketilfelle &&
                    ruleMetadataAndForstegangsSykemelding.ruleMetadata.signatureDate > sykemelding.perioder.sortedFOMDate().first().atStartOfDay().plusDays(
                30
            ) &&
                    !sykemelding.kontaktMedPasient.begrunnelseIkkeKontakt.isNullOrEmpty()
        }),
}

data class RuleMetadataAndForstegangsSykemelding(
    val ruleMetadata: RuleMetadata,
    val erNyttSyketilfelle: Boolean
)
