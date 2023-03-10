package no.nav.syfo.papirsykemelding.rules.syketilfelle

import no.nav.syfo.log
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.papirsykemelding.rules.common.RuleExecution
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.common.UtenJuridisk
import no.nav.syfo.papirsykemelding.rules.dsl.ResultNode
import no.nav.syfo.papirsykemelding.rules.dsl.RuleNode
import no.nav.syfo.papirsykemelding.rules.dsl.TreeNode
import no.nav.syfo.papirsykemelding.rules.dsl.TreeOutput
import no.nav.syfo.papirsykemelding.rules.dsl.join
import no.nav.syfo.papirsykemelding.rules.dsl.printRulePath
import no.nav.syfo.papirsykemelding.service.RuleMetadataSykmelding

typealias SyketilfelleTreeOutput = TreeOutput<SyketilfelleRules, RuleResult>
typealias SyketilfelleTreeNode = TreeNode<SyketilfelleRules, RuleResult>

class SyketilfelleRulesExecution(private val rootNode: TreeNode<SyketilfelleRules, RuleResult> = syketilfelleRuleTree) :
    RuleExecution<SyketilfelleRules> {
    override fun runRules(sykmelding: Sykmelding, ruleMetadata: RuleMetadataSykmelding) =
        rootNode
            .evaluate(sykmelding, ruleMetadata)
            .also { syketilfelleRulePath ->
                log.info("Rules ${sykmelding.id}, ${syketilfelleRulePath.printRulePath()}")
            } to UtenJuridisk
}

private fun TreeNode<SyketilfelleRules, RuleResult>.evaluate(
    sykmelding: Sykmelding,
    ruleMetadata: RuleMetadataSykmelding
): SyketilfelleTreeOutput =
    when (this) {
        is ResultNode -> SyketilfelleTreeOutput(treeResult = result)
        is RuleNode -> {
            val rule = getRule(rule)
            val result = rule(sykmelding, ruleMetadata.ruleMetadata)
            val childNode = if (result.ruleResult) yes else no
            result join childNode.evaluate(sykmelding, ruleMetadata)
        }
    }
