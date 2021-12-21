package no.nav.syfo.application.metrics

import io.prometheus.client.Counter
import io.prometheus.client.Histogram

const val NAMESPACE = "syfosmpapirregler"

val RULE_HIT_STATUS_COUNTER: Counter = Counter.Builder()
    .namespace(NAMESPACE)
    .name("rule_hit_status_counter")
    .labelNames("rule_status")
    .help("Registers a counter for each rule status")
    .register()

val HTTP_HISTOGRAM =
    Histogram
        .Builder()
        .labelNames("path")
        .name("requests_duration_seconds")
        .help("http requests durations for incomming requests in seconds")
        .register()
