package no.nav.syfo.papirsykemelding.rules.common

import no.nav.syfo.model.Sykmelding
import no.nav.syfo.papirsykemelding.rules.dsl.TreeOutput
import no.nav.syfo.papirsykemelding.service.RuleMetadataSykmelding

interface RuleExecution<T> {
    fun runRules(sykmelding: Sykmelding, ruleMetadata: RuleMetadataSykmelding): Pair<TreeOutput<T, RuleResult>, Juridisk>
}
