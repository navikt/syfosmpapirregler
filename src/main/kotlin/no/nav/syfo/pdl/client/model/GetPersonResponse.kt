package no.nav.syfo.pdl.client.model

data class PdlResponse(
    val hentPerson: HentPerson?
)

data class HentPerson(
    val foedsel: List<Foedsel>?
)

data class Foedsel(
    val foedselsdato: String?
)

data class GraphQLResponse<T> (
    val data: T,
    val errors: List<ResponseError>?
)

data class ResponseError(
    val message: String?,
    val locations: List<ErrorLocation>?,
    val path: List<String>?,
    val extensions: ErrorExtension?
)

data class ErrorLocation(
    val line: String?,
    val column: String?
)

data class ErrorExtension(
    val code: String?,
    val classification: String?
)
