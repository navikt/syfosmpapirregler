package no.nav.syfo.papirsykemelding.rules.arbeidsuforhet

import no.nav.syfo.logger
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.papirsykemelding.rules.common.Juridisk
import no.nav.syfo.papirsykemelding.rules.common.RuleExecution
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.dsl.ResultNode
import no.nav.syfo.papirsykemelding.rules.dsl.RuleNode
import no.nav.syfo.papirsykemelding.rules.dsl.TreeNode
import no.nav.syfo.papirsykemelding.rules.dsl.TreeOutput
import no.nav.syfo.papirsykemelding.rules.dsl.join
import no.nav.syfo.papirsykemelding.rules.dsl.printRulePath
import no.nav.syfo.papirsykemelding.service.RuleMetadataSykmelding

typealias ArbeidsuforhetTreeOutput = TreeOutput<ArbeidsuforhetRules, RuleResult>

typealias ArbeidsuforhetTreeNode = Pair<TreeNode<ArbeidsuforhetRules, RuleResult>, Juridisk>

class ArbeidsuforhetRulesExecution(
    private val rootNode: ArbeidsuforhetTreeNode = arbeidsuforhetRuleTreeNew
) : RuleExecution<ArbeidsuforhetRules> {
    override fun runRules(sykmelding: Sykmelding, ruleMetadata: RuleMetadataSykmelding) =
        rootNode.first.evaluate(sykmelding, ruleMetadata.ruleMetadata).also { validationRulePath ->
            logger.info(
                "Rules for sykmeldingid ${sykmelding.id}, ${validationRulePath.printRulePath()}"
            )
        } to rootNode.second
}

private fun TreeNode<ArbeidsuforhetRules, RuleResult>.evaluate(
    sykmelding: Sykmelding,
    ruleMetadata: RuleMetadata,
): ArbeidsuforhetTreeOutput =
    when (this) {
        is ResultNode -> ArbeidsuforhetTreeOutput(treeResult = result)
        is RuleNode -> {
            val rule = getRule(rule)
            val result = rule(sykmelding, ruleMetadata)
            val childNode = if (result.ruleResult) yes else no
            result join childNode.evaluate(sykmelding, ruleMetadata)
        }
    }
