package no.nav.syfo.papirsykemelding.rules.syketilfelle

import io.kotest.core.spec.style.FunSpec
import no.nav.syfo.generateKontaktMedPasient
import no.nav.syfo.generatePeriode
import no.nav.syfo.generateSykemelding
import no.nav.syfo.model.KontaktMedPasient
import no.nav.syfo.model.Status
import no.nav.syfo.ruleMetadataSykmelding
import no.nav.syfo.toRuleMetadata
import org.amshove.kluent.shouldBeEqualTo
import java.time.LocalDate

class SyketilfelleTest : FunSpec({
    val ruleTree = SyketilfelleRulesExecution()

    context("Testing syketilfelle rules and checking the rule outcomes") {
        test("None of the rules hits, Status OK") {
            val sykmelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now(),
                        tom = LocalDate.now().plusDays(7)
                    )
                ),
                tidspunkt = LocalDate.now().atStartOfDay()
            )

            val ruleMetadata = sykmelding.toRuleMetadata()

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata)).first

            status.treeResult.status shouldBeEqualTo Status.OK
            status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo listOf(
                SyketilfelleRules.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING to false,
                SyketilfelleRules.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING_MED_BEGRUNNELSE to false,
                SyketilfelleRules.TILBAKEDATERT_INNTIL_8_DAGER_UTEN_KONTAKTDATO_OG_BEGRUNNELSE to false,
                SyketilfelleRules.TILBAKEDATERT_FORLENGELSE_OVER_1_MND to false,
                SyketilfelleRules.TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE to false
            )

            mapOf(
                "erNyttSyketilfelle" to false,
                "behandletTidspunkt" to ruleMetadata.behandletTidspunkt
            ) shouldBeEqualTo status.ruleInputs

            status.treeResult.ruleHit shouldBeEqualTo null
        }

        test("Tilbakedatert mer enn 8 dager første sykmelding, Status MANUAL_PROCESSING") {
            val sykmelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now().minusDays(20),
                        tom = LocalDate.now()
                    )
                ),
                tidspunkt = LocalDate.now().atStartOfDay()
            )

            val ruleMetadata = sykmelding.toRuleMetadata()

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata, true)).first

            status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
            status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo listOf(
                SyketilfelleRules.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING to true
            )

            mapOf(
                "erNyttSyketilfelle" to true,
                "behandletTidspunkt" to ruleMetadata.behandletTidspunkt
            ) shouldBeEqualTo status.ruleInputs

            status.treeResult.ruleHit shouldBeEqualTo SyketilfelleRuleHit.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING.ruleHit
        }

        test("Tilbakedatert mer enn 8 dager første sykmelding med begrunnelse, Status MANUAL_PROCESSING") {
            val sykmelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now().minusDays(20),
                        tom = LocalDate.now()
                    )
                ),
                kontaktMedPasient = generateKontaktMedPasient(
                    begrunnelseIkkeKontakt = "Noe tull skjedde, med sykmeldingen"
                ),
                tidspunkt = LocalDate.now().atStartOfDay()
            )

            val ruleMetadata = sykmelding.toRuleMetadata()

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata, true)).first

            status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
            status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo listOf(
                SyketilfelleRules.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING to false,
                SyketilfelleRules.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING_MED_BEGRUNNELSE to true
            )

            mapOf(
                "erNyttSyketilfelle" to true,
                "behandletTidspunkt" to ruleMetadata.behandletTidspunkt
            ) shouldBeEqualTo status.ruleInputs

            status.treeResult.ruleHit shouldBeEqualTo SyketilfelleRuleHit.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING_MED_BEGRUNNELSE.ruleHit
        }

        test("Tilbakedatert intil 8 dager uten kontaktdato og begrunnelse, Status MANUAL_PROCESSING") {
            val sykmelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now().minusDays(7),
                        tom = LocalDate.now()
                    )
                ),
                kontaktMedPasient = KontaktMedPasient(null, null)
            )

            val ruleMetadata = sykmelding.toRuleMetadata()

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata, true)).first

            status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
            status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo listOf(
                SyketilfelleRules.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING to false,
                SyketilfelleRules.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING_MED_BEGRUNNELSE to false,
                SyketilfelleRules.TILBAKEDATERT_INNTIL_8_DAGER_UTEN_KONTAKTDATO_OG_BEGRUNNELSE to true
            )

            mapOf(
                "erNyttSyketilfelle" to true,
                "behandletTidspunkt" to ruleMetadata.behandletTidspunkt
            ) shouldBeEqualTo status.ruleInputs

            status.treeResult.ruleHit shouldBeEqualTo SyketilfelleRuleHit.TILBAKEDATERT_INNTIL_8_DAGER_UTEN_KONTAKTDATO_OG_BEGRUNNELSE.ruleHit
        }

        test("Tilbakedatert forlengelse over 1 mnd, Status MANUAL_PROCESSING") {
            val sykmelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now().minusDays(32),
                        tom = LocalDate.now()
                    )
                ),
                kontaktMedPasient = KontaktMedPasient(null, null),
                tidspunkt = LocalDate.now().atStartOfDay()
            )

            val ruleMetadata = sykmelding.toRuleMetadata()

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata, false)).first

            status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
            status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo listOf(
                SyketilfelleRules.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING to false,
                SyketilfelleRules.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING_MED_BEGRUNNELSE to false,
                SyketilfelleRules.TILBAKEDATERT_INNTIL_8_DAGER_UTEN_KONTAKTDATO_OG_BEGRUNNELSE to false,
                SyketilfelleRules.TILBAKEDATERT_FORLENGELSE_OVER_1_MND to true
            )

            mapOf(
                "erNyttSyketilfelle" to false,
                "behandletTidspunkt" to ruleMetadata.behandletTidspunkt
            ) shouldBeEqualTo status.ruleInputs

            status.treeResult.ruleHit shouldBeEqualTo SyketilfelleRuleHit.TILBAKEDATERT_FORLENGELSE_OVER_1_MND.ruleHit
        }

        test("Tilbakedatert med begrunnesle forlengelse, Status MANUAL_PROCESSING") {
            val sykmelding = generateSykemelding(
                perioder = listOf(
                    generatePeriode(
                        fom = LocalDate.now(),
                        tom = LocalDate.now()
                    )
                ),
                kontaktMedPasient = generateKontaktMedPasient(
                    begrunnelseIkkeKontakt = "Noe tull skjedde, med sykmeldingen"
                ),
                tidspunkt = LocalDate.now().plusDays(31).atStartOfDay()
            )

            val ruleMetadata = sykmelding.toRuleMetadata(
                LocalDate.now().plusDays(30).atStartOfDay(),
                LocalDate.now().atStartOfDay()
            )

            val status = ruleTree.runRules(sykmelding, ruleMetadataSykmelding(ruleMetadata, false)).first

            status.treeResult.status shouldBeEqualTo Status.MANUAL_PROCESSING
            status.rulePath.map { it.rule to it.ruleResult } shouldBeEqualTo listOf(
                SyketilfelleRules.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING to false,
                SyketilfelleRules.TILBAKEDATERT_MER_ENN_8_DAGER_FORSTE_SYKMELDING_MED_BEGRUNNELSE to false,
                SyketilfelleRules.TILBAKEDATERT_INNTIL_8_DAGER_UTEN_KONTAKTDATO_OG_BEGRUNNELSE to false,
                SyketilfelleRules.TILBAKEDATERT_FORLENGELSE_OVER_1_MND to false,
                SyketilfelleRules.TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE to true
            )

            mapOf(
                "erNyttSyketilfelle" to false,
                "behandletTidspunkt" to ruleMetadata.behandletTidspunkt
            ) shouldBeEqualTo status.ruleInputs

            status.treeResult.ruleHit shouldBeEqualTo SyketilfelleRuleHit.TILBAKEDATERT_MED_BEGRUNNELSE_FORLENGELSE.ruleHit
        }
    }
})
