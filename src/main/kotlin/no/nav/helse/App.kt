package no.nav.helse

import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.databind.DeserializationFeature
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
import io.ktor.client.features.logging.Logging
import io.ktor.features.*
import io.ktor.http.HttpMethod
import io.ktor.jackson.jackson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.routing.Routing
import io.ktor.util.KtorExperimentalAPI
import io.prometheus.client.hotspot.DefaultExports
import no.nav.helse.aktoer.AktoerGateway
import no.nav.helse.aktoer.AktoerService
import no.nav.helse.arbeidsgiver.ArbeidsgiverGateway
import no.nav.helse.arbeidsgiver.ArbeidsgiverService
import no.nav.helse.arbeidsgiver.arbeidsgiverApis
import no.nav.helse.barn.barnApis
import no.nav.helse.dusseldorf.ktor.client.*
import no.nav.helse.dusseldorf.ktor.core.*
import no.nav.helse.dusseldorf.ktor.health.HealthRoute
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.helse.dusseldorf.ktor.metrics.CallMonitoring
import no.nav.helse.dusseldorf.ktor.metrics.MetricsRoute
import no.nav.helse.general.auth.*
import no.nav.helse.general.error.defaultStatusPages
import no.nav.helse.general.error.initializeErrorCounter
import no.nav.helse.general.jackson.configureObjectMapper
import no.nav.helse.general.validation.ValidationHandler
import no.nav.helse.general.validation.validationStatusPages
import no.nav.helse.soker.SokerGateway
import no.nav.helse.soker.SokerService
import no.nav.helse.soker.sokerApis
import no.nav.helse.soknad.PleiepengesoknadProsesseringGateway
import no.nav.helse.soknad.SoknadService
import no.nav.helse.soknad.soknadApis
import no.nav.helse.vedlegg.*
import java.util.concurrent.TimeUnit
import javax.validation.Validation
import javax.validation.Validator

fun main(args: Array<String>): Unit  = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Application.pleiepengesoknadapi() {
    val appId = environment.config.id()
    logProxyProperties()
    DefaultExports.initialize()

    val configuration = Configuration(environment.config)
    val apiGatewayHttpRequestInterceptor = ApiGatewayHttpRequestInterceptor(configuration.getApiGatewayApiKey())

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
        engine {
            customizeClient {
                setProxyRoutePlanner()
                addInterceptorLast(apiGatewayHttpRequestInterceptor)
            }
        }

    }

    install(ContentNegotiation) {
        jackson {
            dusseldorfConfigured()
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

    val idTokenProvider = IdTokenProvider(cookieName = configuration.getCookieName())
    val jwkProvider = JwkProviderBuilder(configuration.getJwksUrl())
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    install(Authentication) {
        jwtFromCookie {
            verifier(jwkProvider, configuration.getIssuer())
            validate { credentials ->
                val acr = credentials.payload.getClaim("acr").asString()
                if ("Level4" != acr) {
                    throw InsufficientAuthenticationLevelException(acr)
                }
                return@validate JWTPrincipal(credentials.payload)
            }
            withIdTokenProvider(idTokenProvider)
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

    install(CallMonitoring) {
        app = appId
        overridePaths = mapOf(
            Pair(Regex("/vedlegg/.+"), "/vedlegg")
        )
    }

    val systemCredentialsProvider = Oauth2ClientCredentialsProvider(
        monitoredHttpClient = MonitoredHttpClient(
            source = appId,
            destination = "nais-sts",
            httpClient = HttpClient(Apache) {
                install(JsonFeature) {
                    serializer = JacksonSerializer {
                        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    }
                }
                install (Logging) {
                    sl4jLogger("nais-sts")
                }
                engine {
                    customizeClient {
                        setProxyRoutePlanner()
                        addInterceptorLast(apiGatewayHttpRequestInterceptor)
                    }
                }
            }
        ),
        tokenUrl = configuration.getAuthorizationServerTokenUrl(),
        clientId = configuration.getServiceAccountClientId(),
        clientSecret = configuration.getServiceAccountClientSecret(),
        scopes = configuration.getServiceAccountScopes()
    )

    install(Routing) {

        val aktoerService = AktoerService(
            aktoerGateway = AktoerGateway(
                httpClient = httpClient,
                baseUrl = configuration.getAktoerRegisterUrl(),
                systemCredentialsProvider = systemCredentialsProvider
            )
        )

        val vedleggService = VedleggService(
            pleiepengerDokumentGateway = PleiepengerDokumentGateway(
                httpClient = httpClient,
                baseUrl = configuration.getPleiepengerDokumentUrl()
            )
        )

        val sokerService =  SokerService(
            sokerGateway = SokerGateway(
                httpClient = httpClient,
                baseUrl = configuration.getSparkelUrl(),
                aktoerService = aktoerService,
                systemCredentialsProvider = systemCredentialsProvider
            )
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
                        systemCredentialsProvider = systemCredentialsProvider
                    )
                )
            )

            vedleggApis(
                vedleggService = vedleggService,
                idTokenProvider = idTokenProvider
            )

            soknadApis(
                idTokenProvider = idTokenProvider,
                validationHandler = validationHandler,
                soknadService = SoknadService(
                    pleiepengesoknadProsesseringGateway = PleiepengesoknadProsesseringGateway(
                        httpClient = httpClient,
                        baseUrl = configuration.getPleiepengesoknadProsesseringBaseUrl(),
                        systemCredentialsProvider = systemCredentialsProvider
                    ),
                    sokerService = sokerService,
                    vedleggService = vedleggService
                )
            )
        }

        DefaultProbeRoutes()
        MetricsRoute()
        HealthRoute(
            healthChecks = setOf(
                SystemCredentialsProviderHealthCheck(systemCredentialsProvider)
            )
        )
    }

    install(CallId) {
        generated()
    }

    install(CallLogging) {
        correlationIdAndRequestIdInMdc()
        logRequests()
    }
}