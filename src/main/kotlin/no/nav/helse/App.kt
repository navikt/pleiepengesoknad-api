package no.nav.helse

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.metrics.micrometer.*
import io.ktor.routing.*
import io.prometheus.client.hotspot.DefaultExports
import no.nav.helse.arbeidsgiver.ArbeidsgivereGateway
import no.nav.helse.arbeidsgiver.ArbeidsgivereService
import no.nav.helse.arbeidsgiver.arbeidsgiverApis
import no.nav.helse.barn.BarnGateway
import no.nav.helse.barn.BarnService
import no.nav.helse.barn.barnApis
import no.nav.helse.dusseldorf.ktor.auth.*
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
import no.nav.helse.endringsmelding.EndringsmeldingService
import no.nav.helse.endringsmelding.endringsmeldingApis
import no.nav.helse.general.systemauth.AccessTokenClientResolver
import no.nav.helse.innsyn.InnsynGateway
import no.nav.helse.innsyn.InnsynService
import no.nav.helse.kafka.KafkaProducer
import no.nav.helse.mellomlagring.MellomlagringService
import no.nav.helse.mellomlagring.mellomlagringApis
import no.nav.helse.redis.RedisConfig
import no.nav.helse.redis.RedisStore
import no.nav.helse.soker.SøkerGateway
import no.nav.helse.soker.SøkerService
import no.nav.helse.soker.søkerApis
import no.nav.helse.soknad.SøknadService
import no.nav.helse.soknad.soknadApis
import no.nav.helse.vedlegg.K9MellomlagringGateway
import no.nav.helse.vedlegg.VedleggService
import no.nav.helse.vedlegg.vedleggApis
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.AppKt.pleiepengesoknadapi")

fun Application.pleiepengesoknadapi() {
    val appId = environment.config.id()
    logProxyProperties()
    DefaultExports.initialize()

    System.setProperty("dusseldorf.ktor.serializeProblemDetailsWithContentNegotiation", "true")

    val configuration = Configuration(environment.config)
    val accessTokenClientResolver = AccessTokenClientResolver(environment.config.clients())

    install(ContentNegotiation) {
        jackson {
            pleiepengesøknadKonfigurert()
        }
    }

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
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
    val issuers = configuration.issuers()

    install(Authentication) {
        multipleJwtIssuers(
            issuers = issuers,
            extractHttpAuthHeader = { call ->
                idTokenProvider.getIdToken(call)
                    .somHttpAuthHeader()
            }
        )
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

        environment.monitor.subscribe(ApplicationStopping) {
            logger.info("Stopper Kafka Producer.")
            kafkaProducer.stop()
            logger.info("Kafka Producer Stoppet.")
        }

        authenticate(*issuers.allIssuers()) {
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
                    søknadMellomlagretTidTimer = configuration.getSoknadMellomlagringTidTimer(),
                    endringsmeldingMellomlagretTidTimer = configuration.getEndringsmeldingMellomlagringTidTimer(),
                    redisStore = RedisStore(
                        redisClient = RedisConfig.redisClient(
                            redisHost = configuration.getRedisHost(),
                            redisPort = configuration.getRedisPort()
                        )
                    ),
                    passphrase = configuration.getStoragePassphrase(),
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
                idTokenProvider.getIdToken(call).getId()
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


fun StatusPages.Configuration.DefaultStatusPages() {

    exception<Throwblem> { cause ->
        call.respondProblemDetails(cause.getProblemDetails(), logger)
    }

    exception<Throwable> { cause ->
        when (cause) {
            is Problem -> call.respondProblemDetails(cause.getProblemDetails(), logger)
            is IllegalArgumentException -> {
                call.respondProblemDetails(DefaultProblemDetails(
                    title = "IllegalArgumentException",
                    status = 403,
                    detail = "${cause.message}"
                ), logger)
            }
            else -> {
                logger.error("Uhåndtert feil", cause)
                call.respondProblemDetails(UNHANDLED_PROBLEM_MESSAGE, logger)
            }
        }
    }
}

private val UNHANDLED_PROBLEM_MESSAGE = DefaultProblemDetails(
    title = "unhandled-error",
    status = 500,
    detail = "En uhåndtert feil har oppstått."
)