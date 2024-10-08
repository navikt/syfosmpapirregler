package no.nav.syfo.papirsykemelding.rules.common

import no.nav.syfo.model.Status
import no.nav.syfo.papirsykemelding.rules.arbeidsuforhet.arbeidsuforhetRuleTreeNew
import no.nav.syfo.papirsykemelding.rules.dsl.ResultNode
import no.nav.syfo.papirsykemelding.rules.dsl.RuleNode
import no.nav.syfo.papirsykemelding.rules.dsl.TreeNode
import no.nav.syfo.papirsykemelding.rules.hpr.hprRuleTree
import no.nav.syfo.papirsykemelding.rules.legesuspensjon.legeSuspensjonRuleTree
import no.nav.syfo.papirsykemelding.rules.patientunder13.patientAgeUnder13RuleTree
import no.nav.syfo.papirsykemelding.rules.periode.periodeRuleTree
import no.nav.syfo.papirsykemelding.rules.periodlogic.periodLogicRuleTree
import no.nav.syfo.papirsykemelding.rules.tilbakedatering.tilbakedateringRuleTree
import no.nav.syfo.papirsykemelding.rules.validation.validationRuleTree

fun main() {
    val ruleTrees =
        listOf(
            "Lege suspensjon" to legeSuspensjonRuleTree,
            "Validation" to validationRuleTree,
            "Periode validering" to periodLogicRuleTree,
            "HPR" to hprRuleTree,
            "Arbeidsuforhet" to arbeidsuforhetRuleTreeNew,
            "Pasient under 13" to patientAgeUnder13RuleTree,
            "Periode" to periodeRuleTree,
            "Tilbakedatering" to tilbakedateringRuleTree,
        )

    ruleTrees.forEachIndexed { idx, (name, ruleTree) ->
        val builder = StringBuilder()
        builder.append("## ${idx + 1}. $name\n\n")

        builder.append("---\n\n")

        when (val juridiskInfo = ruleTree.second) {
            is MedJuridisk -> {
                val henvisning = juridiskInfo.juridiskHenvisning
                builder.append("- ### Juridisk Henvisning:\n")
                henvisning.lovverk.let { builder.append("  - **Lovverk**: $it\n") }
                henvisning.paragraf.let { builder.append("  - **Paragraf**: $it\n") }
                henvisning.ledd?.let { builder.append("  - **Ledd**: $it\n") }
                henvisning.punktum?.let { builder.append("  - **Punktum**: $it\n") }
                henvisning.bokstav?.let { builder.append("  - **Bokstav**: $it\n") }
            }
            is UtenJuridisk -> {}
        }

        builder.append("\n---\n\n")

        builder.append("```mermaid\n")
        builder.append("graph TD\n")
        ruleTree.first.traverseTree(builder, "root", "root")
        builder.append("    classDef ok fill:#c3ff91,stroke:#004a00,color: black;\n")
        builder.append("    classDef invalid fill:#ff7373,stroke:#ff0000,color: black;\n")
        builder.append("    classDef manuell fill:#ffe24f,stroke:#ffd500,color: #473c00;\n")
        builder.append("```\n\n")

        println(builder.toString())
    }
}

private fun <T> TreeNode<T, RuleResult>.traverseTree(
    builder: StringBuilder,
    thisNodeKey: String,
    nodeKey: String,
) {
    when (this) {
        is ResultNode -> {
            // Is handled by parent node
            return
        }
        is RuleNode -> {
            val currentNodeKey = "${nodeKey}_$rule"
            if (yes is ResultNode) {
                val childResult = (yes as ResultNode<T, RuleResult>).result.status
                val childKey = "${currentNodeKey}_$childResult"
                builder.append(
                    "    $thisNodeKey($rule) -->|Yes| $childKey($childResult)${getStyle(childResult)}\n"
                )
            } else {
                val childRule = (yes as RuleNode<T, RuleResult>).rule
                val childKey = "${currentNodeKey}_$childRule"
                builder.append("    $thisNodeKey($rule) -->|Yes| $childKey($childRule)\n")
                yes.traverseTree(builder, childKey, currentNodeKey)
            }
            if (no is ResultNode) {
                val childResult = (no as ResultNode<T, RuleResult>).result.status
                val childKey = "${currentNodeKey}_$childResult"
                builder.append(
                    "    $thisNodeKey($rule) -->|No| $childKey($childResult)${getStyle(childResult)}\n"
                )
            } else {
                val childRule = (no as RuleNode<T, RuleResult>).rule
                val childKey = "${currentNodeKey}_$childRule"
                builder.append("    $thisNodeKey($rule) -->|No| $childKey($childRule)\n")
                no.traverseTree(builder, "${currentNodeKey}_$childRule", currentNodeKey)
            }
        }
    }
}

fun getStyle(childResult: Status): String =
    when (childResult) {
        Status.OK -> ":::ok"
        Status.INVALID -> ":::invalid"
        Status.MANUAL_PROCESSING -> ":::manuell"
    }
