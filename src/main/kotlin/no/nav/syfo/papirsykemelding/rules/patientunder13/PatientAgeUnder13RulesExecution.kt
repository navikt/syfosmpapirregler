package no.nav.syfo.rules.patientunder13

import no.nav.syfo.logger
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.model.juridisk.JuridiskHenvisning
import no.nav.syfo.model.juridisk.Lovverk
import no.nav.syfo.papirsykemelding.rules.common.MedJuridisk
import no.nav.syfo.papirsykemelding.rules.common.RuleExecution
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.dsl.ResultNode
import no.nav.syfo.papirsykemelding.rules.dsl.RuleNode
import no.nav.syfo.papirsykemelding.rules.dsl.TreeNode
import no.nav.syfo.papirsykemelding.rules.dsl.TreeOutput
import no.nav.syfo.papirsykemelding.rules.dsl.printRulePath
import no.nav.syfo.papirsykemelding.service.RuleMetadataSykmelding


typealias PatientAgeUnder13TreeOutput = TreeOutput<PatientAgeUnder13Rules, RuleResult>

class PatientAgeUnder13RulesExecution(
    val rootNode: TreeNode<PatientAgeUnder13Rules, RuleResult> = patientAgeUnder13RuleTree
) : RuleExecution<PatientAgeUnder13Rules> {
    override fun runRules(sykmelding: Sykmelding, ruleMetadata: RuleMetadataSykmelding) =
        rootNode.evaluate(sykmelding, ruleMetadata).also { patientAgeUnder13 ->
            logger.info("Rules ${sykmelding.id}, ${patientAgeUnder13.printRulePath()}")
        } to
            MedJuridisk(
                JuridiskHenvisning(
                    lovverk = Lovverk.FOLKETRYGDLOVEN,
                    paragraf = "8-3",
                    ledd = 1,
                    punktum = null,
                    bokstav = null,
                ),
            )
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