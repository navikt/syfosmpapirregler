package no.nav.syfo.papirsykemelding.service

import io.prometheus.client.Counter
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.logstash.logback.argument.StructuredArguments.fields
import no.nav.syfo.application.metrics.RULE_HIT_COUNTER
import no.nav.syfo.application.metrics.RULE_HIT_STATUS_COUNTER
import no.nav.syfo.client.legesuspensjon.LegeSuspensjonClient
import no.nav.syfo.client.norskhelsenett.Behandler
import no.nav.syfo.client.norskhelsenett.NorskHelsenettClient
import no.nav.syfo.client.syketilfelle.SyketilfelleClient
import no.nav.syfo.model.ReceivedSykmelding
import no.nav.syfo.model.RuleInfo
import no.nav.syfo.model.RuleResult
import no.nav.syfo.model.Status
import no.nav.syfo.model.ValidationResult
import no.nav.syfo.papirsykemelding.model.LoggingMeta
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.papirsykemelding.rules.BehandlerOgStartdato
import no.nav.syfo.papirsykemelding.rules.HPRRuleChain
import no.nav.syfo.papirsykemelding.rules.LegesuspensjonRuleChain
import no.nav.syfo.papirsykemelding.rules.PeriodLogicRuleChain
import no.nav.syfo.papirsykemelding.rules.RuleMetadataAndForstegangsSykemelding
import no.nav.syfo.papirsykemelding.rules.SyketilfelleRuleChain
import no.nav.syfo.papirsykemelding.rules.ValidationRuleChain
import no.nav.syfo.pdl.FodselsdatoService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@DelicateCoroutinesApi
class PapirsykemeldingRegelService(
    private val ruleHitStatusCounter: Counter = RULE_HIT_STATUS_COUNTER,
    private val legeSuspensjonClient: LegeSuspensjonClient,
    private val syketilfelleClient: SyketilfelleClient,
    private val norskHelsenettClient: NorskHelsenettClient,
    private val juridiskVurderingService: JuridiskVurderingService,
    private val fodselsdatoService: FodselsdatoService
) {

    private val log: Logger = LoggerFactory.getLogger(PapirsykemeldingRegelService::class.java)

    suspend fun validateSykemelding(receivedSykmelding: ReceivedSykmelding): ValidationResult {
        val loggingMeta = LoggingMeta(
            mottakId = receivedSykmelding.navLogId,
            orgNr = receivedSykmelding.legekontorOrgNr,
            msgId = receivedSykmelding.msgId,
            sykmeldingId = receivedSykmelding.sykmelding.id
        )

        log.info("Received papirsykmelding, checking rules, {}", fields(loggingMeta))

        val fodselsdato = fodselsdatoService.getFodselsdato(receivedSykmelding.personNrPasient, loggingMeta)

        val ruleMetadata = RuleMetadata(
            receivedDate = receivedSykmelding.mottattDato,
            signatureDate = receivedSykmelding.sykmelding.signaturDato,
            behandletTidspunkt = receivedSykmelding.sykmelding.behandletTidspunkt,
            patientPersonNumber = receivedSykmelding.personNrPasient,
            rulesetVersion = receivedSykmelding.rulesetVersion,
            legekontorOrgnr = receivedSykmelding.legekontorOrgNr,
            tssid = receivedSykmelding.tssid,
            pasientFodselsdato = fodselsdato
        )

        val validationResult = validateSykemelding(receivedSykmelding, ruleMetadata, loggingMeta)
        ruleHitStatusCounter.labels(validationResult.status.name).inc()
        return validationResult
    }

    private suspend fun validateSykemelding(
        receivedSykmelding: ReceivedSykmelding,
        ruleMetadata: RuleMetadata,
        loggingMeta: LoggingMeta
    ): ValidationResult = with(GlobalScope) {
        val behandler = getBehandlerAsync(receivedSykmelding, loggingMeta).await() ?: return getAndRegisterBehandlerNotInHPR()

        val doctorSuspendedAsync = getDoctorSuspendedAsync(receivedSykmelding)
        val syketilfelleStartdatoAsync = getErNyttSyketilfelleAsync(receivedSykmelding, loggingMeta)

        val syketilfelleStartdato = syketilfelleStartdatoAsync.await()
        val results = listOf(
            ValidationRuleChain(receivedSykmelding.sykmelding, ruleMetadata).executeRules(),
            HPRRuleChain(receivedSykmelding.sykmelding, BehandlerOgStartdato(behandler, syketilfelleStartdato)).executeRules(),
            LegesuspensjonRuleChain(doctorSuspendedAsync.await()).executeRules(),
            PeriodLogicRuleChain(receivedSykmelding.sykmelding, ruleMetadata).executeRules(),
            SyketilfelleRuleChain(
                receivedSykmelding.sykmelding,
                RuleMetadataAndForstegangsSykemelding(
                    ruleMetadata = ruleMetadata,
                    erNyttSyketilfelle = syketilfelleStartdato == null
                )
            ).executeRules()
        ).flatten()

        juridiskVurderingService.processRuleResults(receivedSykmelding, results)

        logRuleResultMetrics(results)

        log.info("Rules hit ${results.filter { it.result }.map { it.rule.name }}, ${fields(loggingMeta)}")
        return validationResult(results)
    }

    private fun logRuleResultMetrics(result: List<RuleResult<*>>) {
        result
            .filter { it.result }
            .forEach {
                RULE_HIT_COUNTER.labels(it.rule.name).inc()
            }
    }

    private fun getAndRegisterBehandlerNotInHPR(): ValidationResult {
        RULE_HIT_COUNTER.labels("BEHANLDER_IKKE_I_HPR").inc()
        return ValidationResult(
            status = Status.MANUAL_PROCESSING,
            ruleHits = listOf(
                RuleInfo(
                    ruleName = "BEHANLDER_IKKE_I_HPR",
                    messageForSender = "Den som har skrevet sykmeldingen din har ikke autorisasjon til dette.",
                    messageForUser = "Behandler er ikke register i HPR",
                    ruleStatus = Status.MANUAL_PROCESSING
                )
            )
        )
    }

    private fun GlobalScope.getBehandlerAsync(
        receivedSykmelding: ReceivedSykmelding,
        loggingMeta: LoggingMeta
    ): Deferred<Behandler?> {
        return async {
            norskHelsenettClient.finnBehandler(
                behandlerFnr = receivedSykmelding.personNrLege,
                msgId = receivedSykmelding.msgId,
                loggingMeta = loggingMeta
            )
        }
    }

    private fun GlobalScope.getErNyttSyketilfelleAsync(receivedSykmelding: ReceivedSykmelding, loggingMeta: LoggingMeta): Deferred<LocalDate?> {
        return async {
            syketilfelleClient.finnStartdatoForSammenhengendeSyketilfelle(receivedSykmelding.personNrPasient, receivedSykmelding.sykmelding.perioder, loggingMeta)
        }
    }

    private fun GlobalScope.getDoctorSuspendedAsync(receivedSykmelding: ReceivedSykmelding): Deferred<Boolean> {
        return async {
            val signaturDatoString = DateTimeFormatter.ISO_DATE.format(receivedSykmelding.sykmelding.signaturDato)
            legeSuspensjonClient.checkTherapist(
                receivedSykmelding.personNrLege,
                receivedSykmelding.navLogId,
                signaturDatoString
            ).suspendert
        }
    }

    private fun validationResult(results: List<RuleResult<*>>): ValidationResult = ValidationResult(
        status = results
            .filter { it.result }
            .map { result -> result.rule.status }.let {
                it.firstOrNull { status -> status == Status.INVALID }
                    ?: it.firstOrNull { status -> status == Status.MANUAL_PROCESSING }
                    ?: Status.OK
            },
        ruleHits = results
            .filter { it.result }
            .map { result ->
                RuleInfo(
                    result.rule.name,
                    result.rule.messageForSender,
                    result.rule.messageForUser,
                    result.rule.status
                )
            }
    )
}
