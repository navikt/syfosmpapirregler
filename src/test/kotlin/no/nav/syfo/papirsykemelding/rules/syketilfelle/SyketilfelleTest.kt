package no.nav.syfo.papirsykemelding.rules.syketilfelle

import io.kotest.core.spec.style.FunSpec
import no.nav.syfo.client.norskhelsenett.Behandler
import no.nav.syfo.client.norskhelsenett.Godkjenning
import no.nav.syfo.client.norskhelsenett.Kode
import no.nav.syfo.generateSykemelding
import no.nav.syfo.model.AktivitetIkkeMulig
import no.nav.syfo.model.Periode
import no.nav.syfo.model.Status
import no.nav.syfo.papirsykemelding.service.BehandlerOgStartdato
import no.nav.syfo.papirsykemelding.service.RuleMetadataSykmelding
import no.nav.syfo.toRuleMetadata
import org.amshove.kluent.shouldBeEqualTo
import java.time.LocalDate

class SyketilfelleTest : FunSpec({
    val ruleTree = SyketilfelleRulesExecution()

    context("Testing hpr rules and checking the rule outcomes") {
        test("har aktiv autorisasjon, Status OK") {
            Periode(
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                AktivitetIkkeMulig(null, null),
                null,
                null,
                null,
                false
            )
            val sykmelding = generateSykemelding(
                perioder = listOf(
                    Periode(
                        LocalDate.of(2020, 1, 1),
                        LocalDate.of(2020, 1, 2),
                        AktivitetIkkeMulig(null, null),
                        null,
                        null,
                        null,
                        false
                    )
                ),
                tidspunkt = LocalDate.of(2020, 1, 3).atStartOfDay()
            )
            val behandler = Behandler(
                listOf(
                    Godkjenning(
                        autorisasjon = Kode(
                            aktiv = true,
                            oid = 7704,
                            verdi = "1"
                        ),
                        helsepersonellkategori = Kode(
                            aktiv = true,
                            oid = 0,
                            verdi = "LE"
                        )
                    )
                )
            )

            val ruleMetadata = sykmelding.toRuleMetadata()

            val ruleMetadataSykmelding = RuleMetadataSykmelding(
                ruleMetadata = ruleMetadata,
                erNyttSyketilfelle = false,
                doctorSuspensjon = false,
                behandlerOgStartdato = BehandlerOgStartdato(behandler, null)
            )

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding)

            status.first.treeResult.status shouldBeEqualTo Status.OK
            status.first.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo listOf(
                SyketilfelleRules.FOR_MANGE_BEHANDLINGSDAGER_PER_UKE  to false,
                SyketilfelleRules.AVVENTENDE_SYKMELDING_KOMBINERT to false
            )

            mapOf(
                "behandlerGodkjenninger" to behandler.godkjenninger,
                "behandlerGodkjenninger" to behandler.godkjenninger
            ) shouldBeEqualTo status.first.ruleInputs

            status.first.treeResult.ruleHit shouldBeEqualTo null
        }
    }

})
