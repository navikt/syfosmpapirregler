package no.nav.syfo.papirsykemelding.rules

import no.nav.syfo.generateSykemelding
import no.nav.syfo.model.Sykmelding
import no.nav.syfo.rules.RuleData
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object PostDiskresjonskodeRuleChainSpek : Spek({

    fun ruleData(
        healthInformation: Sykmelding,
        diskresjonskode: String
    ): RuleData<String> = RuleData(healthInformation, diskresjonskode)

    describe("Testing validation rules and checking the rule outcomes") {

        it("Should check rule PASIENTEN_HAR_KODE_6, should trigger rule") {
            val healthInformation = generateSykemelding()
            val diskresjonskode = "6"

            PostDiskresjonskodeRuleChain.PASIENTEN_HAR_KODE_6(ruleData(healthInformation, diskresjonskode)) shouldEqual true
        }

        it("Should check rule PASIENTEN_HAR_KODE_6, should NOT trigger rule") {
            val healthInformation = generateSykemelding()
            val diskresjonskode = "7"

            PostDiskresjonskodeRuleChain.PASIENTEN_HAR_KODE_6(ruleData(healthInformation, diskresjonskode)) shouldEqual false
        }
    }
})