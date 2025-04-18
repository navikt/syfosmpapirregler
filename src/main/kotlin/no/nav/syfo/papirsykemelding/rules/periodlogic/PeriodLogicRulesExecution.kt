package no.nav.syfo.papirsykemelding.rules.periodlogic

import no.nav.syfo.logger
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.papirsykemelding.rules.common.RuleExecution
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.dsl.ResultNode
import no.nav.syfo.papirsykemelding.rules.dsl.RuleNode
import no.nav.syfo.papirsykemelding.rules.dsl.TreeNode
import no.nav.syfo.papirsykemelding.rules.dsl.TreeOutput
import no.nav.syfo.papirsykemelding.rules.dsl.join
import no.nav.syfo.papirsykemelding.rules.dsl.printRulePath
import no.nav.syfo.papirsykemelding.service.RuleMetadataSykmelding

typealias PeriodLogicTreeOutput = TreeOutput<PeriodLogicRules, RuleResult>

typealias PeriodLogicRuleTree = TreeNode<PeriodLogicRules, RuleResult>

class PeriodLogicRulesExecution(private val rootNode: PeriodLogicRuleTree = periodLogicRuleTree) :
    RuleExecution<PeriodLogicRules> {
    override fun runRules(sykmelding: Sykmelding, ruleMetadata: RuleMetadataSykmelding) =
        rootNode.evaluate(sykmelding, ruleMetadata).also { periodLogicRulePath ->
            logger.info(
                "Rules for sykmeldingid ${sykmelding.id}, ${periodLogicRulePath.printRulePath()}"
            )
        }
}

private fun TreeNode<PeriodLogicRules, RuleResult>.evaluate(
    sykmelding: Sykmelding,
    ruleMetadata: RuleMetadataSykmelding,
): PeriodLogicTreeOutput =
    when (this) {
        is ResultNode -> PeriodLogicTreeOutput(treeResult = result)
        is RuleNode -> {
            val rule = getRule(rule)
            val result = rule(sykmelding, ruleMetadata.ruleMetadata)
            val childNode = if (result.ruleResult) yes else no
            result join childNode.evaluate(sykmelding, ruleMetadata)
        }
    }
