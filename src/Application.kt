package no.nav.pleiepenger.api

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.features.*
import io.ktor.jackson.jackson
import io.ktor.locations.*

import no.nav.pleiepenger.api.ansettelsesforhold.ansettelsesforholdApis
import no.nav.pleiepenger.api.barn.barnApis
import no.nav.pleiepenger.api.general.auth.authorizationStatusPages
import no.nav.pleiepenger.api.general.error.defaultStatusPages
import no.nav.pleiepenger.api.general.jackson.configureObjectMapper
import no.nav.pleiepenger.api.general.validation.ValidationHandler
import no.nav.pleiepenger.api.general.validation.validationStatusPages
import no.nav.pleiepenger.api.id.IdGateway
import no.nav.pleiepenger.api.id.idApis
import no.nav.pleiepenger.api.soker.sokerApis
import no.nav.pleiepenger.api.soknad.soknadApis

import org.slf4j.event.*
import java.net.URI
import javax.validation.Validation
import javax.validation.Validator

fun main(args: Array<String>): Unit  = io.ktor.server.netty.EngineMain.main(args)

@kotlin.jvm.JvmOverloads
fun Application.pleiepengesoknadapi(
    client: HttpClient = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = JacksonSerializer()
        }
        install(Logging) {
            level = LogLevel.HEADERS
        }
    }
) {

    log.info("Client Engine type is '{}'", client.engineConfig.javaClass)

    val objectMapper = configureObjectMapper()
    val validator : Validator = Validation.buildDefaultValidatorFactory().validator
    val validationHandler = ValidationHandler(validator, objectMapper)


    install(ContentNegotiation) {
        jackson {
            configureObjectMapper(this)
        }
    }

    install(Locations) {
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    val corsAddresses = environment.config.property("nav.cors.addresses").getList()

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        allowCredentials = true
        log.info("Configuring CORS")
        corsAddresses.forEach {
            val uri = URI.create(it)
            log.info("Adding host {} with scheme {}", uri.host, uri.scheme)
            host(host = uri.host, schemes = listOf(uri.scheme))
        }
    }


    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }

    install(StatusPages) {
        defaultStatusPages()
        authorizationStatusPages()
        validationStatusPages()
    }

    install(Routing) {
        idApis(
            validationHandler = validationHandler,
            idGateway = IdGateway(
                httpClient = client,
                baseUri = Url(environment.config.property("nav.gateways.idGateway.baseUrl").getString())
            )
        )
        barnApis(
            httpClient = client
        )
        sokerApis(
            httpClient = client
        )
        ansettelsesforholdApis()

        soknadApis(
            validationHandler = validationHandler
        )
    }
}