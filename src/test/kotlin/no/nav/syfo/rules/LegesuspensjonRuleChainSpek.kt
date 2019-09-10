package no.nav.syfo.rules

import no.nav.syfo.generatePerioder
import no.nav.syfo.generateSykemelding
import no.nav.syfo.model.Sykmelding
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object LegesuspensjonRuleChainSpek : Spek({

    fun ruleData(
        sykmelding: Sykmelding,
        suspended: Boolean
    ): RuleData<Boolean> = RuleData(sykmelding, suspended)

    describe("Testing validation rules and checking the rule outcomes") {
        it("Should check rule BEHANDLER_SUSPENDERT, should trigger rule") {
            val sykemelding = generateSykemelding(generatePerioder())
            val suspended = true

            LegesuspensjonRuleChain.BEHANDLER_SUSPENDERT(ruleData(sykemelding, suspended)) shouldEqual true
        }

        it("Should check rule BEHANDLER_SUSPENDERT, should NOT trigger rule") {
            val sykemelding = generateSykemelding(generatePerioder())
            val suspended = false

            LegesuspensjonRuleChain.BEHANDLER_SUSPENDERT(ruleData(sykemelding, suspended)) shouldEqual false
        }
    }
})
