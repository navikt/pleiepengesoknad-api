package no.nav.helse

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.jackson.jackson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.request.path
import io.ktor.routing.Routing
import io.ktor.util.KtorExperimentalAPI
import no.nav.helse.ansettelsesforhold.ansettelsesforholdApis
import no.nav.helse.barn.BarnGateway
import no.nav.helse.barn.BarnService
import no.nav.helse.barn.barnApis
import no.nav.helse.general.auth.InsufficientAuthenticationLevelException
import no.nav.helse.general.auth.authorizationStatusPages
import no.nav.helse.general.auth.jwtFromCookie
import no.nav.helse.general.error.defaultStatusPages
import no.nav.helse.general.jackson.configureObjectMapper
import no.nav.helse.general.validation.ValidationHandler
import no.nav.helse.general.validation.validationStatusPages
import no.nav.helse.id.IdGateway
import no.nav.helse.id.IdService
import no.nav.helse.soker.SokerService
import no.nav.helse.soker.sokerApis
import no.nav.helse.soknad.SoknadKafkaProducer
import no.nav.helse.soknad.SoknadService
import no.nav.helse.soknad.soknadApis
import no.nav.helse.vedlegg.Image2PDFConverter
import no.nav.helse.vedlegg.ImageScaler
import no.nav.helse.vedlegg.vedleggStatusPages
import org.slf4j.event.Level
import javax.validation.Validation
import javax.validation.Validator


fun main(args: Array<String>): Unit  = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Application.pleiepengesoknadapi() {

    val configuration = Configuration(environment.config)
    val objectMapper = configureObjectMapper()
    val validator : Validator = Validation.buildDefaultValidatorFactory().validator
    val validationHandler = ValidationHandler(validator, objectMapper)
    val httpClient= HttpClient(Apache) {
        install(JsonFeature) {
            serializer = JacksonSerializer{
                configureObjectMapper(this)
            }
        }
        install(Logging) {
            level = LogLevel.HEADERS
        }
    }


    install(ContentNegotiation) {
        jackson {
            configureObjectMapper(this)
        }
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        header(HttpHeaders.Authorization)
        allowCredentials = true
        log.info("Configuring CORS")
        configuration.getWhitelistedCorsAddreses().forEach {
            log.info("Adding host {} with scheme {}", it.host, it.scheme)
            host(host = it.host, schemes = listOf(it.scheme))
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
                val acr = credentials.payload.getClaim("acr").asString()
                if ("Level4" != acr) {
                    throw InsufficientAuthenticationLevelException(acr)
                }
                return@validate JWTPrincipal(credentials.payload)
            }
            withCookieName(configuration.getCookieName())
        }
    }

    install(StatusPages) {
        defaultStatusPages()
        authorizationStatusPages()
        validationStatusPages()
        vedleggStatusPages()
    }

    install(Locations)

    install(Routing) {
        val idService = IdService(
            IdGateway(
                httpClient = httpClient,
                baseUri = configuration.getSparkelUrl()
            )
        )

        val barnService = BarnService(
            barnGateway = BarnGateway(
                httpClient = httpClient,
                baseUrl = configuration.getSparkelUrl(),
                idService = idService
            )
        )

        authenticate {
            barnApis(
                barnService = barnService
            )

            soknadApis(
                validationHandler = validationHandler,
                soknadService = SoknadService(
                    soknadKafkaProducer = SoknadKafkaProducer(
                        bootstrapServers = configuration.getKafkaBootstrapServers(),
                        username = configuration.getKafkaUsername(),
                        password = configuration.getKafkaPassword(),
                        objectMapper = objectMapper
                    ),
                    sokerService = SokerService(),
                    image2PDFConverter = Image2PDFConverter(
                        imageScaler = ImageScaler()
                    )
                )
            )
        }
    }
}
