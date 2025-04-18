package no.nav.syfo.papirsykemelding.rules.patientunder13

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

typealias PatientAgeUnder13TreeOutput = TreeOutput<PatientAgeUnder13Rules, RuleResult>

typealias PatientAgeUnder12TreeNode = TreeNode<PatientAgeUnder13Rules, RuleResult>

class PatientAgeUnder13RulesExecution(
    val rootNode: PatientAgeUnder12TreeNode = patientAgeUnder13RuleTree
) : RuleExecution<PatientAgeUnder13Rules> {
    override fun runRules(sykmelding: Sykmelding, ruleMetadata: RuleMetadataSykmelding) =
        rootNode.evaluate(sykmelding, ruleMetadata).also { patientAgeUnder13 ->
            logger.info(
                "Rules for sykmeldingid ${sykmelding.id}, ${patientAgeUnder13.printRulePath()}"
            )
        }
}

private fun TreeNode<PatientAgeUnder13Rules, RuleResult>.evaluate(
    sykmelding: Sykmelding,
    ruleMetadata: RuleMetadataSykmelding,
): PatientAgeUnder13TreeOutput =
    when (this) {
        is ResultNode -> PatientAgeUnder13TreeOutput(treeResult = result)
        is RuleNode -> {
            val rule = getRule(rule)
            val result = rule(sykmelding, ruleMetadata)
            val childNode = if (result.ruleResult) yes else no
            result join childNode.evaluate(sykmelding, ruleMetadata)
        }
    }
