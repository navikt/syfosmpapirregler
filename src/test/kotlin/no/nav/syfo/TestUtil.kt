package no.nav.syfo

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule()).registerModule(KotlinModule())

fun <T> getStringValue(content: T): String {
    return objectMapper.writeValueAsString(content)
}

class ResponseHandler(var responseData: ResponseData = ResponseData()) {
    fun updateResponseData(responseData: ResponseData) {
        this.responseData = responseData
    }
}

class ResponseData(val content: String = "", val statusCode: HttpStatusCode = HttpStatusCode.OK, val headers: Headers = headersOf("Content-Type", "application/json"))

fun getHttpClient(responseHandler: ResponseHandler): HttpClient {
    return HttpClient(MockEngine) {
        install(JsonFeature) {
            serializer = JacksonSerializer {
                registerKotlinModule()
                registerModule(JavaTimeModule())
            }
        }
        engine {
            addHandler {
                respond(responseHandler.responseData.content, status = responseHandler.responseData.statusCode, headers = responseHandler.responseData.headers)
            }
        }
    }
}
