package no.nav.syfo.papirsykemelding.service

import io.prometheus.client.Counter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.logstash.logback.argument.StructuredArguments.fields
import no.nav.syfo.application.metrics.RULE_HIT_STATUS_COUNTER
import no.nav.syfo.client.DiskresjonskodeService
import no.nav.syfo.model.ReceivedSykmelding
import no.nav.syfo.model.RuleInfo
import no.nav.syfo.model.Status
import no.nav.syfo.model.ValidationResult
import no.nav.syfo.papirsykemelding.model.LoggingMeta
import no.nav.syfo.papirsykemelding.model.RuleMetadata
import no.nav.syfo.papirsykemelding.rules.PostDiskresjonskodeRuleChain
import no.nav.syfo.rules.Rule
import no.nav.syfo.rules.executeFlow
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PapirsykemeldingRegelService(
    private val ruleHitStatusCounter: Counter = RULE_HIT_STATUS_COUNTER,
    private val diskresjonskodeService: DiskresjonskodeService
) {

    private val log: Logger = LoggerFactory.getLogger(PapirsykemeldingRegelService::class.java)

    suspend fun validateSykemelding(receivedSykmelding: ReceivedSykmelding): ValidationResult = with(GlobalScope) {

        val loggingMeta = LoggingMeta(
            mottakId = receivedSykmelding.navLogId,
            orgNr = receivedSykmelding.legekontorOrgNr,
            msgId = receivedSykmelding.msgId,
            sykmeldingId = receivedSykmelding.sykmelding.id)

        log.info("Received papirsykmelding, checking rules, {}", fields(loggingMeta))

        val ruleMetadata = RuleMetadata(
            receivedDate = receivedSykmelding.mottattDato,
            signatureDate = receivedSykmelding.sykmelding.signaturDato,
            patientPersonNumber = receivedSykmelding.personNrPasient,
            rulesetVersion = receivedSykmelding.rulesetVersion,
            legekontorOrgnr = receivedSykmelding.legekontorOrgNr,
            tssid = receivedSykmelding.tssid
        )

        val diskresjonskodeAsync = async { diskresjonskodeService.hentDiskresjonskode(ruleMetadata.patientPersonNumber) }

        val results = listOf(
            PostDiskresjonskodeRuleChain.values().executeFlow(receivedSykmelding.sykmelding, diskresjonskodeAsync.await())
        ).flatten()

        log.info("Rules hit {}, {}", results.map { it.name }, fields(loggingMeta))

        val validationResult = validationResult(results)

        ruleHitStatusCounter.labels(validationResult.status.name).inc()

        return validationResult
    }

    private fun validationResult(results: List<Rule<Any>>): ValidationResult = ValidationResult(
        status = results
            .map { status -> status.status }.let {
                it.firstOrNull { status -> status == Status.INVALID }
                    ?: it.firstOrNull { status -> status == Status.MANUAL_PROCESSING }
                    ?: Status.OK
            },
        ruleHits = results.map { rule -> RuleInfo(rule.name, rule.messageForSender!!, rule.messageForUser!!) }
    )
}
