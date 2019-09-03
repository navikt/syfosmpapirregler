package no.nav.syfo.service

class PapirsykemeldingRegelService() {
    fun validateSykemelding(sykemelding: String): String {
        return if (sykemelding.isNotEmpty()) "valid" else "Not valid"
    }
}
