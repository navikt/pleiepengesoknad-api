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
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.features.*
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.jackson.jackson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.request.header
import io.ktor.request.path
import io.ktor.response.header
import io.ktor.routing.Routing
import io.ktor.util.KtorExperimentalAPI
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.hotspot.DefaultExports
import no.nav.helse.aktoer.AktoerGateway
import no.nav.helse.aktoer.AktoerService
import no.nav.helse.arbeidsgiver.ArbeidsgiverGateway
import no.nav.helse.arbeidsgiver.ArbeidsgiverService
import no.nav.helse.arbeidsgiver.arbeidsgiverApis
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
import no.nav.helse.monitorering.MONITORING_PATHS
import no.nav.helse.monitorering.monitoreringApis
import no.nav.helse.soker.SokerGateway
import no.nav.helse.soker.SokerService
import no.nav.helse.soker.sokerApis
import no.nav.helse.soknad.PleiepengesoknadProsesseringGateway
import no.nav.helse.soknad.SoknadService
import no.nav.helse.soknad.soknadApis
import no.nav.helse.systembruker.SystemBrukerTokenGateway
import no.nav.helse.systembruker.SystemBrukerTokenService
import no.nav.helse.vedlegg.*
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.net.ProxySelector
import java.util.*
import javax.validation.Validation
import javax.validation.Validator

private const val GENERATED_REQUEST_ID_PREFIX = "generated-"

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
        expectSuccess = false
        install(JsonFeature) {
            serializer = JacksonSerializer{
                configureObjectMapper(this)
            }
        }
        install(Logging) {
            level = LogLevel.BODY
            logger = Logger.NAV_HTTP_CLEINT_TRACE
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
        generate { UUID.randomUUID().toString() } // CorrelationID skal oppstå i API'et.
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> !MONITORING_PATHS.contains(call.request.path()) }
        callIdMdc("correlation_id")

        mdc("request_id") { call -> // Request ID kan sendes inn fra clienten
            val requestId = call.request.header(HttpHeaders.XRequestId)?.removePrefix(GENERATED_REQUEST_ID_PREFIX) ?: "$GENERATED_REQUEST_ID_PREFIX${UUID.randomUUID()}"
            call.response.header(HttpHeaders.XRequestId, requestId)
            requestId
        }
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
                clientId = configuration.getServiceAccountClientId(),
                clientSecret = configuration.getServiceAccountClientSecret(),
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

//        val soknadKafkaProducer = SoknadKafkaProducer(
//            bootstrapServers = configuration.getKafkaBootstrapServers(),
//            username = configuration.getKafkaUsername(),
//            password = configuration.getKafkaPassword(),
//            objectMapper = objectMapper
//        )

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
                systemBrukerTokenService
            ),
            pingUrls = listOf(
                configuration.getJwksUrl()
            ),
            apiGatewayPingUrls = listOf(
                configuration.getSparkelReadinessUrl(),
                configuration.getAktoerRegisterReadinessUrl(),
                configuration.getPleiepengesoknadProsesserinReadinessUrl()
            ),
            apiGatewayApiKey = apiGatewayApiKey,
            httpClient = pinghHttpClient
        )

        authenticate {

            sokerApis(
                sokerService = sokerService
            )

            barnApis()

            arbeidsgiverApis(
                service = ArbeidsgiverService(
                    gateway = ArbeidsgiverGateway(
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
                    pleiepengesoknadProsesseringGateway = PleiepengesoknadProsesseringGateway(
                        httpClient = httpClient,
                        systemBrukerTokenService = systemBrukerTokenService,
                        baseUrl = configuration.getPleiepengesoknadProsesseringBaseUrl(),
                        apiGatewayApiKey = apiGatewayApiKey
                    ),
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

private val Logger.Companion.NAV_HTTP_CLEINT_TRACE: Logger
    get() = object : Logger {
        private val delegate = LoggerFactory.getLogger("nav.HttpClient")
        override fun log(message: String) {
            delegate.trace(message)
        }
    }