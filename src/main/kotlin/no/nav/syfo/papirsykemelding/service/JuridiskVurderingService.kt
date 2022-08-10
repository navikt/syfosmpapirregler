package no.nav.syfo.papirsykemelding.service

import no.nav.syfo.model.ReceivedSykmelding
import no.nav.syfo.model.RuleResult
import no.nav.syfo.model.Status
import no.nav.syfo.model.juridisk.JuridiskUtfall
import no.nav.syfo.model.juridisk.JuridiskVurdering
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.LocalDateTime
import java.util.UUID

data class JuridiskVurderingResult(
    val juridiskeVurderinger: List<JuridiskVurdering>,
)

class JuridiskVurderingService(
    private val kafkaProducer: KafkaProducer<String, JuridiskVurderingResult>,
    val juridiskVurderingTopic: String,
    val versjonAvKode: String
) {
    companion object {
        val EVENT_NAME = "subsumsjon"
        val VERSION = "1.0.0"
        val KILDE = "syfosmpapirregler"
    }

    fun processRuleResults(
        receivedSykmelding: ReceivedSykmelding,
        result: List<RuleResult<*>>,
    ) {
        val juridiskVurderingResult = JuridiskVurderingResult(
            juridiskeVurderinger = result
                .filter { it.rule.juridiskHenvisning != null }
                .map { resultToJuridiskVurdering(receivedSykmelding, it) }
        )
        kafkaProducer.send(
            ProducerRecord(
                juridiskVurderingTopic,
                receivedSykmelding.sykmelding.id,
                juridiskVurderingResult
            )
        ).get()
    }

    private fun resultToJuridiskVurdering(
        receivedSykmelding: ReceivedSykmelding,
        ruleResult: RuleResult<*>,
    ): JuridiskVurdering {
        return JuridiskVurdering(
            id = UUID.randomUUID().toString(),
            eventName = EVENT_NAME,
            version = VERSION,
            kilde = KILDE,
            versjonAvKode = versjonAvKode,
            fodselsnummer = receivedSykmelding.personNrPasient,
            juridiskHenvisning = ruleResult.rule.juridiskHenvisning
                ?: throw RuntimeException("JuridiskHenvisning kan ikke være null"),
            sporing = mapOf(
                "sykmelding" to receivedSykmelding.sykmelding.id
            ),
            input = ruleResult.rule.toInputMap(),
            utfall = toJuridiskUtfall(
                when (ruleResult.result) {
                    true -> ruleResult.rule.status
                    else -> Status.OK
                }
            ),
            tidsstempel = LocalDateTime.now()
        )
    }

    private fun toJuridiskUtfall(status: Status) = when (status) {
        Status.OK -> {
            JuridiskUtfall.VILKAR_OPPFYLT
        }
        Status.INVALID -> {
            JuridiskUtfall.VILKAR_IKKE_OPPFYLT
        }
        Status.MANUAL_PROCESSING -> {
            JuridiskUtfall.VILKAR_UAVKLART
        }
        else -> {
            JuridiskUtfall.VILKAR_UAVKLART
        }
    }
}
