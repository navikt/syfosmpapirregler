package no.nav.syfo.papirsykemelding.rules.periodlogic

import no.nav.syfo.model.Sykmelding
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.papirsykemelding.model.daysBetween
import no.nav.syfo.papirsykemelding.model.range
import no.nav.syfo.papirsykemelding.model.startedWeeksBetween
import no.nav.syfo.papirsykemelding.model.workdaysBetween
import no.nav.syfo.papirsykemelding.rules.dsl.RuleResult

typealias Rule<T> = (sykmelding: Sykmelding, ruleMetadata: RuleMetadata) -> RuleResult<T>

typealias PeriodLogicRule = Rule<PeriodLogicRules>

val periodeMangler: PeriodLogicRule = { sykmelding, _ ->
    val perioder = sykmelding.perioder
    val periodeMangler = perioder.isEmpty()

    RuleResult(
        ruleInputs = mapOf("perioder" to perioder),
        rule = PeriodLogicRules.PERIODER_MANGLER,
        ruleResult = periodeMangler,
    )
}

val fraDatoEtterTilDato: PeriodLogicRule = { sykmelding, _ ->
    val perioder = sykmelding.perioder

    val fraDatoEtterTilDato = perioder.any { it.fom.isAfter(it.tom) }

    RuleResult(
        ruleInputs = mapOf("perioder" to perioder),
        rule = PeriodLogicRules.FRADATO_ETTER_TILDATO,
        ruleResult = fraDatoEtterTilDato,
    )
}

val overlappendePerioder: PeriodLogicRule = { sykmelding, _ ->
    val perioder = sykmelding.perioder

    val overlappendePerioder =
        perioder.any { periodA ->
            perioder
                .filter { periodB -> periodB != periodA }
                .any { periodB -> periodA.fom in periodB.range() || periodA.tom in periodB.range() }
        }

    RuleResult(
        ruleInputs = mapOf("perioder" to perioder),
        rule = PeriodLogicRules.OVERLAPPENDE_PERIODER,
        ruleResult = overlappendePerioder,
    )
}

val oppholdMellomPerioder: PeriodLogicRule = { sykmelding, _ ->
    val periodeRanges = sykmelding.perioder.sortedBy { it.fom }.map { it.fom to it.tom }

    var oppholdMellomPerioder = false
    for (i in 1 until periodeRanges.size) {
        oppholdMellomPerioder =
            workdaysBetween(periodeRanges[i - 1].second, periodeRanges[i].first) > 0
        if (oppholdMellomPerioder == true) {
            break
        }
    }

    RuleResult(
        ruleInputs = mapOf("periodeRanges" to periodeRanges),
        rule = PeriodLogicRules.OPPHOLD_MELLOM_PERIODER,
        ruleResult = oppholdMellomPerioder,
    )
}

val ikkeDefinertPeriode: PeriodLogicRule = { sykmelding, _ ->
    val perioder = sykmelding.perioder

    val ikkeDefinertPeriode =
        perioder.any {
            it.aktivitetIkkeMulig == null &&
                it.gradert == null &&
                it.avventendeInnspillTilArbeidsgiver.isNullOrEmpty() &&
                !it.reisetilskudd &&
                (it.behandlingsdager == null || it.behandlingsdager == 0)
        }

    RuleResult(
        ruleInputs = mapOf("perioder" to perioder),
        rule = PeriodLogicRules.IKKE_DEFINERT_PERIODE,
        ruleResult = ikkeDefinertPeriode,
    )
}

val avventendeKombinert: PeriodLogicRule = { sykmelding, _ ->
    val perioder = sykmelding.perioder

    val avventendeKombinert =
        perioder.count { it.avventendeInnspillTilArbeidsgiver != null } != 0 && perioder.size > 1

    RuleResult(
        ruleInputs = mapOf("avventendeKombinert" to avventendeKombinert),
        rule = PeriodLogicRules.AVVENTENDE_SYKMELDING_KOMBINERT,
        ruleResult = avventendeKombinert,
    )
}

val manglendeInnspillArbeidsgiver: PeriodLogicRule = { sykmelding, _ ->
    val perioder = sykmelding.perioder

    val manglendeInnspillArbeidsgiver =
        perioder.any {
            it.avventendeInnspillTilArbeidsgiver != null &&
                it.avventendeInnspillTilArbeidsgiver.trim().isNullOrEmpty()
        }

    RuleResult(
        ruleInputs = mapOf("manglendeInnspillArbeidsgiver" to manglendeInnspillArbeidsgiver),
        rule = PeriodLogicRules.MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER,
        ruleResult = manglendeInnspillArbeidsgiver,
    )
}

val avventendeOver16Dager: PeriodLogicRule = { sykmelding, _ ->
    val perioder = sykmelding.perioder

    val avventendeOver16Dager =
        perioder
            .filter { it.avventendeInnspillTilArbeidsgiver != null }
            .any { (it.fom..it.tom).daysBetween() > 16 }

    RuleResult(
        ruleInputs = mapOf("avventendeOver16Dager" to avventendeOver16Dager),
        rule = PeriodLogicRules.AVVENTENDE_SYKMELDING_OVER_16_DAGER,
        ruleResult = avventendeOver16Dager,
    )
}

val forMangeBehandlingsDagerPrUke: PeriodLogicRule = { sykmelding, _ ->
    val perioder = sykmelding.perioder

    val forMangeBehandlingsDagerPrUke =
        perioder.any {
            it.behandlingsdager != null && it.behandlingsdager > it.range().startedWeeksBetween()
        }

    RuleResult(
        ruleInputs = mapOf("forMangeBehandlingsDagerPrUke" to forMangeBehandlingsDagerPrUke),
        rule = PeriodLogicRules.FOR_MANGE_BEHANDLINGSDAGER_PER_UKE,
        ruleResult = forMangeBehandlingsDagerPrUke,
    )
}

val gradertOver99Prosent: PeriodLogicRule = { sykmelding, _ ->
    val gradertePerioder = sykmelding.perioder.mapNotNull { it.gradert }

    val gradertOver99Prosent = gradertePerioder.any { it.grad > 99 }

    RuleResult(
        ruleInputs = mapOf("gradertePerioder" to gradertePerioder),
        rule = PeriodLogicRules.GRADERT_SYKMELDING_OVER_99_PROSENT,
        ruleResult = gradertOver99Prosent,
    )
}

val gradert0Prosent: PeriodLogicRule = { sykmelding, _ ->
    val gradertePerioder = sykmelding.perioder.mapNotNull { it.gradert }

    val gradert0Prosent = gradertePerioder.any { it.grad == 0 }

    RuleResult(
        ruleInputs = mapOf("gradertePerioder" to gradertePerioder),
        rule = PeriodLogicRules.GRADERT_SYKMELDING_0_PROSENT,
        ruleResult = gradert0Prosent,
    )
}

val inneholderBehandlingsDager: PeriodLogicRule = { sykmelding, _ ->
    val perioder = sykmelding.perioder

    val inneholderBehandlingsDager = perioder.any { it.behandlingsdager != null }

    RuleResult(
        ruleInputs = mapOf("inneholderBehandlingsDager" to inneholderBehandlingsDager),
        rule = PeriodLogicRules.SYKMELDING_MED_BEHANDLINGSDAGER,
        ruleResult = inneholderBehandlingsDager,
    )
}
