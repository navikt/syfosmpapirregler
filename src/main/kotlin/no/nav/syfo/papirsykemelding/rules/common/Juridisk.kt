package no.nav.syfo.papirsykemelding.rules.common

import no.nav.syfo.model.juridisk.JuridiskHenvisning

sealed interface Juridisk

data object UtenJuridisk : Juridisk

class MedJuridisk(val juridiskHenvisning: JuridiskHenvisning) : Juridisk
