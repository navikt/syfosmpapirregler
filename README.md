# SYFO papirsykemelding regler
This project contains just the rules for validating whether or not a user is supposed to get paid sick leave

# Technologies used
* Kotlin
* Ktor
* Gradle
* Spek
* Jackson

#### Requirements

* JDK 11

## Getting started

### Building the application
#### Compile and package application
To build locally and run the integration tests you can simply run `./gradlew shadowJar` or on windows 
`gradlew.bat shadowJar`

#### Creating a docker image
Creating a docker image should be as simple as `docker build -t syfosmpapirregler .`

#### Running a docker image
`docker run --rm -it -p 8080:8080 syfosmpapirregler`

### For NAV employees
We are available at the Slack channel #barken
