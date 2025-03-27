import java.io.ByteArrayOutputStream
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = "no.nav.syfo"
version = "1.0.0"

val jvmVersion = JvmTarget.JVM_21

val coroutinesVersion="1.10.1"
val ktorVersion="3.1.2"
val prometheusVersion="0.16.0"
val kluentVersion="1.73"
val logbackVersion="1.5.18"
val logstashEncoderVersion= "8.0"
val mockkVersion="1.13.17"
val nimbusdsVersion="10.0.2"
val jacksonVersion="2.18.3"
val kotlinVersion="2.1.20"
val caffeineVersion="3.2.0"
val ktfmtVersion="0.44"
val kafkaVersion = "3.9.0"
val junitJupiterVersion = "5.12.1"
val diagnosekoderVersion = "1.2025.0"


///Due to vulnerabilities
val snappyJavaVersion = "1.1.10.7"
val commonsCodecVersion = "1.18.0"

application {
    mainClass.set("no.nav.syfo.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

plugins {
    id("application")
    kotlin("jvm") version "2.1.20"
    id("com.gradleup.shadow") version "8.3.6"
    id("com.diffplug.spotless") version "7.0.2"
}


repositories {
    mavenCentral()
    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    implementation("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    implementation("io.prometheus:simpleclient_common:$prometheusVersion")

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-call-id:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")

    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    constraints {
        implementation("commons-codec:commons-codec:$commonsCodecVersion") {
            because("override transient from io.ktor:ktor-client-apache")
        }
    }
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    implementation("org.apache.kafka:kafka_2.12:$kafkaVersion")
    constraints {
        implementation("org.xerial.snappy:snappy-java:$snappyJavaVersion") {
            because("override transient from org.apache.kafka:kafka_2.12")
        }
    }

    implementation("no.nav.helse:diagnosekoder:$diagnosekoderVersion")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")

    implementation("com.github.ben-manes.caffeine:caffeine:$caffeineVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("com.nimbusds:nimbus-jose-jwt:$nimbusdsVersion")
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
}

kotlin {
    compilerOptions {
        jvmTarget = jvmVersion
    }
}

tasks {
    shadowJar {
        archiveBaseName.set("app")
        archiveClassifier.set("")
        isZip64 = true
        manifest {
            attributes(
                mapOf(
                    "Main-Class" to "no.nav.syfo.ApplicationKt",
                ),
            )
        }
    }

    test {
        useJUnitPlatform {
        }
        testLogging {
            showStandardStreams = true
        }
    }

    register<JavaExec>("generateRuleMermaid") {
        val output = ByteArrayOutputStream()
        mainClass.set("no.nav.syfo.papirsykemelding.rules.common.GenerateMermaidKt")
        classpath = sourceSets["main"].runtimeClasspath
        group = "documentation"
        description = "Generates mermaid diagram source of rules"
        standardOutput = output
        doLast {
            val readme = File("README.md")
            val lines = readme.readLines()

            val starterTag = "<!-- RULE_MARKER_START -->"
            val endTag = "<!-- RULE_MARKER_END -->"

            val start = lines.indexOfFirst { it.contains(starterTag) }
            val end = lines.indexOfFirst { it.contains(endTag) }

            val newLines: List<String> =
                lines.subList(0, start) +
                        listOf(
                            starterTag,
                        ) +
                        output.toString().split("\n") +
                        listOf(
                            endTag,
                        ) +
                        lines.subList(end + 1, lines.size)
            readme.writeText(newLines.joinToString("\n"))
        }
    }

    spotless {
        kotlin { ktfmt(ktfmtVersion).kotlinlangStyle() }
        check {
            dependsOn("spotlessApply")
            dependsOn("generateRuleMermaid")
        }
    }
}
