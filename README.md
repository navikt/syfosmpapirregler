[![Build status](https://github.com/navikt/syfosmpapirregler/workflows/Deploy%20to%20dev%20and%20prod/badge.svg)](https://github.com/navikt/syfosmpapirregler/workflows/Deploy%20to%20dev%20and%20prod/badge.svg)

# SYFO papirsykemelding regler
This project contains just the rules for validating whether a user is supposed to get paid sick leave

# Technologies used
* Kotlin
* Ktor
* Gradle
* Kotest
* Jackson


### Prerequisites
* JDK 21

Make sure you have the Java JDK 21 installed
You can check which version you have installed using this command:
``` bash
java -version
```

* Docker

Make sure you have docker installed
You can check which version you have installed using this command:
``` bash
docker --version
```

## Getting started
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
## 0. Lege suspensjon

---

```mermaid
graph TD
    root(BEHANDLER_SUSPENDERT) -->|Yes| root_BEHANDLER_SUSPENDERT_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root(BEHANDLER_SUSPENDERT) -->|No| root_BEHANDLER_SUSPENDERT_OK(OK):::ok
    classDef ok fill:#c3ff91,stroke:#004a00,color: black;
    classDef invalid fill:#ff7373,stroke:#ff0000,color: black;
    classDef manuell fill:#ffe24f,stroke:#ffd500,color: #473c00;
```


## 1. Validation

---

```mermaid
graph TD
    root(UGYLDIG_ORGNR_LENGDE) -->|Yes| root_UGYLDIG_ORGNR_LENGDE_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root(UGYLDIG_ORGNR_LENGDE) -->|No| root_UGYLDIG_ORGNR_LENGDE_OK(OK):::ok
    classDef ok fill:#c3ff91,stroke:#004a00,color: black;
    classDef invalid fill:#ff7373,stroke:#ff0000,color: black;
    classDef manuell fill:#ffe24f,stroke:#ffd500,color: #473c00;
```


## 2. Periode validering

---

```mermaid
graph TD
    root(PERIODER_MANGLER) -->|Yes| root_PERIODER_MANGLER_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root(PERIODER_MANGLER) -->|No| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO(FRADATO_ETTER_TILDATO)
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO(FRADATO_ETTER_TILDATO) -->|Yes| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO(FRADATO_ETTER_TILDATO) -->|No| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER(OVERLAPPENDE_PERIODER)
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER(OVERLAPPENDE_PERIODER) -->|Yes| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER(OVERLAPPENDE_PERIODER) -->|No| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER(OPPHOLD_MELLOM_PERIODER)
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER(OPPHOLD_MELLOM_PERIODER) -->|Yes| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER(OPPHOLD_MELLOM_PERIODER) -->|No| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE(IKKE_DEFINERT_PERIODE)
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE(IKKE_DEFINERT_PERIODE) -->|Yes| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE(IKKE_DEFINERT_PERIODE) -->|No| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT(AVVENTENDE_SYKMELDING_KOMBINERT)
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT(AVVENTENDE_SYKMELDING_KOMBINERT) -->|Yes| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT(AVVENTENDE_SYKMELDING_KOMBINERT) -->|No| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER(MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER)
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER(MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER) -->|Yes| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER(MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER) -->|No| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER(AVVENTENDE_SYKMELDING_OVER_16_DAGER)
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER(AVVENTENDE_SYKMELDING_OVER_16_DAGER) -->|Yes| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER(AVVENTENDE_SYKMELDING_OVER_16_DAGER) -->|No| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE(FOR_MANGE_BEHANDLINGSDAGER_PER_UKE)
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE(FOR_MANGE_BEHANDLINGSDAGER_PER_UKE) -->|Yes| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE(FOR_MANGE_BEHANDLINGSDAGER_PER_UKE) -->|No| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE_GRADERT_SYKMELDING_OVER_99_PROSENT(GRADERT_SYKMELDING_OVER_99_PROSENT)
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE_GRADERT_SYKMELDING_OVER_99_PROSENT(GRADERT_SYKMELDING_OVER_99_PROSENT) -->|Yes| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE_GRADERT_SYKMELDING_OVER_99_PROSENT_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE_GRADERT_SYKMELDING_OVER_99_PROSENT(GRADERT_SYKMELDING_OVER_99_PROSENT) -->|No| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE_GRADERT_SYKMELDING_OVER_99_PROSENT_GRADERT_SYKMELDING_0_PROSENT(GRADERT_SYKMELDING_0_PROSENT)
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE_GRADERT_SYKMELDING_OVER_99_PROSENT_GRADERT_SYKMELDING_0_PROSENT(GRADERT_SYKMELDING_0_PROSENT) -->|Yes| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE_GRADERT_SYKMELDING_OVER_99_PROSENT_GRADERT_SYKMELDING_0_PROSENT_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE_GRADERT_SYKMELDING_OVER_99_PROSENT_GRADERT_SYKMELDING_0_PROSENT(GRADERT_SYKMELDING_0_PROSENT) -->|No| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE_GRADERT_SYKMELDING_OVER_99_PROSENT_GRADERT_SYKMELDING_0_PROSENT_SYKMELDING_MED_BEHANDLINGSDAGER(SYKMELDING_MED_BEHANDLINGSDAGER)
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE_GRADERT_SYKMELDING_OVER_99_PROSENT_GRADERT_SYKMELDING_0_PROSENT_SYKMELDING_MED_BEHANDLINGSDAGER(SYKMELDING_MED_BEHANDLINGSDAGER) -->|Yes| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE_GRADERT_SYKMELDING_OVER_99_PROSENT_GRADERT_SYKMELDING_0_PROSENT_SYKMELDING_MED_BEHANDLINGSDAGER_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE_GRADERT_SYKMELDING_OVER_99_PROSENT_GRADERT_SYKMELDING_0_PROSENT_SYKMELDING_MED_BEHANDLINGSDAGER(SYKMELDING_MED_BEHANDLINGSDAGER) -->|No| root_PERIODER_MANGLER_FRADATO_ETTER_TILDATO_OVERLAPPENDE_PERIODER_OPPHOLD_MELLOM_PERIODER_IKKE_DEFINERT_PERIODE_AVVENTENDE_SYKMELDING_KOMBINERT_MANGLENDE_INNSPILL_TIL_ARBEIDSGIVER_AVVENTENDE_SYKMELDING_OVER_16_DAGER_FOR_MANGE_BEHANDLINGSDAGER_PER_UKE_GRADERT_SYKMELDING_OVER_99_PROSENT_GRADERT_SYKMELDING_0_PROSENT_SYKMELDING_MED_BEHANDLINGSDAGER_OK(OK):::ok
    classDef ok fill:#c3ff91,stroke:#004a00,color: black;
    classDef invalid fill:#ff7373,stroke:#ff0000,color: black;
    classDef manuell fill:#ffe24f,stroke:#ffd500,color: #473c00;
```


## 3. HPR

---

- ### Juridisk Henvisning:
  - **Lovverk**: FOLKETRYGDLOVEN
  - **Paragraf**: 8-7
```mermaid
graph TD
    root(BEHANDLER_GYLIDG_I_HPR) -->|Yes| root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR(BEHANDLER_HAR_AUTORISASJON_I_HPR)
    root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR(BEHANDLER_HAR_AUTORISASJON_I_HPR) -->|Yes| root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR(BEHANDLER_ER_LEGE_I_HPR)
    root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR(BEHANDLER_ER_LEGE_I_HPR) -->|Yes| root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_OK(OK
1\. ledd):::ok
    root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR(BEHANDLER_ER_LEGE_I_HPR) -->|No| root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR(BEHANDLER_ER_TANNLEGE_I_HPR)
    root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR(BEHANDLER_ER_TANNLEGE_I_HPR) -->|Yes| root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_OK(OK
1\. ledd):::ok
    root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR(BEHANDLER_ER_TANNLEGE_I_HPR) -->|No| root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR(BEHANDLER_ER_MANUELLTERAPEUT_I_HPR)
    root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR(BEHANDLER_ER_MANUELLTERAPEUT_I_HPR) -->|Yes| root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR_SYKEFRAVAR_OVER_12_UKER(SYKEFRAVAR_OVER_12_UKER)
    root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR_SYKEFRAVAR_OVER_12_UKER(SYKEFRAVAR_OVER_12_UKER) -->|Yes| root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR_SYKEFRAVAR_OVER_12_UKER_MANUAL_PROCESSING(MANUAL_PROCESSING
1\. ledd):::manuell
    root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR_SYKEFRAVAR_OVER_12_UKER(SYKEFRAVAR_OVER_12_UKER) -->|No| root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR_SYKEFRAVAR_OVER_12_UKER_OK(OK
1\. ledd):::ok
    root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR(BEHANDLER_ER_MANUELLTERAPEUT_I_HPR) -->|No| root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR_BEHANDLER_ER_FT_MED_TILLEGSKOMPETANSE_I_HPR(BEHANDLER_ER_FT_MED_TILLEGSKOMPETANSE_I_HPR)
    root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR_BEHANDLER_ER_FT_MED_TILLEGSKOMPETANSE_I_HPR(BEHANDLER_ER_FT_MED_TILLEGSKOMPETANSE_I_HPR) -->|Yes| root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR_BEHANDLER_ER_FT_MED_TILLEGSKOMPETANSE_I_HPR_SYKEFRAVAR_OVER_12_UKER(SYKEFRAVAR_OVER_12_UKER)
    root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR_BEHANDLER_ER_FT_MED_TILLEGSKOMPETANSE_I_HPR_SYKEFRAVAR_OVER_12_UKER(SYKEFRAVAR_OVER_12_UKER) -->|Yes| root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR_BEHANDLER_ER_FT_MED_TILLEGSKOMPETANSE_I_HPR_SYKEFRAVAR_OVER_12_UKER_MANUAL_PROCESSING(MANUAL_PROCESSING
1\. ledd):::manuell
    root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR_BEHANDLER_ER_FT_MED_TILLEGSKOMPETANSE_I_HPR_SYKEFRAVAR_OVER_12_UKER(SYKEFRAVAR_OVER_12_UKER) -->|No| root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR_BEHANDLER_ER_FT_MED_TILLEGSKOMPETANSE_I_HPR_SYKEFRAVAR_OVER_12_UKER_OK(OK
1\. ledd):::ok
    root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR_BEHANDLER_ER_FT_MED_TILLEGSKOMPETANSE_I_HPR(BEHANDLER_ER_FT_MED_TILLEGSKOMPETANSE_I_HPR) -->|No| root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR_BEHANDLER_ER_FT_MED_TILLEGSKOMPETANSE_I_HPR_BEHANDLER_ER_KI_MED_TILLEGSKOMPETANSE_I_HPR(BEHANDLER_ER_KI_MED_TILLEGSKOMPETANSE_I_HPR)
    root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR_BEHANDLER_ER_FT_MED_TILLEGSKOMPETANSE_I_HPR_BEHANDLER_ER_KI_MED_TILLEGSKOMPETANSE_I_HPR(BEHANDLER_ER_KI_MED_TILLEGSKOMPETANSE_I_HPR) -->|Yes| root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR_BEHANDLER_ER_FT_MED_TILLEGSKOMPETANSE_I_HPR_BEHANDLER_ER_KI_MED_TILLEGSKOMPETANSE_I_HPR_SYKEFRAVAR_OVER_12_UKER(SYKEFRAVAR_OVER_12_UKER)
    root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR_BEHANDLER_ER_FT_MED_TILLEGSKOMPETANSE_I_HPR_BEHANDLER_ER_KI_MED_TILLEGSKOMPETANSE_I_HPR_SYKEFRAVAR_OVER_12_UKER(SYKEFRAVAR_OVER_12_UKER) -->|Yes| root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR_BEHANDLER_ER_FT_MED_TILLEGSKOMPETANSE_I_HPR_BEHANDLER_ER_KI_MED_TILLEGSKOMPETANSE_I_HPR_SYKEFRAVAR_OVER_12_UKER_MANUAL_PROCESSING(MANUAL_PROCESSING
1\. ledd):::manuell
    root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR_BEHANDLER_ER_FT_MED_TILLEGSKOMPETANSE_I_HPR_BEHANDLER_ER_KI_MED_TILLEGSKOMPETANSE_I_HPR_SYKEFRAVAR_OVER_12_UKER(SYKEFRAVAR_OVER_12_UKER) -->|No| root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR_BEHANDLER_ER_FT_MED_TILLEGSKOMPETANSE_I_HPR_BEHANDLER_ER_KI_MED_TILLEGSKOMPETANSE_I_HPR_SYKEFRAVAR_OVER_12_UKER_OK(OK
1\. ledd):::ok
    root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR_BEHANDLER_ER_FT_MED_TILLEGSKOMPETANSE_I_HPR_BEHANDLER_ER_KI_MED_TILLEGSKOMPETANSE_I_HPR(BEHANDLER_ER_KI_MED_TILLEGSKOMPETANSE_I_HPR) -->|No| root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_BEHANDLER_ER_LEGE_I_HPR_BEHANDLER_ER_TANNLEGE_I_HPR_BEHANDLER_ER_MANUELLTERAPEUT_I_HPR_BEHANDLER_ER_FT_MED_TILLEGSKOMPETANSE_I_HPR_BEHANDLER_ER_KI_MED_TILLEGSKOMPETANSE_I_HPR_MANUAL_PROCESSING(MANUAL_PROCESSING
1\. ledd):::manuell
    root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR(BEHANDLER_HAR_AUTORISASJON_I_HPR) -->|No| root_BEHANDLER_GYLIDG_I_HPR_BEHANDLER_HAR_AUTORISASJON_I_HPR_MANUAL_PROCESSING(MANUAL_PROCESSING
1\. ledd):::manuell
    root(BEHANDLER_GYLIDG_I_HPR) -->|No| root_BEHANDLER_GYLIDG_I_HPR_MANUAL_PROCESSING(MANUAL_PROCESSING
1\. ledd):::manuell
    classDef ok fill:#c3ff91,stroke:#004a00,color: black;
    classDef invalid fill:#ff7373,stroke:#ff0000,color: black;
    classDef manuell fill:#ffe24f,stroke:#ffd500,color: #473c00;
```


## 4. Arbeidsuforhet

---

- ### Juridisk Henvisning:
  - **Lovverk**: FOLKETRYGDLOVEN
  - **Paragraf**: 8-4
```mermaid
graph TD
    root(HOVEDDIAGNOSE_MANGLER) -->|Yes| root_HOVEDDIAGNOSE_MANGLER_FRAVAERSGRUNN_MANGLER(FRAVAERSGRUNN_MANGLER)
    root_HOVEDDIAGNOSE_MANGLER_FRAVAERSGRUNN_MANGLER(FRAVAERSGRUNN_MANGLER) -->|Yes| root_HOVEDDIAGNOSE_MANGLER_FRAVAERSGRUNN_MANGLER_MANUAL_PROCESSING(MANUAL_PROCESSING
1\. ledd):::manuell
    root_HOVEDDIAGNOSE_MANGLER_FRAVAERSGRUNN_MANGLER(FRAVAERSGRUNN_MANGLER) -->|No| root_HOVEDDIAGNOSE_MANGLER_FRAVAERSGRUNN_MANGLER_UGYLDIG_KODEVERK_FOR_BIDIAGNOSE(UGYLDIG_KODEVERK_FOR_BIDIAGNOSE)
    root_HOVEDDIAGNOSE_MANGLER_FRAVAERSGRUNN_MANGLER_UGYLDIG_KODEVERK_FOR_BIDIAGNOSE(UGYLDIG_KODEVERK_FOR_BIDIAGNOSE) -->|Yes| root_HOVEDDIAGNOSE_MANGLER_FRAVAERSGRUNN_MANGLER_UGYLDIG_KODEVERK_FOR_BIDIAGNOSE_MANUAL_PROCESSING(MANUAL_PROCESSING
1\. ledd):::manuell
    root_HOVEDDIAGNOSE_MANGLER_FRAVAERSGRUNN_MANGLER_UGYLDIG_KODEVERK_FOR_BIDIAGNOSE(UGYLDIG_KODEVERK_FOR_BIDIAGNOSE) -->|No| root_HOVEDDIAGNOSE_MANGLER_FRAVAERSGRUNN_MANGLER_UGYLDIG_KODEVERK_FOR_BIDIAGNOSE_OK(OK
1\. ledd):::ok
    root(HOVEDDIAGNOSE_MANGLER) -->|No| root_HOVEDDIAGNOSE_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE(UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE)
    root_HOVEDDIAGNOSE_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE(UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE) -->|Yes| root_HOVEDDIAGNOSE_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE_MANUAL_PROCESSING(MANUAL_PROCESSING
1\. ledd):::manuell
    root_HOVEDDIAGNOSE_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE(UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE) -->|No| root_HOVEDDIAGNOSE_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE_ICPC_2_Z_DIAGNOSE(ICPC_2_Z_DIAGNOSE)
    root_HOVEDDIAGNOSE_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE_ICPC_2_Z_DIAGNOSE(ICPC_2_Z_DIAGNOSE) -->|Yes| root_HOVEDDIAGNOSE_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE_ICPC_2_Z_DIAGNOSE_MANUAL_PROCESSING(MANUAL_PROCESSING
1\. ledd):::manuell
    root_HOVEDDIAGNOSE_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE_ICPC_2_Z_DIAGNOSE(ICPC_2_Z_DIAGNOSE) -->|No| root_HOVEDDIAGNOSE_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE_ICPC_2_Z_DIAGNOSE_UGYLDIG_KODEVERK_FOR_BIDIAGNOSE(UGYLDIG_KODEVERK_FOR_BIDIAGNOSE)
    root_HOVEDDIAGNOSE_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE_ICPC_2_Z_DIAGNOSE_UGYLDIG_KODEVERK_FOR_BIDIAGNOSE(UGYLDIG_KODEVERK_FOR_BIDIAGNOSE) -->|Yes| root_HOVEDDIAGNOSE_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE_ICPC_2_Z_DIAGNOSE_UGYLDIG_KODEVERK_FOR_BIDIAGNOSE_MANUAL_PROCESSING(MANUAL_PROCESSING
1\. ledd):::manuell
    root_HOVEDDIAGNOSE_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE_ICPC_2_Z_DIAGNOSE_UGYLDIG_KODEVERK_FOR_BIDIAGNOSE(UGYLDIG_KODEVERK_FOR_BIDIAGNOSE) -->|No| root_HOVEDDIAGNOSE_MANGLER_UGYLDIG_KODEVERK_FOR_HOVEDDIAGNOSE_ICPC_2_Z_DIAGNOSE_UGYLDIG_KODEVERK_FOR_BIDIAGNOSE_OK(OK
1\. ledd):::ok
    classDef ok fill:#c3ff91,stroke:#004a00,color: black;
    classDef invalid fill:#ff7373,stroke:#ff0000,color: black;
    classDef manuell fill:#ffe24f,stroke:#ffd500,color: #473c00;
```


## 5. Pasient under 13

---

- ### Juridisk Henvisning:
  - **Lovverk**: FOLKETRYGDLOVEN
  - **Paragraf**: 8-3
```mermaid
graph TD
    root(PASIENT_YNGRE_ENN_13) -->|Yes| root_PASIENT_YNGRE_ENN_13_MANUAL_PROCESSING(MANUAL_PROCESSING
1\. ledd):::manuell
    root(PASIENT_YNGRE_ENN_13) -->|No| root_PASIENT_YNGRE_ENN_13_OK(OK
1\. ledd):::ok
    classDef ok fill:#c3ff91,stroke:#004a00,color: black;
    classDef invalid fill:#ff7373,stroke:#ff0000,color: black;
    classDef manuell fill:#ffe24f,stroke:#ffd500,color: #473c00;
```


## 6. Periode

---

```mermaid
graph TD
    root(FREMDATERT) -->|Yes| root_FREMDATERT_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root(FREMDATERT) -->|No| root_FREMDATERT_TILBAKEDATERT_MER_ENN_3_AR(TILBAKEDATERT_MER_ENN_3_AR)
    root_FREMDATERT_TILBAKEDATERT_MER_ENN_3_AR(TILBAKEDATERT_MER_ENN_3_AR) -->|Yes| root_FREMDATERT_TILBAKEDATERT_MER_ENN_3_AR_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_FREMDATERT_TILBAKEDATERT_MER_ENN_3_AR(TILBAKEDATERT_MER_ENN_3_AR) -->|No| root_FREMDATERT_TILBAKEDATERT_MER_ENN_3_AR_TOTAL_VARIGHET_OVER_ETT_AAR(TOTAL_VARIGHET_OVER_ETT_AAR)
    root_FREMDATERT_TILBAKEDATERT_MER_ENN_3_AR_TOTAL_VARIGHET_OVER_ETT_AAR(TOTAL_VARIGHET_OVER_ETT_AAR) -->|Yes| root_FREMDATERT_TILBAKEDATERT_MER_ENN_3_AR_TOTAL_VARIGHET_OVER_ETT_AAR_MANUAL_PROCESSING(MANUAL_PROCESSING):::manuell
    root_FREMDATERT_TILBAKEDATERT_MER_ENN_3_AR_TOTAL_VARIGHET_OVER_ETT_AAR(TOTAL_VARIGHET_OVER_ETT_AAR) -->|No| root_FREMDATERT_TILBAKEDATERT_MER_ENN_3_AR_TOTAL_VARIGHET_OVER_ETT_AAR_OK(OK):::ok
    classDef ok fill:#c3ff91,stroke:#004a00,color: black;
    classDef invalid fill:#ff7373,stroke:#ff0000,color: black;
    classDef manuell fill:#ffe24f,stroke:#ffd500,color: #473c00;
```


## 7. Tilbakedatering

---

- ### Juridisk Henvisning:
  - **Lovverk**: FOLKETRYGDLOVEN
  - **Paragraf**: 8-7
```mermaid
graph TD
    root(TILBAKEDATERING) -->|Yes| root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN(SPESIALISTHELSETJENESTEN)
    root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN(SPESIALISTHELSETJENESTEN) -->|Yes| root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_OK(OK
1\. ledd 1. punktum):::ok
    root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN(SPESIALISTHELSETJENESTEN) -->|No| root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING(ETTERSENDING)
    root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING(ETTERSENDING) -->|Yes| root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_OK(OK
1\. ledd 1. punktum):::ok
    root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING(ETTERSENDING) -->|No| root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER(TILBAKEDATERT_INNTIL_4_DAGER)
    root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER(TILBAKEDATERT_INNTIL_4_DAGER) -->|Yes| root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_OK(OK
2\. ledd 2. punktum):::ok
    root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER(TILBAKEDATERT_INNTIL_4_DAGER) -->|No| root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER(TILBAKEDATERT_INNTIL_8_DAGER)
    root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER(TILBAKEDATERT_INNTIL_8_DAGER) -->|Yes| root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_BEGRUNNELSE_MIN_1_ORD(BEGRUNNELSE_MIN_1_ORD)
    root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_BEGRUNNELSE_MIN_1_ORD(BEGRUNNELSE_MIN_1_ORD) -->|Yes| root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_BEGRUNNELSE_MIN_1_ORD_OK(OK
2\. ledd 2. punktum):::ok
    root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_BEGRUNNELSE_MIN_1_ORD(BEGRUNNELSE_MIN_1_ORD) -->|No| root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE(FORLENGELSE)
    root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE(FORLENGELSE) -->|Yes| root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE_OK(OK
1\. ledd 1. punktum):::ok
    root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE(FORLENGELSE) -->|No| root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE_MANUAL_PROCESSING(MANUAL_PROCESSING
1\. ledd 1. punktum):::manuell
    root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER(TILBAKEDATERT_INNTIL_8_DAGER) -->|No| root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_MINDRE_ENN_1_MAANED(TILBAKEDATERT_MINDRE_ENN_1_MAANED)
    root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_MINDRE_ENN_1_MAANED(TILBAKEDATERT_MINDRE_ENN_1_MAANED) -->|Yes| root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_MINDRE_ENN_1_MAANED_BEGRUNNELSE_MIN_1_ORD(BEGRUNNELSE_MIN_1_ORD)
    root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_MINDRE_ENN_1_MAANED_BEGRUNNELSE_MIN_1_ORD(BEGRUNNELSE_MIN_1_ORD) -->|Yes| root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_MINDRE_ENN_1_MAANED_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE(FORLENGELSE)
    root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_MINDRE_ENN_1_MAANED_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE(FORLENGELSE) -->|Yes| root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_MINDRE_ENN_1_MAANED_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE_OK(OK
1\. ledd 1. punktum):::ok
    root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_MINDRE_ENN_1_MAANED_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE(FORLENGELSE) -->|No| root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_MINDRE_ENN_1_MAANED_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE_ARBEIDSGIVERPERIODE(ARBEIDSGIVERPERIODE)
    root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_MINDRE_ENN_1_MAANED_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE_ARBEIDSGIVERPERIODE(ARBEIDSGIVERPERIODE) -->|Yes| root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_MINDRE_ENN_1_MAANED_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE_ARBEIDSGIVERPERIODE_OK(OK
2\. ledd 2. punktum):::ok
    root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_MINDRE_ENN_1_MAANED_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE_ARBEIDSGIVERPERIODE(ARBEIDSGIVERPERIODE) -->|No| root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_MINDRE_ENN_1_MAANED_BEGRUNNELSE_MIN_1_ORD_FORLENGELSE_ARBEIDSGIVERPERIODE_MANUAL_PROCESSING(MANUAL_PROCESSING
):::manuell
    root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_MINDRE_ENN_1_MAANED_BEGRUNNELSE_MIN_1_ORD(BEGRUNNELSE_MIN_1_ORD) -->|No| root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_MINDRE_ENN_1_MAANED_BEGRUNNELSE_MIN_1_ORD_MANUAL_PROCESSING(MANUAL_PROCESSING
1\. ledd 1. punktum):::manuell
    root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_MINDRE_ENN_1_MAANED(TILBAKEDATERT_MINDRE_ENN_1_MAANED) -->|No| root_TILBAKEDATERING_SPESIALISTHELSETJENESTEN_ETTERSENDING_TILBAKEDATERT_INNTIL_4_DAGER_TILBAKEDATERT_INNTIL_8_DAGER_TILBAKEDATERT_MINDRE_ENN_1_MAANED_MANUAL_PROCESSING(MANUAL_PROCESSING
1\. ledd 1. punktum):::manuell
    root(TILBAKEDATERING) -->|No| root_TILBAKEDATERING_OK(OK
1\. ledd 1. punktum):::ok
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