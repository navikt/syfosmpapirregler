package no.nav.syfo.papirsykemelding.rules.tilbakedatering

import no.nav.syfo.logger
import no.nav.syfo.model.Sykmelding
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

typealias TilbakedateringTreeOutput = TreeOutput<TilbakedateringRules, RuleResult>

typealias TilbakedateringTreeNode = Pair<TreeNode<TilbakedateringRules, RuleResult>, Juridisk>

class TilbakedateringRulesExecution(
    private val rootNode: TilbakedateringTreeNode = tilbakedateringRuleTree
) : RuleExecution<TilbakedateringRules> {
    override fun runRules(sykmelding: Sykmelding, ruleMetadata: RuleMetadataSykmelding) =
        rootNode.first.evaluate(sykmelding, ruleMetadata).also { tilbakedateringRulePath ->
            logger.info(
                "Rules for sykmeldingid ${sykmelding.id}, ${tilbakedateringRulePath.printRulePath()}"
            )
        } to rootNode.second
}

private fun TreeNode<TilbakedateringRules, RuleResult>.evaluate(
    sykmelding: Sykmelding,
    metadata: RuleMetadataSykmelding,
): TilbakedateringTreeOutput =
    when (this) {
        is ResultNode -> {
            TilbakedateringTreeOutput(treeResult = result)
        }
        is RuleNode -> {
            val rule = getRule(rule)
            val result = rule(sykmelding, metadata)
            val childNode = if (result.ruleResult) yes else no
            result join childNode.evaluate(sykmelding, metadata)
        }
    }
