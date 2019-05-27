package no.nav.helse

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
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
import no.nav.helse.barn.BarnGateway
import no.nav.helse.barn.BarnService
import no.nav.helse.barn.barnApis
import no.nav.helse.dusseldorf.ktor.client.*
import no.nav.helse.dusseldorf.ktor.core.*
import no.nav.helse.dusseldorf.ktor.health.HealthRoute
import no.nav.helse.dusseldorf.ktor.jackson.JacksonStatusPages
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.helse.dusseldorf.ktor.metrics.CallMonitoring
import no.nav.helse.dusseldorf.ktor.metrics.MetricsRoute
import no.nav.helse.general.auth.*
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
            verifier(jwkProvider, configuration.getIssuer())
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

    install(CallMonitoring) {
        app = appId
        overridePaths = mapOf(
            Pair(Regex("/vedlegg/.+"), "/vedlegg")
        )
    }

    val apiGatewayHttpRequestInterceptor = ApiGatewayHttpRequestInterceptor(configuration.getApiGatewayApiKey())
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
                monitoredHttpClient = Clients.aktoerRegisterClient(apiGatewayHttpRequestInterceptor),
                baseUrl = configuration.getAktoerRegisterUrl(),
                systemCredentialsProvider = systemCredentialsProvider
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
        mdc("id_token_jti") { call ->
            try { idTokenProvider.getIdToken(call).getId() }
            catch (cause: Throwable) { null }
        }
    }
}