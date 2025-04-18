package no.nav.syfo.papirsykemelding.rules.legesuspensjon

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

typealias LegeSuspensjonTreeOutput = TreeOutput<LegeSuspensjonRules, RuleResult>

typealias LegeSuspensjonTreeNode = TreeNode<LegeSuspensjonRules, RuleResult>

class LegeSuspensjonRulesExecution(val rootNode: LegeSuspensjonTreeNode = legeSuspensjonRuleTree) :
    RuleExecution<LegeSuspensjonRules> {
    override fun runRules(
        sykmelding: Sykmelding,
        ruleMetadata: RuleMetadataSykmelding
    ): LegeSuspensjonTreeOutput =
        rootNode.evaluate(sykmelding.id, ruleMetadata.doctorSuspensjon).also {
            legeSuspensjonRulePath ->
            logger.info(
                "Rules for sykmeldingid ${sykmelding.id}, ${legeSuspensjonRulePath.printRulePath()}"
            )
        }
}

private fun TreeNode<LegeSuspensjonRules, RuleResult>.evaluate(
    sykmeldingId: String,
    behandlerSuspendert: Boolean,
): LegeSuspensjonTreeOutput =
    when (this) {
        is ResultNode -> LegeSuspensjonTreeOutput(treeResult = result)
        is RuleNode -> {
            val rule = getRule(rule)
            val result = rule(behandlerSuspendert)
            val childNode = if (result.ruleResult) yes else no
            result join childNode.evaluate(sykmeldingId, behandlerSuspendert)
        }
    }
