package no.nav.syfo.papirsykemelding.service

import no.nav.syfo.model.Status
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.papirsykemelding.rules.arbeidsuforhet.ArbeidsuforhetRulesExecution
import no.nav.syfo.papirsykemelding.rules.arbeidsuforhet.arbeidsuforhetRuleTreeNew
import no.nav.syfo.papirsykemelding.rules.common.Juridisk
import no.nav.syfo.papirsykemelding.rules.common.RuleExecution
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.dsl.TreeOutput
import no.nav.syfo.papirsykemelding.rules.hpr.HPRRulesExecution
import no.nav.syfo.papirsykemelding.rules.hpr.hprRuleTree
import no.nav.syfo.papirsykemelding.rules.legesuspensjon.LegeSuspensjonRulesExecution
import no.nav.syfo.papirsykemelding.rules.legesuspensjon.legeSuspensjonRuleTree
import no.nav.syfo.papirsykemelding.rules.patientunder13.PatientAgeUnder13RulesExecution
import no.nav.syfo.papirsykemelding.rules.patientunder13.patientAgeUnder13RuleTree
import no.nav.syfo.papirsykemelding.rules.periode.PeriodeRulesExecution
import no.nav.syfo.papirsykemelding.rules.periode.periodeRuleTree
import no.nav.syfo.papirsykemelding.rules.periodlogic.PeriodLogicRulesExecution
import no.nav.syfo.papirsykemelding.rules.periodlogic.periodLogicRuleTree
import no.nav.syfo.papirsykemelding.rules.tilbakedatering.TilbakedateringRulesExecution
import no.nav.syfo.papirsykemelding.rules.tilbakedatering.tilbakedateringRuleTree
import no.nav.syfo.papirsykemelding.rules.validation.ValidationRulesExecution
import no.nav.syfo.papirsykemelding.rules.validation.validationRuleTree

class RuleExecutionService {

    private val ruleExecution =
        sequenceOf(
            LegeSuspensjonRulesExecution(legeSuspensjonRuleTree),
            ValidationRulesExecution(validationRuleTree),
            PeriodLogicRulesExecution(periodLogicRuleTree),
            HPRRulesExecution(hprRuleTree),
            ArbeidsuforhetRulesExecution(arbeidsuforhetRuleTreeNew),
            PatientAgeUnder13RulesExecution(patientAgeUnder13RuleTree),
            PeriodeRulesExecution(periodeRuleTree),
            TilbakedateringRulesExecution(tilbakedateringRuleTree),
        )

    fun runRules(
        sykmelding: Sykmelding,
        ruleMetadataSykmelding: RuleMetadataSykmelding,
        sequence: Sequence<RuleExecution<out Enum<*>>> = ruleExecution,
    ): List<Pair<TreeOutput<out Enum<*>, RuleResult>, Juridisk>> {
        var lastStatus = Status.OK
        val results =
            sequence
                .map { it.runRules(sykmelding, ruleMetadataSykmelding) }
                .takeWhile {
                    if (lastStatus == Status.OK) {
                        lastStatus = it.first.treeResult.status
                        true
                    } else {
                        false
                    }
                }
        return results.toList()
    }
}
