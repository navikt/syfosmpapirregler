package no.nav.syfo.papirsykemelding.service

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.logstash.logback.argument.StructuredArguments.fields
import no.nav.syfo.client.legesuspensjon.LegeSuspensjonClient
import no.nav.syfo.client.norskhelsenett.Behandler
import no.nav.syfo.client.norskhelsenett.NorskHelsenettClient
import no.nav.syfo.client.syketilfelle.SyketilfelleClient
import no.nav.syfo.metrics.RULE_NODE_RULE_HIT_COUNTER
import no.nav.syfo.metrics.RULE_NODE_RULE_PATH_COUNTER
import no.nav.syfo.model.ReceivedSykmelding
import no.nav.syfo.model.RuleInfo
import no.nav.syfo.model.Status
import no.nav.syfo.model.ValidationResult
import no.nav.syfo.papirsykemelding.model.LoggingMeta
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.papirsykemelding.model.sortedFOMDate
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.dsl.TreeOutput
import no.nav.syfo.papirsykemelding.rules.dsl.printRulePath
import no.nav.syfo.pdl.FodselsdatoService
import no.nav.syfo.util.secureLogObjectMapper
import no.nav.syfo.util.securelog
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@DelicateCoroutinesApi
class PapirsykemeldingRegelService(
    private val legeSuspensjonClient: LegeSuspensjonClient,
    private val syketilfelleClient: SyketilfelleClient,
    private val norskHelsenettClient: NorskHelsenettClient,
    private val juridiskVurderingService: JuridiskVurderingService,
    private val fodselsdatoService: FodselsdatoService,
    private val ruleExecutionService: RuleExecutionService,
    private val sykmeldingService: SykmeldingService,
) {

    private val log: Logger = LoggerFactory.getLogger(PapirsykemeldingRegelService::class.java)

    suspend fun validateSykemelding(receivedSykmelding: ReceivedSykmelding): ValidationResult {
        val loggingMeta =
            LoggingMeta(
                mottakId = receivedSykmelding.navLogId,
                orgNr = receivedSykmelding.legekontorOrgNr,
                msgId = receivedSykmelding.msgId,
                sykmeldingId = receivedSykmelding.sykmelding.id,
            )

        log.info("Received papirsykmelding, checking rules, {}", fields(loggingMeta))

        val fodselsdato =
            fodselsdatoService.getFodselsdato(receivedSykmelding.personNrPasient, loggingMeta)

        val ruleMetadata =
            RuleMetadata(
                receivedDate = receivedSykmelding.mottattDato,
                signatureDate = receivedSykmelding.sykmelding.signaturDato,
                behandletTidspunkt = receivedSykmelding.sykmelding.behandletTidspunkt,
                patientPersonNumber = receivedSykmelding.personNrPasient,
                rulesetVersion = receivedSykmelding.rulesetVersion,
                legekontorOrgnr = receivedSykmelding.legekontorOrgNr,
                tssid = receivedSykmelding.tssid,
                pasientFodselsdato = fodselsdato,
            )

        return validateSykemelding(receivedSykmelding, ruleMetadata, loggingMeta)
    }

    private suspend fun validateSykemelding(
        receivedSykmelding: ReceivedSykmelding,
        ruleMetadata: RuleMetadata,
        loggingMeta: LoggingMeta,
    ): ValidationResult =
        with(GlobalScope) {
            val behandler =
                getBehandlerAsync(receivedSykmelding, loggingMeta).await()
                    ?: return getAndRegisterBehandlerNotInHPR()

            val doctorSuspendedAsync = getDoctorSuspendedAsync(receivedSykmelding)
            val syketilfelleStartdatoAsync =
                getErNyttSyketilfelleAsync(receivedSykmelding, loggingMeta)

            val syketilfelleStartdato = syketilfelleStartdatoAsync.await()
            val ettersendingOgForlengelse =
                if (erTilbakedatert(receivedSykmelding)) {
                    sykmeldingService.getSykmeldingMetadataInfo(
                        receivedSykmelding.personNrPasient,
                        receivedSykmelding.sykmelding,
                        loggingMeta
                    )
                } else {
                    SykmeldingMetadataInfo(null, emptyList())
                }
            val ruleMetadataSykmelding =
                RuleMetadataSykmelding(
                    ruleMetadata = ruleMetadata,
                    doctorSuspensjon = doctorSuspendedAsync.await(),
                    behandlerOgStartdato = BehandlerOgStartdato(behandler, syketilfelleStartdato),
                    ettersendingOgForlengelse,
                )

            securelog.info(
                "Rule input data: sykmelding: ${receivedSykmelding.sykmelding} ," +
                    "ruleMetadataSykmelding: $ruleMetadataSykmelding"
            )

            val result =
                ruleExecutionService.runRules(receivedSykmelding.sykmelding, ruleMetadataSykmelding)
            result.forEach {
                RULE_NODE_RULE_PATH_COUNTER.labels(
                        it.first.printRulePath(),
                    )
                    .inc()
            }

            juridiskVurderingService.processRuleResults(receivedSykmelding, result)
            val validationResult = validationResult(result.map { it.first })
            RULE_NODE_RULE_HIT_COUNTER.labels(
                    validationResult.status.name,
                    validationResult.ruleHits.firstOrNull()?.ruleName
                        ?: validationResult.status.name,
                )
                .inc()

            if (validationResult.status != Status.OK) {
                securelog.info(
                    "RuleResult for ${receivedSykmelding.sykmelding.id}: ${
                        secureLogObjectMapper
                        .writeValueAsString(result.filter { it.first.treeResult.status != Status.OK })}"
                )
            }
            return validationResult
        }

    private fun getAndRegisterBehandlerNotInHPR(): ValidationResult {
        return ValidationResult(
            status = Status.MANUAL_PROCESSING,
            ruleHits =
                listOf(
                    RuleInfo(
                        ruleName = "BEHANLDER_IKKE_I_HPR",
                        messageForSender =
                            "Den som har skrevet sykmeldingen din har ikke autorisasjon til dette.",
                        messageForUser = "Behandler er ikke register i HPR",
                        ruleStatus = Status.MANUAL_PROCESSING,
                    ),
                ),
        )
    }

    private fun GlobalScope.getBehandlerAsync(
        receivedSykmelding: ReceivedSykmelding,
        loggingMeta: LoggingMeta,
    ): Deferred<Behandler?> {
        return async {
            norskHelsenettClient.finnBehandler(
                behandlerFnr = receivedSykmelding.personNrLege,
                msgId = receivedSykmelding.msgId,
                loggingMeta = loggingMeta,
            )
        }
    }

    private fun erTilbakedatert(receivedSykmelding: ReceivedSykmelding): Boolean =
        receivedSykmelding.sykmelding.signaturDato
            .toLocalDate()
            .isAfter(receivedSykmelding.sykmelding.perioder.sortedFOMDate().first().plusDays(3))

    private fun GlobalScope.getErNyttSyketilfelleAsync(
        receivedSykmelding: ReceivedSykmelding,
        loggingMeta: LoggingMeta
    ): Deferred<LocalDate?> {
        return async {
            syketilfelleClient.finnStartdatoForSammenhengendeSyketilfelle(
                receivedSykmelding.personNrPasient,
                receivedSykmelding.sykmelding.perioder,
                loggingMeta
            )
        }
    }

    private fun GlobalScope.getDoctorSuspendedAsync(
        receivedSykmelding: ReceivedSykmelding
    ): Deferred<Boolean> {
        return async {
            val signaturDatoString =
                DateTimeFormatter.ISO_DATE.format(receivedSykmelding.sykmelding.signaturDato)
            legeSuspensjonClient
                .checkTherapist(
                    receivedSykmelding.personNrLege,
                    receivedSykmelding.navLogId,
                    signaturDatoString,
                )
                .suspendert
        }
    }

    private fun validationResult(
        results: List<TreeOutput<out Enum<*>, RuleResult>>
    ): ValidationResult =
        ValidationResult(
            status =
                results
                    .map { result -> result.treeResult.status }
                    .let {
                        it.firstOrNull { status -> status == Status.INVALID }
                            ?: it.firstOrNull { status -> status == Status.MANUAL_PROCESSING }
                                ?: Status.OK
                    },
            ruleHits =
                results
                    .mapNotNull { it.treeResult.ruleHit }
                    .map { result ->
                        RuleInfo(
                            result.rule,
                            result.messageForSender,
                            result.messageForUser,
                            result.status,
                        )
                    },
        )
}

data class BehandlerOgStartdato(
    val behandler: Behandler,
    val startdato: LocalDate?,
)

data class RuleMetadataSykmelding(
    val ruleMetadata: RuleMetadata,
    val doctorSuspensjon: Boolean,
    val behandlerOgStartdato: BehandlerOgStartdato,
    val sykmeldingMetadataInfo: SykmeldingMetadataInfo,
)
