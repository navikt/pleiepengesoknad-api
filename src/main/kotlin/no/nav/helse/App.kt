package no.nav.helse

import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.features.*
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.jackson.jackson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.routing.Routing
import io.ktor.util.KtorExperimentalAPI
import io.prometheus.client.hotspot.DefaultExports
import no.nav.helse.aktoer.AktoerGateway
import no.nav.helse.aktoer.AktoerService
import no.nav.helse.arbeidsgiver.ArbeidsgiverGateway
import no.nav.helse.arbeidsgiver.ArbeidsgiverService
import no.nav.helse.arbeidsgiver.arbeidsgiverApis
import no.nav.helse.barn.BarnGateway
import no.nav.helse.barn.BarnService
import no.nav.helse.barn.barnApis
import no.nav.helse.dusseldorf.ktor.auth.clients
import no.nav.helse.dusseldorf.ktor.client.HttpRequestHealthCheck
import no.nav.helse.dusseldorf.ktor.client.HttpRequestHealthConfig
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.*
import no.nav.helse.dusseldorf.ktor.health.HealthReporter
import no.nav.helse.dusseldorf.ktor.health.HealthRoute
import no.nav.helse.dusseldorf.ktor.health.HealthService
import no.nav.helse.dusseldorf.ktor.jackson.JacksonStatusPages
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.helse.dusseldorf.ktor.metrics.MetricsRoute
import no.nav.helse.dusseldorf.ktor.metrics.init
import no.nav.helse.general.auth.*
import no.nav.helse.general.systemauth.AccessTokenClientResolver
import no.nav.helse.k9.K9OppslagBarnService
import no.nav.helse.k9.K9OppslagGateway
import no.nav.helse.k9.K9OppslagSokerService
import no.nav.helse.person.PersonGateway
import no.nav.helse.person.PersonService
import no.nav.helse.soker.sokerApis
import no.nav.helse.soknad.PleiepengesoknadMottakGateway
import no.nav.helse.soknad.SoknadService
import no.nav.helse.soknad.soknadApis
import no.nav.helse.vedlegg.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.TimeUnit

fun main(args: Array<String>): Unit  = io.ktor.server.netty.EngineMain.main(args)

private val logger: Logger = LoggerFactory.getLogger("nav.pleiepengesoknadapi")

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Application.pleiepengesoknadapi() {
    val appId = environment.config.id()
    logProxyProperties()
    DefaultExports.initialize()

    val configuration = Configuration(environment.config)
    val apiGatewayApiKey = configuration.getApiGatewayApiKey()
    val accessTokenClientResolver = AccessTokenClientResolver(environment.config.clients(), apiGatewayApiKey)

    install(ContentNegotiation) {
        jackson {
            dusseldorfConfigured().configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
        }
    }

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Delete)
        allowNonSimpleContentTypes = true
        allowCredentials = true
        log.info("Configuring CORS")
        configuration.getWhitelistedCorsAddreses().forEach {
            log.info("Adding host {} with scheme {}", it.host, it.scheme)
            host(host = it.authority, schemes = listOf(it.scheme))
        }
    }

    val idTokenProvider = IdTokenProvider(cookieName = configuration.getCookieName())
    val jwkProvider = JwkProviderBuilder(configuration.getJwksUrl().toURL())
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    install(Authentication) {
        jwt {
            realm = appId
            verifier(jwkProvider, configuration.getIssuer()) {
                acceptNotBefore(10)
                acceptIssuedAt(10)
            }
            authHeader { call ->
                idTokenProvider
                    .getIdToken(call)
                    .medValidertLevel("Level4")
                    .somHttpAuthHeader()
            }
            validate { credentials ->
                return@validate JWTPrincipal(credentials.payload)
            }
        }
    }

    install(StatusPages) {
        DefaultStatusPages()
        JacksonStatusPages()
        authorizationStatusPages()
    }

    install(Locations)

    install(Routing) {

        val aktoerGateway = AktoerGateway(
            baseUrl = configuration.getAktoerRegisterUrl(),
            accessTokenClient = accessTokenClientResolver.aktoerRegister(),
            apiGatewayApiKey = apiGatewayApiKey
        )
        val aktoerService = AktoerService(aktoerGateway)

        val vedleggService = VedleggService(
            pleiepengerDokumentGateway = PleiepengerDokumentGateway(
                baseUrl = configuration.getPleiepengerDokumentUrl()
            )
        )

        val personGateway = PersonGateway(
            baseUrl = configuration.getSparkelUrl(),
            accessTokenClient = accessTokenClientResolver.sparkel(),
            apiGatewayApiKey = apiGatewayApiKey
        )

        val barnGateway = BarnGateway(
            baseUrl = configuration.getSparkelUrl(),
            aktoerService = aktoerService,
            accessTokenClient = accessTokenClientResolver.sparkel(),
            apiGatewayApiKey = apiGatewayApiKey
        )

        val arbeidsgiverGateway = ArbeidsgiverGateway(
            aktoerService = aktoerService,
            baseUrl = configuration.getSparkelUrl(),
            accessTokenClient = accessTokenClientResolver.sparkel(),
            apiGatewayApiKey = apiGatewayApiKey
        )

        val pleiepengesoknadMottakGateway = PleiepengesoknadMottakGateway(
            baseUrl = configuration.getPleiepengesoknadMottakBaseUrl(),
            accessTokenClient = accessTokenClientResolver.pleiepengesoknadMottak(),
            sendeSoknadTilProsesseringScopes = configuration.getSendSoknadTilProsesseringScopes(),
            apiGatewayApiKey = apiGatewayApiKey
        )

        val k9OppslagGateway = K9OppslagGateway(
            baseUrl = configuration.getK9OppslagUrl(),
            accessTokenClient = accessTokenClientResolver.k9Oppslag(),
            apiGatewayApiKey = apiGatewayApiKey
        )

        val k9OppslagSokerService = K9OppslagSokerService(
            k9OppslagGateway = k9OppslagGateway
        )

        authenticate {

            sokerApis(
                k9OppslagSokerService = k9OppslagSokerService
            )

            barnApis(
                k9OppslagBarnService = K9OppslagBarnService(
                    k9OppslagGateway = k9OppslagGateway
                )
            )

            arbeidsgiverApis(
                service = ArbeidsgiverService(
                    gateway = arbeidsgiverGateway
                )
            )

            vedleggApis(
                vedleggService = vedleggService,
                idTokenProvider = idTokenProvider
            )

            soknadApis(
                idTokenProvider = idTokenProvider,
                soknadService = SoknadService(
                    pleiepengesoknadMottakGateway = pleiepengesoknadMottakGateway,
                    k9OppslagSokerService = k9OppslagSokerService,
                    vedleggService = vedleggService,
                    aktoerService = aktoerService
                )
            )
        }

        val healthService = HealthService(
            healthChecks = setOf(
                aktoerGateway,
                personGateway,
                barnGateway,
                arbeidsgiverGateway,
                pleiepengesoknadMottakGateway,
                HttpRequestHealthCheck(mapOf(
                    configuration.getJwksUrl() to HttpRequestHealthConfig(expectedStatus = HttpStatusCode.OK, includeExpectedStatusEntity = false),
                    Url.buildURL(baseUrl = configuration.getPleiepengerDokumentUrl(), pathParts = listOf("health")) to HttpRequestHealthConfig(expectedStatus = HttpStatusCode.OK),
                    Url.buildURL(baseUrl = configuration.getPleiepengesoknadMottakBaseUrl(), pathParts = listOf("health")) to HttpRequestHealthConfig(expectedStatus = HttpStatusCode.OK, httpHeaders = mapOf(apiGatewayApiKey.headerKey to apiGatewayApiKey.value)),
                    Url.buildURL(baseUrl = configuration.getSparkelUrl(), pathParts = listOf("isready")) to HttpRequestHealthConfig(expectedStatus = HttpStatusCode.OK, httpHeaders = mapOf(apiGatewayApiKey.headerKey to apiGatewayApiKey.value))
                ))
            )
        )

        HealthReporter(
            app = appId,
            healthService = healthService,
            frequency = Duration.ofMinutes(1)
        )

        DefaultProbeRoutes()
        MetricsRoute()
        HealthRoute(
            healthService = healthService
        )
    }

    install(MicrometerMetrics) {
        init(appId)
    }


    intercept(ApplicationCallPipeline.Monitoring) {
        call.request.log()
    }

    install(CallId) {
        generated()
    }

    install(CallLogging) {
        correlationIdAndRequestIdInMdc()
        logRequests()
        mdc("id_token_jti") { call ->
            try { idTokenProvider.getIdToken(call).getId() }
            catch (cause: Throwable) { null }
        }
    }
}