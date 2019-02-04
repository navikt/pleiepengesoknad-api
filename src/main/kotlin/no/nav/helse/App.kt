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
import io.ktor.features.*
import io.ktor.http.HttpMethod
import io.ktor.jackson.jackson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.request.path
import io.ktor.routing.Routing
import io.ktor.util.KtorExperimentalAPI
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.hotspot.DefaultExports
import no.nav.helse.aktoer.AktoerGateway
import no.nav.helse.aktoer.AktoerService
import no.nav.helse.ansettelsesforhold.AnsettelsesforholdGateway
import no.nav.helse.ansettelsesforhold.AnsettelsesforholdService
import no.nav.helse.ansettelsesforhold.ansettelsesforholdApis
import no.nav.helse.barn.BarnService
import no.nav.helse.barn.barnApis
import no.nav.helse.general.auth.IdTokenProvider
import no.nav.helse.general.auth.InsufficientAuthenticationLevelException
import no.nav.helse.general.auth.authorizationStatusPages
import no.nav.helse.general.auth.jwtFromCookie
import no.nav.helse.general.error.defaultStatusPages
import no.nav.helse.general.error.initializeErrorCounter
import no.nav.helse.general.jackson.configureObjectMapper
import no.nav.helse.general.validation.ValidationHandler
import no.nav.helse.general.validation.validationStatusPages
import no.nav.helse.monitorering.monitoreringApis
import no.nav.helse.soker.SokerGateway
import no.nav.helse.soker.SokerService
import no.nav.helse.soker.sokerApis
import no.nav.helse.soknad.SoknadKafkaProducer
import no.nav.helse.soknad.SoknadService
import no.nav.helse.soknad.soknadApis
import no.nav.helse.systembruker.SystemBrukerTokenGateway
import no.nav.helse.systembruker.SystemBrukerTokenService
import no.nav.helse.vedlegg.*
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.net.ProxySelector
import java.util.*
import javax.validation.Validation
import javax.validation.Validator

private val logger: Logger = LoggerFactory.getLogger("nav.Application")

fun main(args: Array<String>): Unit  = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Application.pleiepengesoknadapi() {

    val collectorRegistry = CollectorRegistry.defaultRegistry
    DefaultExports.initialize()

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
        engine { customizeClient { setProxyRoutePlanner() } }

    }
    val pinghHttpClient= HttpClient(Apache) {
        engine {
            socketTimeout = 1_000  // Max time between TCP packets - default 10 seconds
            connectTimeout = 1_000 // Max time to establish an HTTP connection - default 10 seconds
            customizeClient { setProxyRoutePlanner() }
        }
        install(Logging) {
            level = LogLevel.BODY
        }
    }

    configuration.logIndirectlyUsedConfiguration()

    install(ContentNegotiation) {
        jackson {
            configureObjectMapper(this)
        }
    }

    install(CallId) {
        header("Nav-Call-Id")
        generate { UUID.randomUUID().toString() }
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
        callIdMdc("call_id")
    }

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Delete)
        allowCredentials = true
        log.info("Configuring CORS")
        configuration.getWhitelistedCorsAddreses().forEach {
            log.info("Adding host {} with scheme {}", it.host, it.scheme)
            host(host = it.authority, schemes = listOf(it.scheme))
        }
    }

    val idTokenProviderBuilder = IdTokenProvider(cookieName = configuration.getCookieName())

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
            withIdTokenProvider(idTokenProviderBuilder)
        }
    }

    val errorCounter = initializeErrorCounter()

    install(StatusPages) {
        defaultStatusPages(errorCounter)
        authorizationStatusPages(errorCounter)
        validationStatusPages(errorCounter)
        vedleggStatusPages(errorCounter)
    }

    install(Locations)

    install(Routing) {

        val apiGatewayApiKey = configuration.getApiGatewayApiKey()

        val systemBrukerTokenService = SystemBrukerTokenService(
            SystemBrukerTokenGateway(
                username = configuration.getServiceAccountUsername(),
                password = configuration.getServiceAccountPassword(),
                scopes = configuration.getServiceAccountScopes(),
                baseUrl = configuration.getAuthorizationServerTokenUrl(),
                httpClient = httpClient,
                apiGatewayApiKey = apiGatewayApiKey
            )
        )

        val aktoerService = AktoerService(
            aktoerGateway = AktoerGateway(
                httpClient = httpClient,
                baseUrl = configuration.getAktoerRegisterUrl(),
                systemBrukerTokenService = systemBrukerTokenService,
                apiGatewayApiKey = apiGatewayApiKey
            )
        )

        val soknadKafkaProducer = SoknadKafkaProducer(
            bootstrapServers = configuration.getKafkaBootstrapServers(),
            username = configuration.getKafkaUsername(),
            password = configuration.getKafkaPassword(),
            objectMapper = objectMapper
        )

        val vedleggService = VedleggService(
            vedleggStorage = InMemoryVedleggStorage()
        )

        val sokerService =  SokerService(
            sokerGateway = SokerGateway(
                httpClient = httpClient,
                baseUrl = configuration.getSparkelUrl(),
                aktoerService = aktoerService,
                apiGatewayApiKey = apiGatewayApiKey,
                systemBrukerTokenService = systemBrukerTokenService
            )
        )

        monitoreringApis(
            collectorRegistry = collectorRegistry,
            readiness = listOf(
                soknadKafkaProducer,
                systemBrukerTokenService
            ),
            pingUrls = listOf(
                configuration.getJwksUrl()
            ),
            apiGatewayPingUrls = listOf(
                configuration.getSparkelReadinessUrl(),
                configuration.getAktoerRegisterReadinessUrl()
            ),
            apiGatewayApiKey = apiGatewayApiKey,
            httpClient = pinghHttpClient
        )

        authenticate {

            sokerApis(
                sokerService = sokerService
            )

            barnApis(
                barnService = BarnService()
            )

            ansettelsesforholdApis(
                service = AnsettelsesforholdService(
                    gateway = AnsettelsesforholdGateway(
                        httpClient = httpClient,
                        aktoerService = aktoerService,
                        baseUrl = configuration.getSparkelUrl(),
                        systemBrukerTokenService = systemBrukerTokenService,
                        apiGatewayApiKey = apiGatewayApiKey
                    )
                )
            )

            vedleggApis(
                vedleggService = vedleggService
            )

            soknadApis(
                validationHandler = validationHandler,
                soknadService = SoknadService(
                    soknadKafkaProducer = soknadKafkaProducer,
                    sokerService = sokerService,
                    vedleggService = vedleggService
                )
            )
        }
    }
}

private fun HttpAsyncClientBuilder.setProxyRoutePlanner() {
    setRoutePlanner(SystemDefaultRoutePlanner(ProxySelector.getDefault()))
}