package no.nav.syfo.papirsykemelding.rules

import no.nav.syfo.client.norskhelsenett.Behandler
import no.nav.syfo.model.Status
import no.nav.syfo.papirsykemelding.model.HelsepersonellKategori
import no.nav.syfo.rules.Description
import no.nav.syfo.rules.Rule
import no.nav.syfo.rules.RuleData

enum class HPRRuleChain(
    override val ruleId: Int?,
    override val status: Status,
    override val messageForUser: String,
    override val messageForSender: String,
    override val predicate: (RuleData<Behandler>) -> Boolean
) : Rule<RuleData<Behandler>> {

    @Description("Behandler er ikke gyldig i HPR på konsultasjonstidspunkt")
    BEHANDLER_IKKE_GYLDIG_I_HPR(
        1402,
        Status.MANUAL_PROCESSING,
        "Den som skrev sykmeldingen manglet autorisasjon.",
        "Behandler er ikke gyldig i HPR på konsultasjonstidspunkt", { (_, behandler) ->
            !behandler.godkjenninger.any {
                it.autorisasjon?.aktiv != null && it.autorisasjon.aktiv
            }
        }),

    @Description("Behandler har ikke gyldig autorisasjon i HPR")
    BEHANDLER_MANGLER_AUTORISASJON_I_HPR(
        1403,
        Status.MANUAL_PROCESSING,
        "Den som skrev sykmeldingen manglet autorisasjon.",
        "Behandler har ikke til gyldig autorisasjon i HPR", { (_, behandler) ->
            !behandler.godkjenninger.any {
                it.autorisasjon?.aktiv != null &&
                        it.autorisasjon.aktiv &&
                        it.autorisasjon.oid == 7704 &&
                        it.autorisasjon.verdi != null &&
                        it.autorisasjon.verdi in arrayOf("1", "17", "4", "2", "14", "18")
            }
        }),

    @Description("Behandler finnes i HPR men er ikke lege, kiropraktor, fysioterapeut eller tannlege")
    BEHANDLER_IKKE_LE_KI_TL_FT_I_HPR(
        1407,
        Status.MANUAL_PROCESSING,
        "Den som skrev sykmeldingen manglet autorisasjon.",
        "Behandler finnes i HPR men er ikke lege, kiropraktor, fysioterapeut eller tannlege",
        { (_, behandler) ->
            !behandler.godkjenninger.any {
                it.helsepersonellkategori?.aktiv != null &&
                        it.autorisasjon?.aktiv == true && it.helsepersonellkategori.verdi != null &&
                        harAktivHelsepersonellAutorisasjonsSom(
                            behandler, listOf(
                                HelsepersonellKategori.LEGE.verdi,
                                HelsepersonellKategori.KIROPRAKTOR.verdi,
                                HelsepersonellKategori.TANNLEGE.verdi,
                                HelsepersonellKategori.FYSIOTERAPAEUT.verdi
                            )
                        )
            }
        }),

    @Description("Hvis en sykmelding fra kiropraktor eller fysioterapeut overstiger 12 uker regnet fra første sykefraværsdag skal meldingen avvises")
    BEHANDLER_FT_KI_OVER_12_UKER(
        1519,
        Status.MANUAL_PROCESSING,
        "Den som skrev sykmeldingen mangler autorisasjon.",
        "Behandler er kiropraktor eller fysioterapeut overstiger 12 uker regnet fra første sykefraværsdag",
        { (sykmelding, behandler) ->
            sykmelding.perioder.any { (it.fom..it.tom).daysBetween() > 84 } &&
                    !harAktivHelsepersonellAutorisasjonsSom(
                        behandler, listOf(
                            HelsepersonellKategori.LEGE.verdi,
                            HelsepersonellKategori.TANNLEGE.verdi
                        )
                    ) &&
                    harAktivHelsepersonellAutorisasjonsSom(
                        behandler, listOf(
                            HelsepersonellKategori.KIROPRAKTOR.verdi,
                            HelsepersonellKategori.FYSIOTERAPAEUT.verdi
                        )
                    )
        }),
}

fun harAktivHelsepersonellAutorisasjonsSom(behandler: Behandler, helsepersonerVerdi: List<String>): Boolean =
    behandler.godkjenninger.any { godkjenning ->
        godkjenning.helsepersonellkategori?.aktiv != null &&
                godkjenning.autorisasjon?.aktiv == true && godkjenning.helsepersonellkategori.verdi != null &&
                godkjenning.helsepersonellkategori.let {
                    it.aktiv && it.verdi in helsepersonerVerdi
                }
    }
