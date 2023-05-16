[![Build status](https://github.com/navikt/syfosmpapirregler/workflows/Deploy%20to%20dev%20and%20prod/badge.svg)](https://github.com/navikt/syfosmpapirregler/workflows/Deploy%20to%20dev%20and%20prod/badge.svg)

# SYFO papirsykemelding regler
This project contains just the rules for validating whether a user is supposed to get paid sick leave

# Technologies used
* Kotlin
* Ktor
* Gradle
* Kotest
* Jackson

#### Requirements

* JDK 17

## Getting started
### Getting github-package-registry packages NAV-IT
Some packages used in this repo is uploaded to the GitHub Package Registry which requires authentication. It can, for example, be solved like this in Gradle:
```
val githubUser: String by project
val githubPassword: String by project
repositories {
    maven {
        credentials {
            username = githubUser
            password = githubPassword
        }
        setUrl("https://maven.pkg.github.com/navikt/syfosm-common")
    }
}
```

`githubUser` and `githubPassword` can be put into a separate file `~/.gradle/gradle.properties` with the following content:

```                                                     
githubUser=x-access-token
githubPassword=[token]
```

Replace `[token]` with a personal access token with scope `read:packages`.
See githubs guide [creating-a-personal-access-token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token) on
how to create a personal access token.

Alternatively, the variables can be configured via environment variables:

* `ORG_GRADLE_PROJECT_githubUser`
* `ORG_GRADLE_PROJECT_githubPassword`

or the command line:

``` bash
./gradlew -PgithubUser=x-access-token -PgithubPassword=$yourtoken
```

### Building the application
#### Compile and package application
To build locally and run the integration tests you can simply run
``` bash
./gradlew shadowJar
```
or on windows 
`gradlew.bat shadowJar`

#### Creating a docker image
Creating a docker image should be as simple as `docker build -t syfosmpapirregler .`

#### Running a docker image
``` bash
docker run --rm -it -p 8080:8080 syfosmpapirregler
```

### Upgrading the gradle wrapper
Find the newest version of gradle here: https://gradle.org/releases/ Then run this command:

``` bash
./gradlew wrapper --gradle-version $gradleVersjon
```

<!-- RULE_MARKER_START -->
Lege suspensjon
```mermaid
graph TD
    root(BEHANDLER_SUSPENDERT) -->|Yes| root_BEHANDLER_SUSPENDERT_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root(BEHANDLER_SUSPENDERT) -->|No| root_BEHANDLER_SUSPENDERT_OK(OK):::ok
    classDef ok fill:#c3ff91,stroke:#004a00,color: black;
    classDef invalid fill:#ff7373,stroke:#ff0000,color: black;
    classDef manuell fill:#ffe24f,stroke:#ffd500,color: #473c00;
```
HPR
```mermaid
graph TD
    root(BEHANDLER_IKKE_GYLDIG_I_HPR) -->|Yes| root_BEHANDLER_IKKE_GYLDIG_I_HPR_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root(BEHANDLER_IKKE_GYLDIG_I_HPR) -->|No| root_BEHANDLER_IKKE_GYLDIG_I_HPR_BEHANDLER_MANGLER_AUTORISASJON_I_HPR(BEHANDLER_MANGLER_AUTORISASJON_I_HPR)
    root_BEHANDLER_IKKE_GYLDIG_I_HPR_BEHANDLER_MANGLER_AUTORISASJON_I_HPR(BEHANDLER_MANGLER_AUTORISASJON_I_HPR) -->|Yes| root_BEHANDLER_IKKE_GYLDIG_I_HPR_BEHANDLER_MANGLER_AUTORISASJON_I_HPR_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_BEHANDLER_IKKE_GYLDIG_I_HPR_BEHANDLER_MANGLER_AUTORISASJON_I_HPR(BEHANDLER_MANGLER_AUTORISASJON_I_HPR) -->|No| root_BEHANDLER_IKKE_GYLDIG_I_HPR_BEHANDLER_MANGLER_AUTORISASJON_I_HPR_BEHANDLER_IKKE_LE_KI_MT_TL_FT_I_HPR(BEHANDLER_IKKE_LE_KI_MT_TL_FT_I_HPR)
    root_BEHANDLER_IKKE_GYLDIG_I_HPR_BEHANDLER_MANGLER_AUTORISASJON_I_HPR_BEHANDLER_IKKE_LE_KI_MT_TL_FT_I_HPR(BEHANDLER_IKKE_LE_KI_MT_TL_FT_I_HPR) -->|Yes| root_BEHANDLER_IKKE_GYLDIG_I_HPR_BEHANDLER_MANGLER_AUTORISASJON_I_HPR_BEHANDLER_IKKE_LE_KI_MT_TL_FT_I_HPR_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_BEHANDLER_IKKE_GYLDIG_I_HPR_BEHANDLER_MANGLER_AUTORISASJON_I_HPR_BEHANDLER_IKKE_LE_KI_MT_TL_FT_I_HPR(BEHANDLER_IKKE_LE_KI_MT_TL_FT_I_HPR) -->|No| root_BEHANDLER_IKKE_GYLDIG_I_HPR_BEHANDLER_MANGLER_AUTORISASJON_I_HPR_BEHANDLER_IKKE_LE_KI_MT_TL_FT_I_HPR_BEHANDLER_MT_FT_KI_OVER_12_UKER(BEHANDLER_MT_FT_KI_OVER_12_UKER)
    root_BEHANDLER_IKKE_GYLDIG_I_HPR_BEHANDLER_MANGLER_AUTORISASJON_I_HPR_BEHANDLER_IKKE_LE_KI_MT_TL_FT_I_HPR_BEHANDLER_MT_FT_KI_OVER_12_UKER(BEHANDLER_MT_FT_KI_OVER_12_UKER) -->|Yes| root_BEHANDLER_IKKE_GYLDIG_I_HPR_BEHANDLER_MANGLER_AUTORISASJON_I_HPR_BEHANDLER_IKKE_LE_KI_MT_TL_FT_I_HPR_BEHANDLER_MT_FT_KI_OVER_12_UKER_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_BEHANDLER_IKKE_GYLDIG_I_HPR_BEHANDLER_MANGLER_AUTORISASJON_I_HPR_BEHANDLER_IKKE_LE_KI_MT_TL_FT_I_HPR_BEHANDLER_MT_FT_KI_OVER_12_UKER(BEHANDLER_MT_FT_KI_OVER_12_UKER) -->|No| root_BEHANDLER_IKKE_GYLDIG_I_HPR_BEHANDLER_MANGLER_AUTORISASJON_I_HPR_BEHANDLER_IKKE_LE_KI_MT_TL_FT_I_HPR_BEHANDLER_MT_FT_KI_OVER_12_UKER_OK(OK):::ok
    classDef ok fill:#c3ff91,stroke:#004a00,color: black;
    classDef invalid fill:#ff7373,stroke:#ff0000,color: black;
    classDef manuell fill:#ffe24f,stroke:#ffd500,color: #473c00;
```
Validation
```mermaid
graph TD
    root(PASIENT_YNGRE_ENN_13) -->|Yes| root_PASIENT_YNGRE_ENN_13_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root(PASIENT_YNGRE_ENN_13) -->|No| root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70(PASIENT_ELDRE_ENN_70)
    root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70(PASIENT_ELDRE_ENN_70) -->|Yes| root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70(PASIENT_ELDRE_ENN_70) -->|No| root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE(UKJENT_DIAGNOSEKODETYPE)
    root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE(UKJENT_DIAGNOSEKODETYPE) -->|Yes| root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE(UKJENT_DIAGNOSEKODETYPE) -->|No| root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE_ICPC_2_Z_DIAGNOSE(ICPC_2_Z_DIAGNOSE)
    root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE_ICPC_2_Z_DIAGNOSE(ICPC_2_Z_DIAGNOSE) -->|Yes| root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE_ICPC_2_Z_DIAGNOSE_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE_ICPC_2_Z_DIAGNOSE(ICPC_2_Z_DIAGNOSE) -->|No| root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE_ICPC_2_Z_DIAGNOSE_HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER(HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER)
    root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE_ICPC_2_Z_DIAGNOSE_HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER(HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER) -->|Yes| root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE_ICPC_2_Z_DIAGNOSE_HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE_ICPC_2_Z_DIAGNOSE_HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER(HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER) -->|No| root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE_ICPC_2_Z_DIAGNOSE_HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE(UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE)
    root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE_ICPC_2_Z_DIAGNOSE_HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE(UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE) -->|Yes| root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE_ICPC_2_Z_DIAGNOSE_HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE_ICPC_2_Z_DIAGNOSE_HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE(UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE) -->|No| root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE_ICPC_2_Z_DIAGNOSE_HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE_UGYLDIG_KODEVERK_FOR_BIDIAGNOSE(UGYLDIG_KODEVERK_FOR_BIDIAGNOSE)
    root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE_ICPC_2_Z_DIAGNOSE_HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE_UGYLDIG_KODEVERK_FOR_BIDIAGNOSE(UGYLDIG_KODEVERK_FOR_BIDIAGNOSE) -->|Yes| root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE_ICPC_2_Z_DIAGNOSE_HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE_UGYLDIG_KODEVERK_FOR_BIDIAGNOSE_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE_ICPC_2_Z_DIAGNOSE_HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE_UGYLDIG_KODEVERK_FOR_BIDIAGNOSE(UGYLDIG_KODEVERK_FOR_BIDIAGNOSE) -->|No| root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE_ICPC_2_Z_DIAGNOSE_HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE_UGYLDIG_KODEVERK_FOR_BIDIAGNOSE_UGYLDIG_ORGNR_LENGDE(UGYLDIG_ORGNR_LENGDE)
    root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE_ICPC_2_Z_DIAGNOSE_HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE_UGYLDIG_KODEVERK_FOR_BIDIAGNOSE_UGYLDIG_ORGNR_LENGDE(UGYLDIG_ORGNR_LENGDE) -->|Yes| root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE_ICPC_2_Z_DIAGNOSE_HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE_UGYLDIG_KODEVERK_FOR_BIDIAGNOSE_UGYLDIG_ORGNR_LENGDE_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE_ICPC_2_Z_DIAGNOSE_HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE_UGYLDIG_KODEVERK_FOR_BIDIAGNOSE_UGYLDIG_ORGNR_LENGDE(UGYLDIG_ORGNR_LENGDE) -->|No| root_PASIENT_YNGRE_ENN_13_PASIENT_ELDRE_ENN_70_UKJENT_DIAGNOSEKODETYPE_ICPC_2_Z_DIAGNOSE_HOVEDDIAGNOSE_ELLER_FRAVAERSGRUNN_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE_UGYLDIG_KODEVERK_FOR_BIDIAGNOSE_UGYLDIG_ORGNR_LENGDE_OK(OK):::ok
    classDef ok fill:#c3ff91,stroke:#004a00,color: black;
    classDef invalid fill:#ff7373,stroke:#ff0000,color: black;
    classDef manuell fill:#ffe24f,stroke:#ffd500,color: #473c00;
```
Periode
```mermaid
graph TD
    root(PERIODER_MANGLER) -->|Yes| root_PERIODER_MANGLER_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root(PERIODER_MANGLER) -->|No| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO(FRADATO_ETTER_TILDATO)
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO(FRADATO_ETTER_TILDATO) -->|Yes| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO(FRADATO_ETTER_TILDATO) -->|No| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER(OVERLAPPENDE_PERIODER)
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER(OVERLAPPENDE_PERIODER) -->|Yes| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER(OVERLAPPENDE_PERIODER) -->|No| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER(OPPHOLD_MELLOM_PERIODER)
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER(OPPHOLD_MELLOM_PERIODER) -->|Yes| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER(OPPHOLD_MELLOM_PERIODER) -->|No| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR(TILBAKEDATERT_MER_ENN_3_AR)
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR(TILBAKEDATERT_MER_ENN_3_AR) -->|Yes| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR(TILBAKEDATERT_MER_ENN_3_AR) -->|No| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT(FREMDATERT)
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT(FREMDATERT) -->|Yes| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT(FREMDATERT) -->|No| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR(TOTAL_VARIGHET_OVER_ETT_AAR)
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR(TOTAL_VARIGHET_OVER_ETT_AAR) -->|Yes| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR(TOTAL_VARIGHET_OVER_ETT_AAR) -->|No| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR_AVVENTENDE_SYKMELDING_KOMBINERT(AVVENTENDE_SYKMELDING_KOMBINERT)
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR_AVVENTENDE_SYKMELDING_KOMBINERT(AVVENTENDE_SYKMELDING_KOMBINERT) -->|Yes| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR_AVVENTENDE_SYKMELDING_KOMBINERT_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR_AVVENTENDE_SYKMELDING_KOMBINERT(AVVENTENDE_SYKMELDING_KOMBINERT) -->|No| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER(MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER)
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER(MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER) -->|Yes| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER(MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER) -->|No| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER(AVVENTENDE_SYKMELDING_OVER_16_DAGER)
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER(AVVENTENDE_SYKMELDING_OVER_16_DAGER) -->|Yes| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER(AVVENTENDE_SYKMELDING_OVER_16_DAGER) -->|No| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE(FOR_MANGE_BEHANDLINGSDAGER_PER_UKE)
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE(FOR_MANGE_BEHANDLINGSDAGER_PER_UKE) -->|Yes| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE(FOR_MANGE_BEHANDLINGSDAGER_PER_UKE) -->|No| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE_GRADERT_SYKMELDING_OVER_99_PROSENT(GRADERT_SYKMELDING_OVER_99_PROSENT)
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE_GRADERT_SYKMELDING_OVER_99_PROSENT(GRADERT_SYKMELDING_OVER_99_PROSENT) -->|Yes| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE_GRADERT_SYKMELDING_OVER_99_PROSENT_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE_GRADERT_SYKMELDING_OVER_99_PROSENT(GRADERT_SYKMELDING_OVER_99_PROSENT) -->|No| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_TILBAKEDATERT_MER_ENN_3_AR_FREMDATERT_TOTAL_VARIGHET_OVER_ETT_AAR_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE_GRADERT_SYKMELDING_OVER_99_PROSENT_OK(OK):::ok
    classDef ok fill:#c3ff91,stroke:#004a00,color: black;
    classDef invalid fill:#ff7373,stroke:#ff0000,color: black;
    classDef manuell fill:#ffe24f,stroke:#ffd500,color: #473c00;
```
Tilbakedatering
```mermaid
graph TD
    root(TILBAKEDATERING) -->|Yes| root_TILBAKEDATERING_ETTERSENDING(ETTERSENDING)
    root_TILBAKEDATERING_ETTERSENDING(ETTERSENDING) -->|Yes| root_TILBAKEDATERING_ETTERSENDING_OK(OK):::ok
    root_TILBAKEDATERING_ETTERSENDING(ETTERSENDING) -->|No| root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER(TILBAKEDATERT_INNTIL_8_DAGER)
    root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER(TILBAKEDATERT_INNTIL_8_DAGER) -->|Yes| root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_BEGRUNNELSE_MIN_1_ORD(BEGRUNNELSE_MIN_1_ORD)
    root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_BEGRUNNELSE_MIN_1_ORD(BEGRUNNELSE_MIN_1_ORD) -->|Yes| root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_BEGRUNNELSE_MIN_1_ORD_OK(OK):::ok
    root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_BEGRUNNELSE_MIN_1_ORD(BEGRUNNELSE_MIN_1_ORD) -->|No| root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE(FORLENGELSE)
    root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE(FORLENGELSE) -->|Yes| root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE_OK(OK):::ok
    root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE(FORLENGELSE) -->|No| root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE_SPESIALISTHELSETJENESTEN(SPESIALISTHELSETJENESTEN)
    root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE_SPESIALISTHELSETJENESTEN(SPESIALISTHELSETJENESTEN) -->|Yes| root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE_SPESIALISTHELSETJENESTEN_OK(OK):::ok
    root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE_SPESIALISTHELSETJENESTEN(SPESIALISTHELSETJENESTEN) -->|No| root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE_SPESIALISTHELSETJENESTEN_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER(TILBAKEDATERT_INNTIL_8_DAGER) -->|No| root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER(TILBAKEDATERT_INNTIL_30_DAGER)
    root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER(TILBAKEDATERT_INNTIL_30_DAGER) -->|Yes| root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER_BEGRUNNELSE_MIN_1_ORD(BEGRUNNELSE_MIN_1_ORD)
    root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER_BEGRUNNELSE_MIN_1_ORD(BEGRUNNELSE_MIN_1_ORD) -->|Yes| root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE(FORLENGELSE)
    root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE(FORLENGELSE) -->|Yes| root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE_OK(OK):::ok
    root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE(FORLENGELSE) -->|No| root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE_ARBEIDSGIVERPERIODE(ARBEIDSGIVERPERIODE)
    root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE_ARBEIDSGIVERPERIODE(ARBEIDSGIVERPERIODE) -->|Yes| root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE_ARBEIDSGIVERPERIODE_OK(OK):::ok
    root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE_ARBEIDSGIVERPERIODE(ARBEIDSGIVERPERIODE) -->|No| root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE_ARBEIDSGIVERPERIODE_SPESIALISTHELSETJENESTEN(SPESIALISTHELSETJENESTEN)
    root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE_ARBEIDSGIVERPERIODE_SPESIALISTHELSETJENESTEN(SPESIALISTHELSETJENESTEN) -->|Yes| root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE_ARBEIDSGIVERPERIODE_SPESIALISTHELSETJENESTEN_OK(OK):::ok
    root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE_ARBEIDSGIVERPERIODE_SPESIALISTHELSETJENESTEN(SPESIALISTHELSETJENESTEN) -->|No| root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE_ARBEIDSGIVERPERIODE_SPESIALISTHELSETJENESTEN_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER_BEGRUNNELSE_MIN_1_ORD(BEGRUNNELSE_MIN_1_ORD) -->|No| root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER_BEGRUNNELSE_MIN_1_ORD_SPESIALISTHELSETJENESTEN(SPESIALISTHELSETJENESTEN)
    root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER_BEGRUNNELSE_MIN_1_ORD_SPESIALISTHELSETJENESTEN(SPESIALISTHELSETJENESTEN) -->|Yes| root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER_BEGRUNNELSE_MIN_1_ORD_SPESIALISTHELSETJENESTEN_OK(OK):::ok
    root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER_BEGRUNNELSE_MIN_1_ORD_SPESIALISTHELSETJENESTEN(SPESIALISTHELSETJENESTEN) -->|No| root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER_BEGRUNNELSE_MIN_1_ORD_SPESIALISTHELSETJENESTEN_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER(TILBAKEDATERT_INNTIL_30_DAGER) -->|No| root_TILBAKEDATERING_ETTERSENDING_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_INNTIL_30_DAGER_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root(TILBAKEDATERING) -->|No| root_TILBAKEDATERING_OK(OK):::ok
    classDef ok fill:#c3ff91,stroke:#004a00,color: black;
    classDef invalid fill:#ff7373,stroke:#ff0000,color: black;
    classDef manuell fill:#ffe24f,stroke:#ffd500,color: #473c00;
```
Gradert
```mermaid
graph TD
    root(GRADERT_UNDER_20_PROSENT) -->|Yes| root_GRADERT_UNDER_20_PROSENT_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root(GRADERT_UNDER_20_PROSENT) -->|No| root_GRADERT_UNDER_20_PROSENT_OK(OK):::ok
    classDef ok fill:#c3ff91,stroke:#004a00,color: black;
    classDef invalid fill:#ff7373,stroke:#ff0000,color: black;
    classDef manuell fill:#ffe24f,stroke:#ffd500,color: #473c00;
```

<!-- RULE_MARKER_END -->

### Contact

This project is maintained by [navikt/teamsykmelding](CODEOWNERS)

Questions and/or feature requests? Please create an [issue](https://github.com/navikt/syfosmpapirregler/issues)

If you work in [@navikt](https://github.com/navikt) you can reach us at the Slack
channel [#team-sykmelding](https://nav-it.slack.com/archives/CMA3XV997)