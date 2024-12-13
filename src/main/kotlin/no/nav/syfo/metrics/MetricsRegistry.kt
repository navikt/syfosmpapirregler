package no.nav.syfo.metrics

import io.prometheus.client.Counter
import io.prometheus.client.Histogram

const val NAMESPACE = "syfosmpapirregler"

val HTTP_HISTOGRAM: Histogram =
    Histogram.Builder()
        .labelNames("path")
        .name("requests_duration_seconds")
        .help("http requests durations for incomming requests in seconds")
        .register()

val RULE_NODE_RULE_HIT_COUNTER: Counter =
    Counter.Builder()
        .namespace(NAMESPACE)
        .name("rulenode_rule_hit_counter")
        .labelNames("status", "rule_hit")
        .help("Counts rulenode rules")
        .register()

val RULE_NODE_RULE_PATH_COUNTER: Counter =
    Counter.Builder()
        .namespace(NAMESPACE)
        .name("rulenode_rule_path_counter")
        .labelNames("path")
        .help("Counts rulenode rule paths")
        .register()

val FODSELSDATO_FRA_PDL_COUNTER: Counter =
    Counter.build()
        .namespace(NAMESPACE)
        .name("fodselsdato_fra_pdl_counter")
        .help("Antall fodselsdatoer hentet fra PDL")
        .register()

val FODSELSDATO_FRA_IDENT_COUNTER: Counter =
    Counter.build()
        .namespace(NAMESPACE)
        .name("fodselsdato_fra_ident_counter")
        .help("Antall fodselsdatoer utledet fra fnr/dnr")
        .register()

val ARBEIDSGIVERPERIODE_PAPIR_RULE_COUNTER: Counter =
    Counter.Builder()
        .namespace(NAMESPACE)
        .labelNames("version", "status")
        .name("arbeidsgiverperiode_count")
        .help("Counts number of cases of arbeidsgiverperiode rule")
        .register()
