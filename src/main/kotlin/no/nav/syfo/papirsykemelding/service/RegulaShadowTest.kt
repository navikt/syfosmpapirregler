package no.nav.syfo.papirsykemelding.service

import no.nav.syfo.client.MerknadType
import no.nav.syfo.client.PeriodetypeDTO
import no.nav.syfo.client.RegelStatusDTO
import no.nav.syfo.client.SykmeldingDTO
import no.nav.syfo.client.SykmeldingsperiodeDTO
import no.nav.syfo.client.norskhelsenett.Godkjenning
import no.nav.syfo.model.Periode
import no.nav.syfo.model.ReceivedSykmelding
import no.nav.syfo.model.SporsmalSvar
import no.nav.syfo.model.ValidationResult
import no.nav.syfo.papirsykemelding.rules.common.Juridisk
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.dsl.TreeOutput
import no.nav.syfo.papirsykemelding.rules.dsl.printRulePath
import no.nav.tsm.regulus.regula.RegulaAvsender
import no.nav.tsm.regulus.regula.RegulaBehandler
import no.nav.tsm.regulus.regula.RegulaMeta
import no.nav.tsm.regulus.regula.RegulaPasient
import no.nav.tsm.regulus.regula.RegulaPayload
import no.nav.tsm.regulus.regula.executeRegulaRules
import no.nav.tsm.regulus.regula.executor.ExecutionMode
import no.nav.tsm.regulus.regula.payload.Aktivitet
import no.nav.tsm.regulus.regula.payload.AnnenFravarsArsak
import no.nav.tsm.regulus.regula.payload.BehandlerGodkjenning
import no.nav.tsm.regulus.regula.payload.BehandlerKode
import no.nav.tsm.regulus.regula.payload.BehandlerPeriode
import no.nav.tsm.regulus.regula.payload.BehandlerTilleggskompetanse
import no.nav.tsm.regulus.regula.payload.Diagnose
import no.nav.tsm.regulus.regula.payload.TidligereSykmelding
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val log: Logger = LoggerFactory.getLogger("regula-shadow-test")

fun regulaShadowTest(
    receivedSykmelding: ReceivedSykmelding,
    ruleMetadataSykmelding: RuleMetadataSykmelding,
    tidligereSykmeldinger: List<SykmeldingDTO>,
    oldResult: List<Pair<TreeOutput<out Enum<*>, RuleResult>, Juridisk>>,
    oldValidationResult: ValidationResult,
) {
    try {
        val oldSykmelding = receivedSykmelding.sykmelding
        val mappedTidligereSykmeldinger =
            tidligereSykmeldinger
                // TODO: Should this be controlled by the lib? Probably
                .filter { it.behandlingsutfall.status != RegelStatusDTO.INVALID }
                .filterNot { harTilbakedatertMerknad(it) }
                .filter { it.medisinskVurdering?.hovedDiagnose?.kode != null }
                .filter {
                    it.medisinskVurdering?.hovedDiagnose?.kode ==
                        oldSykmelding.medisinskVurdering.hovedDiagnose?.kode
                }
                .map {
                    TidligereSykmelding(
                        sykmeldingId = it.id,
                        aktivitet =
                            it.sykmeldingsperioder.map(
                                SykmeldingsperiodeDTO::toSykmeldingPeriode,
                            ),
                        hoveddiagnose =
                            it.medisinskVurdering?.hovedDiagnose?.let { diagnose ->
                                Diagnose(
                                    kode = diagnose.kode,
                                    system = "TODO: System kommer ikke fra registeret? :huh:",
                                )
                            },
                    )
                }
        val rulePayload =
            RegulaPayload(
                sykmeldingId = oldSykmelding.id,
                hoveddiagnose =
                    oldSykmelding.medisinskVurdering.hovedDiagnose?.let {
                        Diagnose(kode = it.kode, system = it.system)
                    },
                bidiagnoser =
                    oldSykmelding.medisinskVurdering.biDiagnoser.map {
                        Diagnose(kode = it.kode, system = it.system)
                    },
                annenFravarsArsak =
                    oldSykmelding.medisinskVurdering.annenFraversArsak?.let { annenFraversArsak ->
                        AnnenFravarsArsak(
                            beskrivelse = annenFraversArsak.beskrivelse,
                            grunn = annenFraversArsak.grunn.map { it.name },
                        )
                    },
                aktivitet = oldSykmelding.perioder.map(Periode::toSykmeldingPeriode),
                utdypendeOpplysninger = mapSvar(oldSykmelding.utdypendeOpplysninger),
                kontaktPasientBegrunnelseIkkeKontakt =
                    oldSykmelding.kontaktMedPasient.begrunnelseIkkeKontakt,
                tidligereSykmeldinger = mappedTidligereSykmeldinger,
                behandletTidspunkt = ruleMetadataSykmelding.ruleMetadata.behandletTidspunkt,
                pasient =
                    RegulaPasient(
                        ident = receivedSykmelding.personNrPasient,
                        fodselsdato = ruleMetadataSykmelding.ruleMetadata.pasientFodselsdato,
                    ),
                meta =
                    RegulaMeta.LegacyMeta(
                        mottattDato = ruleMetadataSykmelding.ruleMetadata.receivedDate,
                        signaturdato = oldSykmelding.signaturDato,
                        rulesetVersion = receivedSykmelding.rulesetVersion,
                    ),
                behandler =
                    RegulaBehandler(
                        suspendert = ruleMetadataSykmelding.doctorSuspensjon,
                        fnr = oldSykmelding.behandler.fnr,
                        legekontorOrgnr = ruleMetadataSykmelding.ruleMetadata.legekontorOrgnr,
                        godkjenninger =
                            ruleMetadataSykmelding.behandlerOgStartdato.behandler.godkjenninger.map(
                                Godkjenning::toBehandlerGodkjenning,
                            ),
                    ),
                // TODO: avsenderSammeSomPasient regel finnes ikke i syfosmpapirregler, burde være nullable?
                avsender = RegulaAvsender("PAPIRSYKMELDING"),
            )

        val newResult =
            executeRegulaRules(
                rulePayload,
                mode = ExecutionMode.PAPIR,
            )

        val newVsOld: List<Pair<String, String>> =
            oldResult
                .map { it.first.printRulePath() }
                .zip(
                    newResult.results.map { it.rulePath },
                )

        val allPathsEqual = newVsOld.all { (old, new) -> old == new }

        if (allPathsEqual) {
            log.info(
                """ ✅ REGULA SHADOW TEST Result: OK
                | SykmeldingID: ${oldSykmelding.id}
                | Outcome: ${newResult.status.name} (${oldValidationResult.status.name})
                | Chains executed: ${oldResult.size} / ${newResult.results.size}
            """
                    .trimMargin(),
            )
        } else {
            log.warn(
                """ ❌ REGULA SHADOW TEST Result: DIVERGENCE DETECTED
                    | SykmeldingID: ${oldSykmelding.id}
                    | Outcome: ${newResult.status.name} (${oldValidationResult.status.name})
                    | Chains executed: ${oldResult.size} / ${newResult.results.size}
                    | Diverging paths count: ${newVsOld.count { (old, new) -> old != new }} 
                    | 
                    | Some meta:
                    |  * Tidligere sykmeldinger count: ${mappedTidligereSykmeldinger.size}
                    | 
                    | Diverging paths:
                    |${
                    newVsOld.filter { (old, new) -> old != new }.joinToString("\n") { (old, new) ->
                        "Old: $old\nNew: $new"
                    }
                }
                    """
                    .trimMargin(),
            )
        }
    } catch (e: Exception) {
        log.error("Regulus Regula smoke test failed", e)
    }
}

private fun Periode.toSykmeldingPeriode(): Aktivitet =
    when {
        aktivitetIkkeMulig != null ->
            Aktivitet.IkkeMulig(
                fom = fom,
                tom = tom,
            )

        gradert != null ->
            Aktivitet.Gradert(
                fom = fom,
                tom = tom,
                grad = gradert.grad,
            )

        reisetilskudd ->
            Aktivitet.Reisetilskudd(
                fom = fom,
                tom = tom,
            )

        behandlingsdager != null ->
            Aktivitet.Behandlingsdager(
                fom = fom,
                tom = tom,
                behandlingsdager = behandlingsdager,
            )

        avventendeInnspillTilArbeidsgiver != null ->
            Aktivitet.Avventende(
                fom = fom,
                tom = tom,
                avventendeInnspillTilArbeidsgiver = avventendeInnspillTilArbeidsgiver,
            )

        else ->
            Aktivitet.Ugyldig(
                fom = fom,
                tom = tom,
            )
    }

private fun SykmeldingsperiodeDTO.toSykmeldingPeriode(): Aktivitet =
    when {
        type == PeriodetypeDTO.AKTIVITET_IKKE_MULIG ->
            Aktivitet.IkkeMulig(
                fom = fom,
                tom = tom,
            )

        type == PeriodetypeDTO.GRADERT && gradert != null ->
            Aktivitet.Gradert(
                fom = fom,
                tom = tom,
                grad = gradert.grad,
            )

        type == PeriodetypeDTO.REISETILSKUDD ->
            Aktivitet.Reisetilskudd(
                fom = fom,
                tom = tom,
            )

        type == PeriodetypeDTO.BEHANDLINGSDAGER ->
            Aktivitet.Behandlingsdager(
                fom = fom,
                tom = tom,
                behandlingsdager =
                    // TODO: Kommer ikke fra registeret, ikke nødvendig for testene, burde regula ha
                    // forskjellige typer for de to periode (gamle og nåværende)
                    0,
            )

        type == PeriodetypeDTO.AVVENTENDE ->
            Aktivitet.Avventende(
                fom = fom,
                tom = tom,
                // TODO: Kommer ikke fra registeret, ikke nødvendig for testene, burde regula ha
                // forskjellige typer for de to periode (gamle og nåværende)
                avventendeInnspillTilArbeidsgiver = null,
            )

        else -> {
            log.warn("Shadow test: Ukjent periode type: $type")
            Aktivitet.Ugyldig(
                fom = fom,
                tom = tom,
            )
        }
    }

private fun Godkjenning.toBehandlerGodkjenning() =
    BehandlerGodkjenning(
        helsepersonellkategori =
            helsepersonellkategori?.let {
                BehandlerKode(
                    oid = it.oid,
                    aktiv = it.aktiv,
                    verdi = it.verdi,
                )
            },
        tillegskompetanse =
            tillegskompetanse?.map { tillegskompetanse ->
                BehandlerTilleggskompetanse(
                    avsluttetStatus =
                        tillegskompetanse.avsluttetStatus?.let {
                            BehandlerKode(
                                oid = it.oid,
                                aktiv = it.aktiv,
                                verdi = it.verdi,
                            )
                        },
                    gyldig =
                        tillegskompetanse.gyldig?.let {
                            BehandlerPeriode(fra = it.fra, til = it.til)
                        },
                    type =
                        tillegskompetanse.type?.let {
                            BehandlerKode(
                                oid = it.oid,
                                aktiv = it.aktiv,
                                verdi = it.verdi,
                            )
                        },
                )
            },
        autorisasjon =
            autorisasjon?.let {
                BehandlerKode(
                    oid = it.oid,
                    aktiv = it.aktiv,
                    verdi = it.verdi,
                )
            },
    )

private fun mapSvar(
    input: Map<String, Map<String, SporsmalSvar>>
): Map<String, Map<String, Map<String, String>>> {
    return input.mapValues { (_, innerMap) ->
        innerMap.mapValues { (_, svar) ->
            mapOf(
                "sporsmal" to svar.sporsmal,
                "svar" to svar.svar,
                "restriksjoner" to svar.restriksjoner.joinToString(",") { it.name },
            )
        }
    }
}

private fun harTilbakedatertMerknad(sykmelding: SykmeldingDTO): Boolean {
    return sykmelding.merknader?.any { MerknadType.contains(it.type) } ?: false
}
