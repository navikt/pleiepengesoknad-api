package no.nav.pleiepenger.api

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.JWTPrincipal
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
import no.nav.pleiepenger.api.barn.BarnGateway
import no.nav.pleiepenger.api.barn.BarnService
import no.nav.pleiepenger.api.barn.barnApis
import no.nav.pleiepenger.api.general.auth.authorizationStatusPages
import no.nav.pleiepenger.api.general.auth.jwtFromCookie
import no.nav.pleiepenger.api.general.error.defaultStatusPages
import no.nav.pleiepenger.api.general.jackson.configureObjectMapper
import no.nav.pleiepenger.api.general.validation.ValidationHandler
import no.nav.pleiepenger.api.general.validation.validationStatusPages
import no.nav.pleiepenger.api.id.IdGateway
import no.nav.pleiepenger.api.id.IdService
import no.nav.pleiepenger.api.soker.sokerApis
import no.nav.pleiepenger.api.soknad.soknadApis

import org.slf4j.event.*
import javax.validation.Validation
import javax.validation.Validator

fun main(args: Array<String>): Unit  = io.ktor.server.netty.EngineMain.main(args)

@kotlin.jvm.JvmOverloads
fun Application.pleiepengesoknadapi(
    client: HttpClient = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = JacksonSerializer{
                configureObjectMapper(this)
            }
        }
        install(Logging) {
            level = LogLevel.HEADERS
        }
    }
) {

    val configuration = Configuration(environment.config)
    log.info("Client Engine type is '{}'", client.engineConfig.javaClass)

    val objectMapper = configureObjectMapper()
    val validator : Validator = Validation.buildDefaultValidatorFactory().validator
    val validationHandler = ValidationHandler(validator, objectMapper)


    install(ContentNegotiation) {
        jackson {
            configureObjectMapper(this)
        }
    }

    install(Authentication) {
        jwtFromCookie {
            val jwkProvider = JwkProviderBuilder(configuration.getJwksUrl())
                .cached(configuration.getJwkCacheSize(), configuration.getJwkCacheExpiryDuration(), configuration.getJwkCacheExpiryTimeUnit())
                .rateLimited(configuration.getJwsJwkRateLimitBucketSize(), configuration.getJwkRateLimitRefillRate(), configuration.getJwkRateLimitRefillTimeUnit())
                .build()
            verifier(jwkProvider, configuration.getIssuer())
            validate { credentials ->
                return@validate JWTPrincipal(credentials.payload)
            }
            withCookieName(configuration.getCookieName())
        }
    }

    install(Locations) {
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }


    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        allowCredentials = true
        log.info("Configuring CORS")
        configuration.getWhitelistedCorsAddreses().forEach {
            log.info("Adding host {} with scheme {}", it.host, it.scheme)
            host(host = it.host, schemes = listOf(it.scheme))
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
        val idService = IdService(
            IdGateway(
                httpClient = client,
                baseUri = configuration.getSparkelUrl()
            )
        )

        authenticate {
            barnApis(
                barnService = BarnService(
                    barnGateway = BarnGateway(
                        httpClient = client,
                        baseUrl = configuration.getSparkelUrl(),
                        idService = idService
                    )
                )
            )
        }

        sokerApis(
            httpClient = client
        )
        ansettelsesforholdApis()

        soknadApis(
            validationHandler = validationHandler
        )
    }
}