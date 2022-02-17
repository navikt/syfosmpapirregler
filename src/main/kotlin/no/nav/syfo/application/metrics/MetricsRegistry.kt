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

val HTTP_HISTOGRAM: Histogram = Histogram
    .Builder()
    .labelNames("path")
    .name("requests_duration_seconds")
    .help("http requests durations for incomming requests in seconds")
    .register()

val RULE_HIT_COUNTER: Counter = Counter.Builder()
    .namespace("syfosm")
    .name("rule_hit_counter")
    .labelNames("rule_name")
    .help("Counts the amount of times a rule is hit").register()

val FODSELSDATO_FRA_PDL_COUNTER: Counter = Counter.build()
    .namespace(NAMESPACE)
    .name("fodselsdato_fra_pdl_counter")
    .help("Antall fodselsdatoer hentet fra PDL")
    .register()

val FODSELSDATO_FRA_IDENT_COUNTER: Counter = Counter.build()
    .namespace(NAMESPACE)
    .name("fodselsdato_fra_ident_counter")
    .help("Antall fodselsdatoer utledet fra fnr/dnr")
    .register()
