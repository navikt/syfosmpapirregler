package no.nav.syfo.papirsykemelding.rules.hpr

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
import no.nav.syfo.papirsykemelding.service.BehandlerOgStartdato
import no.nav.syfo.papirsykemelding.service.RuleMetadataSykmelding

typealias HPRTreeOutput = TreeOutput<HPRRules, RuleResult>

typealias HPRTreeNode = TreeNode<HPRRules, RuleResult>

class HPRRulesExecution(private val rootNode: HPRTreeNode = hprRuleTree) : RuleExecution<HPRRules> {
    override fun runRules(sykmelding: Sykmelding, ruleMetadata: RuleMetadataSykmelding) =
        rootNode.evaluate(sykmelding, ruleMetadata.behandlerOgStartdato).also { hprRulePath ->
            logger.info("Rules for sykmeldingid ${sykmelding.id}, ${hprRulePath.printRulePath()}")
        }
}

private fun TreeNode<HPRRules, RuleResult>.evaluate(
    sykmelding: Sykmelding,
    behandlerOgStartdato: BehandlerOgStartdato,
): HPRTreeOutput =
    when (this) {
        is ResultNode -> HPRTreeOutput(treeResult = result)
        is RuleNode -> {
            val rule = getRule(rule)
            val result = rule(sykmelding, behandlerOgStartdato)
            val childNode = if (result.ruleResult) yes else no
            result join childNode.evaluate(sykmelding, behandlerOgStartdato)
        }
    }
