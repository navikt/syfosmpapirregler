package no.nav.syfo.papirsykemelding.service

import io.ktor.util.KtorExperimentalAPI
import io.prometheus.client.Counter
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.logstash.logback.argument.StructuredArguments.fields
import no.nav.syfo.application.metrics.RULE_HIT_STATUS_COUNTER
import no.nav.syfo.client.diskresjonskode.DiskresjonskodeService
import no.nav.syfo.client.legesuspensjon.LegeSuspensjonClient
import no.nav.syfo.client.norskhelsenett.Behandler
import no.nav.syfo.client.norskhelsenett.NorskHelsenettClient
import no.nav.syfo.client.syketilfelle.SyketilfelleClient
import no.nav.syfo.client.syketilfelle.model.intoSyketilfelle
import no.nav.syfo.model.ReceivedSykmelding
import no.nav.syfo.model.RuleInfo
import no.nav.syfo.model.Status
import no.nav.syfo.model.ValidationResult
import no.nav.syfo.papirsykemelding.model.LoggingMeta
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.papirsykemelding.rules.HPRRuleChain
import no.nav.syfo.papirsykemelding.rules.LegesuspensjonRuleChain
import no.nav.syfo.papirsykemelding.rules.PeriodLogicRuleChain
import no.nav.syfo.papirsykemelding.rules.PostDiskresjonskodeRuleChain
import no.nav.syfo.papirsykemelding.rules.RuleMetadataAndForstegangsSykemelding
import no.nav.syfo.papirsykemelding.rules.SyketilfelleRuleChain
import no.nav.syfo.papirsykemelding.rules.ValideringRuleChain
import no.nav.syfo.rules.RULE_HIT_COUNTER
import no.nav.syfo.rules.Rule
import no.nav.syfo.rules.executeFlow
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@KtorExperimentalAPI
class PapirsykemeldingRegelService(
    private val ruleHitStatusCounter: Counter = RULE_HIT_STATUS_COUNTER,
    private val diskresjonskodeService: DiskresjonskodeService,
    private val legeSuspensjonClient: LegeSuspensjonClient,
    private val syketilfelleClient: SyketilfelleClient,
    private val norskHelsenettClient: NorskHelsenettClient
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

        val ruleMetadata = RuleMetadata(
            receivedDate = receivedSykmelding.mottattDato,
            signatureDate = receivedSykmelding.sykmelding.signaturDato,
            behandletTidspunkt = receivedSykmelding.sykmelding.behandletTidspunkt,
            patientPersonNumber = receivedSykmelding.personNrPasient,
            rulesetVersion = receivedSykmelding.rulesetVersion,
            legekontorOrgnr = receivedSykmelding.legekontorOrgNr,
            tssid = receivedSykmelding.tssid
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

        val diskresjonskodeAsync = hentDiskresjonskodeAsync(ruleMetadata)
        val doctorSuspendedAsync = getDoctorSuspendedAsync(receivedSykmelding)
        val erNyttSyketilfelleAync = getErNyttSyketilfelleAsync(receivedSykmelding)

        val results = listOf(
            ValideringRuleChain.values().executeFlow(receivedSykmelding.sykmelding, ruleMetadata),
            PostDiskresjonskodeRuleChain.values().executeFlow(
                receivedSykmelding.sykmelding, diskresjonskodeAsync.await()
            ),
            HPRRuleChain.values().executeFlow(receivedSykmelding.sykmelding, behandler),
            LegesuspensjonRuleChain.values().executeFlow(receivedSykmelding.sykmelding, doctorSuspendedAsync.await()),
            PeriodLogicRuleChain.values().executeFlow(receivedSykmelding.sykmelding, ruleMetadata),
            SyketilfelleRuleChain.values().executeFlow(
                receivedSykmelding.sykmelding,
                getRuleMetadataAndForstegangsSykemelding(ruleMetadata, erNyttSyketilfelleAync)
            )
        ).flatten()
        log.info("Rules hit {}, {}", results.map { it.name }, fields(loggingMeta))
        return validationResult(results)
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

    private suspend fun getRuleMetadataAndForstegangsSykemelding(
        ruleMetadata: RuleMetadata,
        erNyttSyketilfelleAync: Deferred<Boolean>
    ): RuleMetadataAndForstegangsSykemelding =
        RuleMetadataAndForstegangsSykemelding(ruleMetadata, erNyttSyketilfelleAync.await())

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

    private fun GlobalScope.getErNyttSyketilfelleAsync(receivedSykmelding: ReceivedSykmelding): Deferred<Boolean> {
        return async {
            val syketilfelle = receivedSykmelding.sykmelding.perioder.intoSyketilfelle(
                receivedSykmelding.sykmelding.pasientAktoerId, receivedSykmelding.mottattDato,
                receivedSykmelding.msgId
            )

            syketilfelleClient.fetchErNytttilfelle(syketilfelle, receivedSykmelding.sykmelding.pasientAktoerId)
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

    private fun GlobalScope.hentDiskresjonskodeAsync(ruleMetadata: RuleMetadata) =
        async { diskresjonskodeService.hentDiskresjonskode(ruleMetadata.patientPersonNumber) }

    private fun validationResult(results: List<Rule<Any>>): ValidationResult = ValidationResult(
        status = results
            .map { status -> status.status }.let {
                it.firstOrNull { status -> status == Status.INVALID }
                    ?: it.firstOrNull { status -> status == Status.MANUAL_PROCESSING }
                    ?: Status.OK
            },
        ruleHits = results.map { rule -> RuleInfo(rule.name, rule.messageForSender!!, rule.messageForUser!!, rule.status) }
    )
}
