package no.nav.syfo.papirsykemelding.rules

import no.nav.syfo.model.Status
import no.nav.syfo.rules.Description
import no.nav.syfo.rules.Rule
import no.nav.syfo.rules.RuleData

enum class PostDiskresjonskodeRuleChain(
    override val ruleId: Int?,
    override val status: Status,
    override val messageForUser: String,
    override val messageForSender: String,
    override val predicate: (RuleData<String>) -> Boolean
) : Rule<RuleData<String>> {
    @Description("Pasient er registrert med sperrekode 6, sperret adresse, strengt fortrolig. Kode 6 overstyrer oppfølgingsregler. Melding går ikke til Arena.")
    PASIENTEN_HAR_KODE_6(
            1305,
            Status.MANUAL_PROCESSING,
            "Pasient er registrert med sperrekode 6, sperret adresse, strengt fortrolig",
            "Pasient er registrert med sperrekode 6, sperret adresse, strengt fortrolig", { (_, diskresjonskode) ->
        diskresjonskode == "6"
    })
}
