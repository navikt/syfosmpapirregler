package no.nav.syfo.papirsykemelding.rules.validation

import no.nav.syfo.logger
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.papirsykemelding.rules.common.RuleExecution
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.dsl.ResultNode
import no.nav.syfo.papirsykemelding.rules.dsl.RuleNode
import no.nav.syfo.papirsykemelding.rules.dsl.TreeNode
import no.nav.syfo.papirsykemelding.rules.dsl.TreeOutput
import no.nav.syfo.papirsykemelding.rules.dsl.join
import no.nav.syfo.papirsykemelding.rules.dsl.printRulePath
import no.nav.syfo.papirsykemelding.service.RuleMetadataSykmelding

typealias ValidationTreeOutput = TreeOutput<ValidationRules, RuleResult>

typealias ValidationTreeNode = TreeNode<ValidationRules, RuleResult>

class ValidationRulesExecution(private val rootNode: ValidationTreeNode = validationRuleTree) :
    RuleExecution<ValidationRules> {
    override fun runRules(sykmelding: Sykmelding, ruleMetadata: RuleMetadataSykmelding) =
        rootNode.evaluate(sykmelding, ruleMetadata.ruleMetadata).also { validationRulePath ->
            logger.info(
                "Rules for sykmeldingid ${sykmelding.id}, ${validationRulePath.printRulePath()}"
            )
        }
}

private fun TreeNode<ValidationRules, RuleResult>.evaluate(
    sykmelding: Sykmelding,
    ruleMetadata: RuleMetadata,
): ValidationTreeOutput =
    when (this) {
        is ResultNode -> ValidationTreeOutput(treeResult = result)
        is RuleNode -> {
            val rule = getRule(rule)
            val result = rule(sykmelding, ruleMetadata)
            val childNode = if (result.ruleResult) yes else no
            result join childNode.evaluate(sykmelding, ruleMetadata)
        }
    }
