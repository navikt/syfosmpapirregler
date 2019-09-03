import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val coroutinesVersion = "1.2.2"
val ktorVersion = "1.2.3"
val prometheusVersion = "0.5.0"
val spekVersion = "2.0.6"
val kluentVersion = "1.39"
val logbackVersion = "1.2.3"
val logstashEncoderVersion = "5.1"
val mockkVersion = "1.9.3"
val nimbusdsVersion = "7.5.1"
plugins {
    java
    kotlin("jvm") version "1.3.41"
    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("org.jmailen.kotlinter") version "2.1.0"
    id("com.diffplug.gradle.spotless") version "3.24.0"
}

group = "no.nav.syfo"
version = "1.0.0-SNAPSHOT"

repositories {
    maven(url = "https://dl.bintray.com/kotlin/ktor")
    maven(url = "https://dl.bintray.com/spekframework/spek-dev")
    maven(url = "https://kotlin.bintray.com/kotlinx")
    mavenCentral()
    jcenter()
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

    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")

    testImplementation ("io.mockk:mockk:$mockkVersion")
    testImplementation ("com.nimbusds:nimbus-jose-jwt:$nimbusdsVersion")
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testImplementation ("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
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
        kotlinOptions.jvmTarget = "1.8"
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