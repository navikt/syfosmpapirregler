package no.nav.syfo.papirsykemelding.rules

import no.nav.syfo.client.norskhelsenett.Behandler
import no.nav.syfo.client.norskhelsenett.Godkjenning
import no.nav.syfo.client.norskhelsenett.Kode
import no.nav.syfo.generatePeriode
import no.nav.syfo.generatePerioder
import no.nav.syfo.generateSykemelding
import no.nav.syfo.getGyldigBehandler
import no.nav.syfo.getUgyldigBehandler
import no.nav.syfo.papirsykemelding.model.HelsepersonellKategori
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.LocalDate

class HPRRuleChainTest : Spek({

    describe("1402 Behandler er ikke gyldig i HPR på konsultasjonstidspunktet") {
        it("Should not trigger rule") {
            HPRRuleChain(
                generateSykemelding(generatePerioder()),
                BehandlerOgStartdato(getGyldigBehandler(), null)
            ).getRuleByName("BEHANDLER_IKKE_GYLDIG_I_HPR").executeRule().result shouldBeEqualTo false
        }

        it("Should not trigger rule") {
            HPRRuleChain(
                generateSykemelding(),
                BehandlerOgStartdato(getUgyldigBehandler(), null)
            ).getRuleByName("BEHANDLER_IKKE_GYLDIG_I_HPR").executeRule().result shouldBeEqualTo true
        }
    }

    describe("1403 Behandler har ikke gyldig autorisasjon i HPR") {
        it("Should not trigger rule for kodeverdi") {
            val validAuthorizations: List<String> = listOf("1", "17", "4", "2", "14", "18")
            for (verdi in validAuthorizations) {
                val behandler = getBehandler(autorisasjon = Kode(true, 7704, verdi))
                HPRRuleChain(
                    generateSykemelding(),
                    BehandlerOgStartdato(behandler, null)
                ).getRuleByName("BEHANDLER_MANGLER_AUTORISASJON_I_HPR")
                    .executeRule()
                    .result shouldBeEqualTo false
            }
        }
        it("Should trigger rule when verdi is not valid") {
            val behandler = getBehandler(autorisasjon = Kode(true, 7704, ""))
            HPRRuleChain(
                generateSykemelding(),
                BehandlerOgStartdato(behandler, null)
            ).getRuleByName("BEHANDLER_MANGLER_AUTORISASJON_I_HPR").executeRule().result shouldBeEqualTo true
        }

        it("Should trigger rule when verdi is null") {
            val behandler = getBehandler(autorisasjon = Kode(true, 7704, null))
            HPRRuleChain(
                generateSykemelding(),
                BehandlerOgStartdato(behandler, null)
            ).getRuleByName("BEHANDLER_MANGLER_AUTORISASJON_I_HPR").executeRule().result shouldBeEqualTo true
        }

        it("Should trigger rule when not aktiv") {
            val behandler = getBehandler(autorisasjon = Kode(false, 7704, "1"))
            HPRRuleChain(
                generateSykemelding(),
                BehandlerOgStartdato(behandler, null)
            ).getRuleByName("BEHANDLER_MANGLER_AUTORISASJON_I_HPR").executeRule().result shouldBeEqualTo true
        }

        it("Should trigger rule when with incorrect oid") {
            val behandler = getBehandler(autorisasjon = Kode(true, 7703, "1"))
            HPRRuleChain(
                generateSykemelding(),
                BehandlerOgStartdato(behandler, null)
            ).getRuleByName("BEHANDLER_MANGLER_AUTORISASJON_I_HPR").executeRule().result shouldBeEqualTo true
        }
    }
    describe("1407 Behandler finnes i HPR men er ikke lege, kiropraktor, fysioterapeut eller tannlege") {
        it("Should not trigger rule") {
            val behandlerTyper = listOf("LE", "KI", "FT", "TL")
            for (type in behandlerTyper) {
                val behandler = getBehandler(
                    helsepersonellkategori = Kode(
                        aktiv = true,
                        verdi = type,
                        oid = 9060
                    )
                )
                HPRRuleChain(
                    generateSykemelding(),
                    BehandlerOgStartdato(behandler, null)
                ).getRuleByName("BEHANDLER_IKKE_LE_KI_MT_TL_FT_I_HPR").executeRule().result shouldBeEqualTo false
            }
        }
        it("Should trigger rule when not valid kategori") {
            val behandler = getBehandler(
                helsepersonellkategori = Kode(
                    aktiv = true,
                    verdi = "DD",
                    oid = 9060
                )
            )
            HPRRuleChain(
                generateSykemelding(),
                BehandlerOgStartdato(behandler, null)
            ).getRuleByName("BEHANDLER_IKKE_LE_KI_MT_TL_FT_I_HPR").executeRule().result shouldBeEqualTo true
        }
        it("Should trigger rule when helsepersonellkategori ikke er aktiv") {
            val behandler = getBehandler(
                helsepersonellkategori = Kode(
                    aktiv = false,
                    verdi = "LE",
                    oid = 9060
                )
            )
            val sykmelding = generateSykemelding()

            HPRRuleChain(
                sykmelding,
                BehandlerOgStartdato(behandler, null)
            ).getRuleByName("BEHANDLER_IKKE_LE_KI_MT_TL_FT_I_HPR").executeRule().result shouldBeEqualTo true
        }
    }

    describe("1519 BEHANDLER_MT_FT_KI_OVER_12_UKER") {

        it("Should not trigger rule when lege") {
            val behandler = getBehandler(
                helsepersonellkategori = Kode(
                    aktiv = true,
                    verdi = "LE",
                    oid = 9060
                )
            )
            val fomDate = LocalDate.of(2019, 1, 1)
            val sykemelding = generateSykemelding(
                listOf(
                    generatePeriode(
                        fom = fomDate,
                        tom = fomDate.plusWeeks(12).plusDays(1)
                    )
                )
            )

            HPRRuleChain(
                sykemelding,
                BehandlerOgStartdato(
                    behandler,
                    null
                )
            ).getRuleByName("BEHANDLER_MT_FT_KI_OVER_12_UKER")
                .executeRule()
                .result shouldBeEqualTo false
        }

        it("Should not trigger rule") {
            val behandler = getBehandler(
                helsepersonellkategori = Kode(
                    aktiv = true,
                    verdi = "KI",
                    oid = 9060
                )
            )
            val fomDate = LocalDate.of(2019, 1, 1)
            val sykemelding = generateSykemelding(
                listOf(
                    generatePeriode(
                        fom = fomDate,
                        tom = fomDate.plusWeeks(12)
                    )
                )
            )
            HPRRuleChain(
                sykemelding,
                BehandlerOgStartdato(
                    behandler,
                    null
                )
            ).getRuleByName("BEHANDLER_MT_FT_KI_OVER_12_UKER")
                .executeRule()
                .result shouldBeEqualTo false
        }

        it("BEHANDLER_MT_FT_KI_OVER_12_UKER, hould trigger rule") {
            val behandler = getBehandler(
                helsepersonellkategori = Kode(
                    aktiv = true,
                    verdi = "KI",
                    oid = 9060
                )
            )
            val fomDate = LocalDate.of(2019, 1, 1)
            val sykemelding = generateSykemelding(
                listOf(
                    generatePeriode(
                        fom = fomDate,
                        tom = fomDate.plusWeeks(12).plusDays(1)
                    )
                )
            )
            HPRRuleChain(
                sykemelding,
                BehandlerOgStartdato(
                    behandler,
                    LocalDate.of(2019, 1, 1)
                )
            ).getRuleByName("BEHANDLER_MT_FT_KI_OVER_12_UKER")
                .executeRule()
                .result shouldBeEqualTo true
        }

        it("Sjekker BEHANDLER_MT_FT_KI_OVER_12_UKER, slår ut fordi startdato for tidligere sykefravær gir varighet på mer enn 12 uker") {
            val sykmelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.of(2019, 3, 1),
                        tom = LocalDate.of(2019, 3, 27)
                    )
                )
            )

            val behandler = Behandler(
                listOf(
                    Godkjenning(
                        autorisasjon = Kode(
                            aktiv = true,
                            oid = 0,
                            verdi = ""
                        ),
                        helsepersonellkategori = Kode(
                            aktiv = true,
                            oid = 0,
                            verdi = HelsepersonellKategori.KIROPRAKTOR.verdi
                        )
                    )
                )
            )

            HPRRuleChain(
                sykmelding,
                BehandlerOgStartdato(
                    behandler,
                    LocalDate.of(2019, 1, 1)
                )
            ).getRuleByName("BEHANDLER_MT_FT_KI_OVER_12_UKER")
                .executeRule()
                .result shouldBeEqualTo true
        }

        it("Sjekker BEHANDLER_MT_FT_KI_OVER_12_UKER, slår ikke ut fordi det er nytt sykefravær") {
            val healthInformation = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.of(2019, 3, 1),
                        tom = LocalDate.of(2019, 3, 27)
                    )
                )
            )

            val behandler = Behandler(
                listOf(
                    Godkjenning(
                        autorisasjon = Kode(
                            aktiv = true,
                            oid = 0,
                            verdi = ""
                        ),
                        helsepersonellkategori = Kode(
                            aktiv = true,
                            oid = 0,
                            verdi = HelsepersonellKategori.KIROPRAKTOR.verdi
                        )
                    )
                )
            )

            HPRRuleChain(
                healthInformation,
                BehandlerOgStartdato(behandler, null)
            ).getRuleByName("BEHANDLER_MT_FT_KI_OVER_12_UKER")
                .executeRule()
                .result shouldBeEqualTo false
        }

        it("Sjekker BEHANDLER_MT_FT_KI_OVER_12_UKER, slår ikke ut fordi behandler er Lege(LE) og Kiropraktor(KI)") {
            val healthInformation = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.of(2019, 3, 1),
                        tom = LocalDate.of(2019, 3, 27)
                    )
                )
            )

            val behandler = Behandler(
                listOf(
                    Godkjenning(
                        autorisasjon = Kode(
                            aktiv = true,
                            oid = 0,
                            verdi = ""
                        ),
                        helsepersonellkategori = Kode(
                            aktiv = true,
                            oid = 0,
                            verdi = HelsepersonellKategori.KIROPRAKTOR.verdi
                        )
                    ),
                    Godkjenning(
                        autorisasjon = Kode(
                            aktiv = true,
                            oid = 0,
                            verdi = ""
                        ),
                        helsepersonellkategori = Kode(
                            aktiv = true,
                            oid = 0,
                            verdi = HelsepersonellKategori.LEGE.verdi
                        )
                    )
                )
            )

            HPRRuleChain(healthInformation, BehandlerOgStartdato(behandler, LocalDate.of(2019, 1, 1)))
                .getRuleByName("BEHANDLER_MT_FT_KI_OVER_12_UKER")
                .executeRule()
                .result shouldBeEqualTo false
        }

        it("Sjekker BEHANDLER_MT_FT_KI_OVER_12_UKER, slår ikke ut fordi startdato for tidligere sykefravær gir varighet på mindre enn 12 uker") {
            val healthInformation = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.of(2019, 3, 1),
                        tom = LocalDate.of(2019, 3, 27)
                    )
                )
            )

            val behandler = Behandler(
                listOf(
                    Godkjenning(
                        autorisasjon = Kode(
                            aktiv = true,
                            oid = 0,
                            verdi = ""
                        ),
                        helsepersonellkategori = Kode(
                            aktiv = true,
                            oid = 0,
                            verdi = HelsepersonellKategori.KIROPRAKTOR.verdi
                        )
                    )
                )
            )

            HPRRuleChain(
                healthInformation,
                BehandlerOgStartdato(
                    behandler,
                    LocalDate.of(2019, 2, 20)
                )
            ).getRuleByName("BEHANDLER_MT_FT_KI_OVER_12_UKER")
                .executeRule().result shouldBeEqualTo false
        }
    }
})

private fun getBehandler(
    autorisasjon: Kode = Kode(true, 7704, "1"),
    helsepersonellkategori: Kode = Kode(
        aktiv = false,
        verdi = "LE",
        oid = 9060
    )
) =
    Behandler(
        listOf(
            Godkjenning(
                helsepersonellkategori,
                autorisasjon
            )
        )
    )
