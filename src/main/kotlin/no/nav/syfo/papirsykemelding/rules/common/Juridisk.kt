package no.nav.syfo.papirsykemelding.rules.common

import no.nav.syfo.model.juridisk.JuridiskHenvisning

sealed interface Juridisk

data object UtenJuridisk : Juridisk

data class MedJuridisk(val juridiskHenvisning: JuridiskHenvisning) : Juridisk
