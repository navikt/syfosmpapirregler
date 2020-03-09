package no.nav.syfo.papirsykemelding.rules

import java.time.LocalDate
import no.nav.syfo.client.norskhelsenett.Behandler
import no.nav.syfo.client.norskhelsenett.Godkjenning
import no.nav.syfo.client.norskhelsenett.Kode
import no.nav.syfo.generatePeriode
import no.nav.syfo.generatePerioder
import no.nav.syfo.generateSykemelding
import no.nav.syfo.getGyldigBehandler
import no.nav.syfo.getUgyldigBehandler
import no.nav.syfo.rules.RuleData
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class HPRRuleChainTest : Spek({

    describe("1402 Behandler er ikke gyldig i HPR på konsultasjonstidspunktet") {
        it("Should not trigger rule") {
            HPRRuleChain.BEHANDLER_IKKE_GYLDIG_I_HPR(
                RuleData(
                    generateSykemelding(generatePerioder()),
                    getGyldigBehandler()
                )
            ) shouldEqual false
        }

        it("Should not trigger rule") {
            HPRRuleChain.BEHANDLER_IKKE_GYLDIG_I_HPR(
                getRuleData(
                    getUgyldigBehandler()
                )
            ) shouldEqual true
        }
    }

    describe("1403 Behandler har ikke gyldig autorisasjon i HPR") {
        it("Should not trigger rule for kodeverdi") {
            val validAuthorizations: List<String> = listOf("1", "17", "4", "2", "14", "18")
            for (verdi in validAuthorizations) {
                val behandler = getBehandler(autorisasjon = Kode(true, 7704, verdi))
                HPRRuleChain.BEHANDLER_MANGLER_AUTORISASJON_I_HPR(
                    getRuleData(
                        behandler
                    )
                ) shouldEqual false
            }
        }
        it("Should trigger rule when verdi is not valid") {
            val behandler = getBehandler(autorisasjon = Kode(true, 7704, ""))
            HPRRuleChain.BEHANDLER_MANGLER_AUTORISASJON_I_HPR(
                getRuleData(
                    behandler
                )
            ) shouldEqual true
        }

        it("Should trigger rule when verdi is null") {
            val behandler = getBehandler(autorisasjon = Kode(true, 7704, null))
            HPRRuleChain.BEHANDLER_MANGLER_AUTORISASJON_I_HPR(
                getRuleData(
                    behandler
                )
            ) shouldEqual true
        }

        it("Should trigger rule when not aktiv") {
            val behandler = getBehandler(autorisasjon = Kode(false, 7704, "1"))
            HPRRuleChain.BEHANDLER_MANGLER_AUTORISASJON_I_HPR(
                getRuleData(
                    behandler
                )
            ) shouldEqual true
        }

        it("Should trigger rule when with incorrect oid") {
            val behandler = getBehandler(autorisasjon = Kode(true, 7703, "1"))
            HPRRuleChain.BEHANDLER_MANGLER_AUTORISASJON_I_HPR(
                getRuleData(
                    behandler
                )
            ) shouldEqual true
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
                HPRRuleChain.BEHANDLER_IKKE_LE_KI_TL_FT_I_HPR(
                    getRuleData(
                        behandler
                    )
                ) shouldEqual false
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
            HPRRuleChain.BEHANDLER_IKKE_LE_KI_TL_FT_I_HPR(
                getRuleData(
                    behandler
                )
            ) shouldEqual true
        }
        it("Should trigger rule when helsepersonellkategori ikke er aktiv") {
            val behandler = getBehandler(
                helsepersonellkategori = Kode(
                    aktiv = false,
                    verdi = "LE",
                    oid = 9060
                )
            )
            HPRRuleChain.BEHANDLER_IKKE_LE_KI_TL_FT_I_HPR(
                getRuleData(
                    behandler
                )
            ) shouldEqual true
        }
    }

    describe("1519 BEHANDLER_FT_KI_OVER_12_UKER") {

        it("Should not trigger rule when lege") {
            val behandler = getBehandler(
                helsepersonellkategori = Kode(
                    aktiv = true,
                    verdi = "LE",
                    oid = 9060
                )
            )
            val fomDate = LocalDate.of(2019, 1, 1)
            val sykemelding = generateSykemelding(listOf(generatePeriode(
                fom = fomDate,
                tom = fomDate.plusWeeks(12).plusDays(1)
            )))
            HPRRuleChain.BEHANDLER_FT_KI_OVER_12_UKER(RuleData(sykemelding, behandler)) shouldEqual false
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
            val sykemelding = generateSykemelding(listOf(generatePeriode(
                fom = fomDate,
                tom = fomDate.plusWeeks(12)
            )))
            HPRRuleChain.BEHANDLER_FT_KI_OVER_12_UKER(RuleData(sykemelding, behandler)) shouldEqual false
        }

        it("Should trigger rule") {
            val behandler = getBehandler(
                helsepersonellkategori = Kode(
                    aktiv = true,
                    verdi = "KI",
                    oid = 9060
                )
            )
            val fomDate = LocalDate.of(2019, 1, 1)
            val sykemelding = generateSykemelding(listOf(generatePeriode(
                fom = fomDate,
                tom = fomDate.plusWeeks(12).plusDays(1)
            )))
            HPRRuleChain.BEHANDLER_FT_KI_OVER_12_UKER(RuleData(sykemelding, behandler)) shouldEqual true
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

private fun getRuleData(behandler: Behandler): RuleData<Behandler> = RuleData(generateSykemelding(), behandler)
