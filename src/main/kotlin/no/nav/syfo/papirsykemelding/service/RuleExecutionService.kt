package no.nav.syfo.papirsykemelding.service

import no.nav.syfo.model.Status
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.papirsykemelding.rules.common.Juridisk
import no.nav.syfo.papirsykemelding.rules.common.RuleExecution
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.dsl.TreeOutput
import no.nav.syfo.papirsykemelding.rules.gradert.GradertRulesExecution
import no.nav.syfo.papirsykemelding.rules.gradert.gradertRuleTree
import no.nav.syfo.papirsykemelding.rules.hpr.HPRRulesExecution
import no.nav.syfo.papirsykemelding.rules.hpr.hprRuleTree
import no.nav.syfo.papirsykemelding.rules.legesuspensjon.LegeSuspensjonRulesExecution
import no.nav.syfo.papirsykemelding.rules.legesuspensjon.legeSuspensjonRuleTree
import no.nav.syfo.papirsykemelding.rules.periodlogic.PeriodLogicRulesExecution
import no.nav.syfo.papirsykemelding.rules.validation.ValidationRulesExecution
import no.nav.syfo.papirsykemelding.rules.validation.validationRuleTree
import no.nav.syfo.rules.periodlogic.periodLogicRuleTree
import no.nav.syfo.rules.tilbakedatering.TilbakedateringRulesExecution
import no.nav.syfo.rules.tilbakedatering.tilbakedateringRuleTree

class RuleExecutionService() {

    private val ruleExecution = sequenceOf(
        LegeSuspensjonRulesExecution(legeSuspensjonRuleTree),
        HPRRulesExecution(hprRuleTree),
        ValidationRulesExecution(validationRuleTree),
        PeriodLogicRulesExecution(periodLogicRuleTree),
        TilbakedateringRulesExecution(tilbakedateringRuleTree),
        GradertRulesExecution(gradertRuleTree),

    )

    fun runRules(
        sykmelding: Sykmelding,
        ruleMetadataSykmelding: RuleMetadataSykmelding,
        sequence: Sequence<RuleExecution<out Enum<*>>> = ruleExecution,
    ): List<Pair<TreeOutput<out Enum<*>, RuleResult>, Juridisk>> {
        var lastStatus = Status.OK
        val results = sequence
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
