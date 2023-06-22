package no.nav.syfo.rules.tilbakedatering

import no.nav.syfo.log
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.papirsykemelding.rules.common.MedJuridisk
import no.nav.syfo.papirsykemelding.rules.common.RuleExecution
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.dsl.ResultNode
import no.nav.syfo.papirsykemelding.rules.dsl.RuleNode
import no.nav.syfo.papirsykemelding.rules.dsl.TreeNode
import no.nav.syfo.papirsykemelding.rules.dsl.TreeOutput
import no.nav.syfo.papirsykemelding.rules.dsl.join
import no.nav.syfo.papirsykemelding.rules.dsl.printRulePath
import no.nav.syfo.papirsykemelding.rules.tilbakedatering.tilbakeDatertJuridiskHenvisning
import no.nav.syfo.papirsykemelding.service.RuleMetadataSykmelding

typealias TilbakedateringTreeOutput = TreeOutput<TilbakedateringRules, RuleResult>

typealias TilbakedateringTreeNode = TreeNode<TilbakedateringRules, RuleResult>

class TilbakedateringRulesExecution(
    private val rootNode: TilbakedateringTreeNode = tilbakedateringRuleTree
) : RuleExecution<TilbakedateringRules> {
    override fun runRules(sykmelding: Sykmelding, ruleMetadata: RuleMetadataSykmelding) =
        rootNode.evaluate(sykmelding, ruleMetadata).also { tilbakedateringRulePath ->
            log.info("Rules ${sykmelding.id}, ${tilbakedateringRulePath.printRulePath()}")
        } to MedJuridisk(tilbakeDatertJuridiskHenvisning())
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
