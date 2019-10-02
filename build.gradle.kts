import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val coroutinesVersion = "1.3.2"
val ktorVersion = "1.2.5"
val prometheusVersion = "0.5.0"
val spekVersion = "2.0.6"
val kluentVersion = "1.39"
val logbackVersion = "1.2.3"
val logstashEncoderVersion = "5.1"
val mockkVersion = "1.9.3"
val nimbusdsVersion = "7.5.1"
val smCommonVersion = "2019.09.25-05-44-08e26429f4e37cd57d99ba4d39fc74099a078b97"
val jacksonVersion = "2.9.8"
val jfairyVersion = "0.6.2"
val diskresjonskodeV1Version= "1.2019.07.11-06.47-b55f47790a9d"
val javaxJaxwsApiVersion = "2.2.1"
val jaxwsToolsVersion = "2.3.1"
val jaxbApiVersion = "2.4.0-b180830.0359"
val cxfVersion = "3.2.7"
val commonsTextVersion = "1.4"
val saajVersion = "1.4.0"
val javaxActivationVersion = "1.1.1"

plugins {
    java
    kotlin("jvm") version "1.3.50"
    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("org.jmailen.kotlinter") version "2.1.0"
    id("com.diffplug.gradle.spotless") version "3.24.0"
}

group = "no.nav.syfo"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/kotlin/ktor")
    maven(url = "https://dl.bintray.com/spekframework/spek-dev")
    maven(url = "https://kotlin.bintray.com/kotlinx")
    maven (url = "https://oss.sonatype.org/content/groups/staging/")


}

dependencies {
    implementation(kotlin("stdlib"))
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation ("io.ktor:ktor-server-netty:$ktorVersion")
    implementation ("io.ktor:ktor-jackson:$ktorVersion")
    implementation ("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    implementation ("io.prometheus:simpleclient_common:$prometheusVersion")
    implementation ("io.ktor:ktor-jackson:$ktorVersion")
    implementation ("io.ktor:ktor-auth:$ktorVersion")
    implementation ("io.ktor:ktor-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation ("org.apache.commons:commons-text:$commonsTextVersion")
    implementation ("org.apache.cxf:cxf-rt-frontend-jaxws:$cxfVersion")
    implementation ("org.apache.cxf:cxf-rt-features-logging:$cxfVersion")
    implementation ("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")
    implementation ("org.apache.cxf:cxf-rt-ws-security:$cxfVersion")
    implementation ("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation ("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation ("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("javax.xml.ws:jaxws-api:$javaxJaxwsApiVersion")
    implementation ("no.nav.syfo.sm:syfosm-common-models:$smCommonVersion")
    implementation ("no.nav.syfo.sm:syfosm-common-rules:$smCommonVersion")
    implementation ("no.nav.syfo.sm:syfosm-common-networking:$smCommonVersion")
    implementation ("no.nav.syfo.sm:syfosm-common-rest-sts:$smCommonVersion")
    implementation ("no.nav.syfo.sm:syfosm-common-diagnosis-codes:$smCommonVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")
    implementation("no.nav.syfo.sm:syfosm-common-ws:$smCommonVersion")
    implementation ("no.nav.tjenestespesifikasjoner:diskresjonskodev1-tjenestespesifikasjon:$diskresjonskodeV1Version")
    implementation("javax.activation:activation:$javaxActivationVersion")

    implementation("javax.xml.bind:jaxb-api:$jaxbApiVersion")
    implementation("com.sun.xml.ws:jaxws-tools:$jaxwsToolsVersion") {
        exclude(group = "com.sun.xml.ws", module = "policy")
    }
    implementation("com.sun.xml.messaging.saaj:saaj-impl:$saajVersion")

    testImplementation ("io.mockk:mockk:$mockkVersion")
    testImplementation ("com.nimbusds:nimbus-jose-jwt:$nimbusdsVersion")
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testImplementation ("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
    testImplementation("com.devskiller:jfairy:$jfairyVersion")
    testRuntimeOnly("org.spekframework.spek2:spek-runtime-jvm:$spekVersion") {
        exclude(group = "org.jetbrains.kotlin")
    }
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion") {
        exclude(group = "org.jetbrains.kotlin")
    }
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") {
        exclude(group = "org.eclipse.jetty")
    }
}

tasks {
    withType<Jar> {
        manifest.attributes["Main-Class"] = "no.nav.syfo.BootstrapKt"
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "12"
    }

    withType<ShadowJar> {
        transform(ServiceFileTransformer::class.java) {
            setPath("META-INF/cxf")
            include("bus-extensions.txt")
        }
    }

    withType<Test> {
        useJUnitPlatform {
            includeEngines("spek2")
        }
        testLogging {
            showStandardStreams = true
        }
    }
}