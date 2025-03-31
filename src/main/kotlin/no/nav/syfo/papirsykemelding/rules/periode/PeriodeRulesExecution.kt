package no.nav.syfo.papirsykemelding.rules.periode

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

typealias PeriodeRuleTreeOutput = TreeOutput<PeriodeRules, RuleResult>

typealias PeriodeRuleNode = TreeNode<PeriodeRules, RuleResult>

class PeriodeRulesExecution(private val rootNode: PeriodeRuleNode = periodeRuleTree) :
    RuleExecution<PeriodeRules> {
    override fun runRules(sykmelding: Sykmelding, ruleMetadata: RuleMetadataSykmelding) =
        rootNode.evaluate(sykmelding, ruleMetadata).also {
            logger.info("Rules for sykmeldingid ${sykmelding.id}, ${it.printRulePath()}")
        }

    private fun TreeNode<PeriodeRules, RuleResult>.evaluate(
        sykmelding: Sykmelding,
        ruleMetadata: RuleMetadataSykmelding,
    ): PeriodeRuleTreeOutput =
        when (this) {
            is ResultNode -> PeriodeRuleTreeOutput(treeResult = result)
            is RuleNode -> {
                val rule = getRule(rule)
                val result = rule(sykmelding, ruleMetadata.ruleMetadata)
                val childNode = if (result.ruleResult) yes else no
                result join childNode.evaluate(sykmelding, ruleMetadata)
            }
        }
}
