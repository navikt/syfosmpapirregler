package no.nav.syfo.papirsykemelding.service

import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.UUID
import no.nav.syfo.getEnvVar
import no.nav.syfo.model.ReceivedSykmelding
import no.nav.syfo.model.Status
import no.nav.syfo.model.juridisk.JuridiskUtfall
import no.nav.syfo.model.juridisk.JuridiskVurdering
import no.nav.syfo.papirsykemelding.rules.common.MedJuridisk
import no.nav.syfo.papirsykemelding.rules.common.RuleResult
import no.nav.syfo.papirsykemelding.rules.dsl.TreeOutput
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

data class JuridiskVurderingResult(
    val juridiskeVurderinger: List<JuridiskVurdering>,
)

class JuridiskVurderingService(
    private val kafkaProducer: KafkaProducer<String, JuridiskVurderingResult>,
    val juridiskVurderingTopic: String,
    val versjonsKode: String = getEnvVar("NAIS_APP_IMAGE"),
) {
    companion object {
        val EVENT_NAME = "subsumsjon"
        val VERSION = "1.0.0"
        val KILDE = "syfosmpapirregler"
    }

    fun processRuleResults(
        receivedSykmelding: ReceivedSykmelding,
        result: List<TreeOutput<out Enum<*>, RuleResult>>,
    ) {
        val juridiskVurderingResult =
            JuridiskVurderingResult(
                juridiskeVurderinger =
                    result.mapNotNull {
                        when (val juridisk = it.treeResult.juridisk) {
                            is MedJuridisk ->
                                resultToJuridiskVurdering(receivedSykmelding, it, juridisk)
                            else -> null
                        }
                    },
            )
        kafkaProducer
            .send(
                ProducerRecord(
                    juridiskVurderingTopic,
                    receivedSykmelding.sykmelding.id,
                    juridiskVurderingResult,
                ),
            )
            .get()
    }

    private fun resultToJuridiskVurdering(
        receivedSykmelding: ReceivedSykmelding,
        result: TreeOutput<out Enum<*>, RuleResult>,
        medJuridisk: MedJuridisk,
    ): JuridiskVurdering {
        return JuridiskVurdering(
            id = UUID.randomUUID().toString(),
            eventName = EVENT_NAME,
            version = VERSION,
            kilde = KILDE,
            versjonAvKode = versjonsKode,
            fodselsnummer = receivedSykmelding.personNrPasient,
            juridiskHenvisning = medJuridisk.juridiskHenvisning,
            sporing =
                mapOf(
                    "sykmelding" to receivedSykmelding.sykmelding.id,
                ),
            input = result.ruleInputs,
            utfall = toJuridiskUtfall(result.treeResult.status),
            tidsstempel = ZonedDateTime.now(ZoneOffset.UTC),
        )
    }

    private fun toJuridiskUtfall(status: Status) =
        when (status) {
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
