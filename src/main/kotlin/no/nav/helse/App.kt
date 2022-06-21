package no.nav.helse

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.routing.*
import io.prometheus.client.hotspot.DefaultExports
import no.nav.helse.arbeidsgiver.ArbeidsgivereGateway
import no.nav.helse.arbeidsgiver.ArbeidsgivereService
import no.nav.helse.arbeidsgiver.arbeidsgiverApis
import no.nav.helse.barn.BarnGateway
import no.nav.helse.barn.BarnService
import no.nav.helse.barn.barnApis
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.helse.dusseldorf.ktor.auth.IdTokenStatusPages
import no.nav.helse.dusseldorf.ktor.auth.clients
import no.nav.helse.dusseldorf.ktor.client.HttpRequestHealthCheck
import no.nav.helse.dusseldorf.ktor.client.HttpRequestHealthConfig
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.DefaultProbeRoutes
import no.nav.helse.dusseldorf.ktor.core.DefaultStatusPages
import no.nav.helse.dusseldorf.ktor.core.correlationIdAndRequestIdInMdc
import no.nav.helse.dusseldorf.ktor.core.generated
import no.nav.helse.dusseldorf.ktor.core.id
import no.nav.helse.dusseldorf.ktor.core.log
import no.nav.helse.dusseldorf.ktor.core.logProxyProperties
import no.nav.helse.dusseldorf.ktor.core.logRequests
import no.nav.helse.dusseldorf.ktor.health.HealthReporter
import no.nav.helse.dusseldorf.ktor.health.HealthRoute
import no.nav.helse.dusseldorf.ktor.health.HealthService
import no.nav.helse.dusseldorf.ktor.jackson.JacksonStatusPages
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.helse.dusseldorf.ktor.metrics.MetricsRoute
import no.nav.helse.dusseldorf.ktor.metrics.init
import no.nav.helse.endringsmelding.EndringsmeldingService
import no.nav.helse.endringsmelding.endringsmeldingApis
import no.nav.helse.general.systemauth.AccessTokenClientResolver
import no.nav.helse.innsyn.InnsynGateway
import no.nav.helse.innsyn.InnsynService
import no.nav.helse.kafka.KafkaProducer
import no.nav.helse.mellomlagring.K9BrukerdialogCacheGateway
import no.nav.helse.mellomlagring.MellomlagringService
import no.nav.helse.mellomlagring.mellomlagringApis
import no.nav.helse.soker.SøkerGateway
import no.nav.helse.soker.SøkerService
import no.nav.helse.soker.søkerApis
import no.nav.helse.soknad.SøknadService
import no.nav.helse.soknad.soknadApis
import no.nav.helse.vedlegg.K9MellomlagringGateway
import no.nav.helse.vedlegg.VedleggService
import no.nav.helse.vedlegg.vedleggApis
import no.nav.security.token.support.v2.RequiredClaims
import no.nav.security.token.support.v2.asIssuerProps
import no.nav.security.token.support.v2.tokenValidationSupport
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)


fun Application.pleiepengesoknadapi() {
    val appId = environment.config.id()
    val logger: Logger = LoggerFactory.getLogger("no.nav.helse.AppKt.pleiepengesoknadapi")
    logProxyProperties()
    DefaultExports.initialize()

    System.setProperty("dusseldorf.ktor.serializeProblemDetailsWithContentNegotiation", "true")

    val configuration = Configuration(environment.config)
    val config = this.environment.config
    val idTokenProvider = IdTokenProvider(cookieName = configuration.getCookieName())
    val allIssuers = config.asIssuerProps().keys
    val accessTokenClientResolver = AccessTokenClientResolver(environment.config.clients())

    install(ContentNegotiation) {
        jackson {
            pleiepengesøknadKonfigurert()
        }
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowNonSimpleContentTypes = true
        allowCredentials = true
        logger.info("Configuring CORS")
        configuration.getWhitelistedCorsAddreses().forEach {
            logger.info("Adding host {} with scheme {}", it.host, it.scheme)
            allowHost(host = it.authority, schemes = listOf(it.scheme))
        }
    }


    install(Authentication) {
        allIssuers.forEach { issuer: String ->
            tokenValidationSupport(
                name = issuer,
                config = config,
                requiredClaims = RequiredClaims(
                    issuer = issuer,
                    claimMap = arrayOf("acr=Level4")
                )
            )
        }
    }

    install(StatusPages) {
        DefaultStatusPages()
        JacksonStatusPages()
        IdTokenStatusPages()
    }

    install(Routing) {
        val k9MellomlagringGateway = K9MellomlagringGateway(
            baseUrl = configuration.getK9MellomlagringUrl(),
            accessTokenClient = accessTokenClientResolver.accessTokenClient(),
            k9MellomlagringScope = configuration.getK9MellomlagringScopes()
        )

        val vedleggService = VedleggService(k9MellomlagringGateway = k9MellomlagringGateway)

        val søkerGateway = SøkerGateway(baseUrl = configuration.getK9OppslagUrl())
        val barnGateway = BarnGateway(baseUrl = configuration.getK9OppslagUrl())
        val arbeidsgivereGateway = ArbeidsgivereGateway(baseUrl = configuration.getK9OppslagUrl())
        val søkerService = SøkerService(søkerGateway = søkerGateway)

        val barnService = BarnService(
            barnGateway = barnGateway,
            cache = configuration.cache()
        )

        val kafkaProducer = KafkaProducer(kafkaConfig = configuration.getKafkaConfig())

        environment!!.monitor.subscribe(ApplicationStopping) {
            logger.info("Stopper Kafka Producer.")
            kafkaProducer.stop()
            logger.info("Kafka Producer Stoppet.")
        }

        authenticate(*allIssuers.toTypedArray()) {
            søkerApis(
                søkerService = søkerService,
                idTokenProvider = idTokenProvider
            )

            barnApis(
                barnService = barnService,
                idTokenProvider = idTokenProvider
            )

            arbeidsgiverApis(
                arbeidsgivereService = ArbeidsgivereService(
                    arbeidsgivereGateway = arbeidsgivereGateway
                ),
                idTokenProvider = idTokenProvider,
                miljø = configuration.miljø()
            )

            mellomlagringApis(
                mellomlagringService = MellomlagringService(
                    k9BrukerdialogCacheGateway = K9BrukerdialogCacheGateway(
                        baseUrl = configuration.getK9BrukerdialogCacheUrl(),
                        tokenxClient = accessTokenClientResolver.tokenxClient(),
                        k9BrukerdialogCacheTokenxAudience = configuration.getK9BrukerdialogCacheTokenxAudience()
                    ),
                    søknadMellomlagretTidTimer = configuration.getSoknadMellomlagringTidTimer(),
                    endringsmeldingMellomlagretTidTimer = configuration.getEndringsmeldingMellomlagringTidTimer()
                ),
                idTokenProvider = idTokenProvider
            )

            vedleggApis(
                vedleggService = vedleggService,
                idTokenProvider = idTokenProvider,
                søkerService = søkerService
            )

            soknadApis(
                idTokenProvider = idTokenProvider,
                søknadService = SøknadService(
                    vedleggService = vedleggService,
                    barnService = barnService,
                    søkerService = søkerService,
                    kafkaProducer = kafkaProducer
                ),
                barnService = barnService,
                søkerService = søkerService,
                vedleggService = vedleggService
            )

            endringsmeldingApis(
                endringsmeldingService = EndringsmeldingService(
                    kafkaProducer = kafkaProducer
                ),
                søkerService = søkerService,
                barnService = barnService,
                innsynService = InnsynService(
                    innsynGateway = InnsynGateway(baseUrl = configuration.getSifInnsynApiUrl())
                ),
                idTokenProvider = idTokenProvider,
                miljø = configuration.miljø()
            )
        }

        val healthService = HealthService(
            healthChecks = setOf(
                kafkaProducer,
                HttpRequestHealthCheck(
                    mapOf(
                        Url.buildURL(
                            baseUrl = configuration.getK9MellomlagringUrl(),
                            pathParts = listOf("health")
                        ) to HttpRequestHealthConfig(expectedStatus = HttpStatusCode.OK)
                    )
                )
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
            try {
                val idToken = idTokenProvider.getIdToken(call)
                logger.info("Issuer [{}]", idToken.issuer())
                idToken.getId()
            } catch (cause: Throwable) {
                null
            }
        }
    }
}

fun ObjectMapper.pleiepengesøknadKonfigurert(): ObjectMapper {
    return dusseldorfConfigured().apply {
        configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
    }
}

 fun ObjectMapper.k9MellomlagringGatewayKonfigurert(): ObjectMapper {
     return jacksonObjectMapper().dusseldorfConfigured().apply {
         configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
         propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
     }
}

fun ObjectMapper.k9SelvbetjeningOppslagKonfigurert(): ObjectMapper {
    return jacksonObjectMapper().dusseldorfConfigured().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        registerModule(JavaTimeModule())
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    }
}
