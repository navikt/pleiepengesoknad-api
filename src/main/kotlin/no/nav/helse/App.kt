package no.nav.helse

import com.auth0.jwk.JwkProviderBuilder
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.features.*
import io.ktor.http.HttpMethod
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
import no.nav.helse.dusseldorf.ktor.core.*
import no.nav.helse.dusseldorf.ktor.health.HealthRoute
import no.nav.helse.dusseldorf.ktor.jackson.JacksonStatusPages
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.helse.dusseldorf.ktor.metrics.MetricsRoute
import no.nav.helse.dusseldorf.ktor.metrics.init
import no.nav.helse.general.auth.*
import no.nav.helse.general.systemauth.AuthorizationServiceResolver
import no.nav.helse.person.PersonGateway
import no.nav.helse.person.PersonService
import no.nav.helse.soker.SokerService
import no.nav.helse.soker.sokerApis
import no.nav.helse.soknad.PleiepengesoknadProsesseringGateway
import no.nav.helse.soknad.SoknadService
import no.nav.helse.soknad.soknadApis
import no.nav.helse.vedlegg.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
    addApiGatewayFuelInterceptor(configuration.getApiGatewayApiKey())
    val authorizationServiceResolver = AuthorizationServiceResolver(environment.config.clients())

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

    val sparkelClient = Clients.sparkelClient(apiGatewayHttpRequestInterceptor)

    val systemCredentialsProvider = Oauth2ClientCredentialsProvider(
        monitoredHttpClient = Clients.stsClient(apiGatewayHttpRequestInterceptor),
        tokenUrl = configuration.getAuthorizationServerTokenUrl(),
        clientId = configuration.getServiceAccountClientId(),
        clientSecret = configuration.getServiceAccountClientSecret(),
        scopes = configuration.getServiceAccountScopes()
    )


    install(Routing) {

        val aktoerService = AktoerService(
            aktoerGateway = AktoerGateway(
                baseUrl = configuration.getAktoerRegisterUrl(),
                authorizationService = authorizationServiceResolver.aktoerRegister()
            )
        )

        val vedleggService = VedleggService(
            pleiepengerDokumentGateway = PleiepengerDokumentGateway(
                monitoredHttpClient = Clients.pleiepengerDokumentClient(),
                baseUrl = configuration.getPleiepengerDokumentUrl()
            )
        )

        val personService = PersonService(
            personGateway = PersonGateway(
                monitoredHttpClient = sparkelClient,
                baseUrl = configuration.getSparkelUrl(),
                systemCredentialsProvider = systemCredentialsProvider
            ),
            aktoerService = aktoerService
        )

        val sokerService = SokerService(
            personService = personService
        )

        authenticate {

            sokerApis(
                sokerService = sokerService
            )

            barnApis(
                barnService = BarnService(
                    barnGateway = BarnGateway(
                        monitoredHttpClient = sparkelClient,
                        baseUrl = configuration.getSparkelUrl(),
                        aktoerService = aktoerService,
                        systemCredentialsProvider = systemCredentialsProvider
                    )
                )
            )

            arbeidsgiverApis(
                service = ArbeidsgiverService(
                    gateway = ArbeidsgiverGateway(
                        monitoredHttpClient = sparkelClient,
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
                soknadService = SoknadService(
                    pleiepengesoknadProsesseringGateway = PleiepengesoknadProsesseringGateway(
                        monitoredHttpClient = Clients.pleiepengesoknadProsesseringClient(apiGatewayHttpRequestInterceptor),
                        baseUrl = configuration.getPleiepengesoknadProsesseringBaseUrl(),
                        systemCredentialsProvider = systemCredentialsProvider
                    ),
                    sokerService = sokerService,
                    personService = personService,
                    vedleggService = vedleggService,
                    aktoerService = aktoerService
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

private fun addApiGatewayFuelInterceptor(apiGatewayApiKey: ApiGatewayApiKey) {
    FuelManager.instance.addRequestInterceptor { next: (Request) -> Request ->
        { req: Request ->
            if (req.url.path.contains("helse-reverse-proxy", true)) {
                req.header(mapOf(apiGatewayApiKey.headerKey to apiGatewayApiKey.value))
            }
            next(req)
        }
    }
}