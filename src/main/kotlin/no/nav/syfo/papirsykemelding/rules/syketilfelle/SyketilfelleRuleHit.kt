package no.nav.syfo.papirsykemelding.rules.syketilfelle

import no.nav.syfo.model.Status
import no.nav.syfo.model.juridisk.JuridiskHenvisning
import no.nav.syfo.model.juridisk.Lovverk
import no.nav.syfo.papirsykemelding.rules.common.RuleHit

fun syketilfelleJuridiskHenvisning(): JuridiskHenvisning {
    return JuridiskHenvisning(
        lovverk = Lovverk.FOLKETRYGDLOVEN,
        paragraf = "8-7",
        ledd = 2,
        punktum = null,
        bokstav = null
    )
}

enum class SyketilfelleRuleHit(
    val ruleHit: RuleHit
) {
    TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING(
        ruleHit = RuleHit(
            rule = "TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING",
            status = Status.MANUAL_PROCESSING,
            messageForSender = "Første sykmelding er tilbakedatert mer enn det som er tillatt, eller felt 11.1 er ikke utfylt",
            messageForUser = "Sykmeldingen er tilbakedatert uten begrunnelse fra den som sykmeldte deg."
        )
    ),
    TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING_MED_BEGRUNNELSE(
        ruleHit = RuleHit(
            rule = "TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING_MED_BEGRUNNELSE",
            status = Status.MANUAL_PROCESSING,
            messageForSender = "Første sykmelding er tilbakedatert og felt 11.2 (begrunnelseIkkeKontakt) er utfylt",
            messageForUser = "Første sykmelding er tilbakedatert og årsak for tilbakedatering er angitt."
        )
    ),
    TILBAKEDATERT_INNTIL_8_DAGER_UTEN_KONTAKTDATO_OG_BEGRUNNELSE(
        ruleHit = RuleHit(
            rule = "TILBAKEDATERT_INNTIL_8_DAGER_UTEN_KONTAKTDATO_OG_BEGRUNNELSE",
            status = Status.MANUAL_PROCESSING,
            messageForSender = "Første sykmelding er tilbakedatert uten at dato for kontakt (felt 11.1) eller at begrunnelse (felt 11.2) er utfylt",
            messageForUser = "Sykmeldingen er tilbakedatert uten begrunnelse eller uten at det er opplyst når du kontaktet den som sykmeldte deg."
        )
    ),
    TILBAKEDATERT_FORLENGELSE_OVER_1_MND(
        ruleHit = RuleHit(
            rule = "TILBAKEDATERT_FORLENGELSE_OVER_1_MND",
            status = Status.MANUAL_PROCESSING,
            messageForSender = "Fom-dato i ny sykmelding som er en forlengelse kan maks være tilbakedatert 1 mnd fra behandlingstidspunkt og felt 11.1 er ikke utfylt",
            messageForUser = "Sykmeldingen er tilbakedatert uten at det er opplyst når du kontaktet den som sykmeldte deg."
        )
    ),
    TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE(
        ruleHit = RuleHit(
            rule = "TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE",
            status = Status.MANUAL_PROCESSING,
            messageForSender = "Sykmeldingen er tilbakedatert og årsak for tilbakedatering er angit",
            messageForUser = "Sykmeldingen er tilbakedatert og årsak for tilbakedatering er angit"
        )
    )
}
