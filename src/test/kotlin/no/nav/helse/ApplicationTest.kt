package no.nav.helse

import com.auth0.jwt.exceptions.TokenExpiredException
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import com.typesafe.config.ConfigFactory
import io.ktor.config.ApplicationConfig
import io.ktor.config.HoconApplicationConfig
import io.ktor.http.*
import kotlin.test.*
import io.ktor.server.testing.*
import io.ktor.util.KtorExperimentalAPI
import no.nav.common.KafkaEnvironment
import no.nav.helse.ansettelsesforhold.AnsettelsesforholdResponse
import no.nav.helse.barn.BarnResponse
import no.nav.helse.general.auth.CookieNotSetException
import no.nav.helse.general.auth.InsufficientAuthenticationLevelException
import no.nav.helse.general.jackson.configureObjectMapper
import no.nav.helse.kafka.bootstrapKafka
import no.nav.helse.kafka.getPassword
import no.nav.helse.kafka.getUsername
import no.nav.helse.wiremock.*
import org.junit.AfterClass
import org.junit.BeforeClass
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

private const val fnr = "290990123456"
private val oneMinuteInMillis = Duration.ofMinutes(1).toMillis()
private val logger: Logger = LoggerFactory.getLogger("nav.ApplicationTest")

@KtorExperimentalAPI
class ApplicationTest {

    private val objectMapper = configureObjectMapper()

    private companion object {

        val wireMockServer: WireMockServer = bootstrapWiremock()
        val kafkaEnvironment: KafkaEnvironment = bootstrapKafka()

        fun getConfig() : ApplicationConfig {

            val fileConfig = ConfigFactory.load()
            val testConfig = ConfigFactory.parseMap(mutableMapOf(
                Pair("nav.gateways.sparkel_url", wireMockServer.getSparkelUrl()),
                Pair("nav.authorization.jwks_uri", wireMockServer.getJwksUri()),
                Pair("nav.kafka.bootstrap_servers", kafkaEnvironment.brokersURL),
                Pair("nav.kafka.username", kafkaEnvironment.getUsername()),
                Pair("nav.kafka.password", kafkaEnvironment.getPassword()),
                Pair("nav.authorization.token_url", wireMockServer.getAuthorizationTokenUrl())
            ))

            val mergedConfig = testConfig.withFallback(fileConfig)

            return HoconApplicationConfig(mergedConfig)
        }


        val engine = TestApplicationEngine(createTestEnvironment {
            config = getConfig()
        })


        @BeforeClass
        @JvmStatic
        fun buildUp() {
            engine.start(wait = true)
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            logger.info("Tearing down")
            kafkaEnvironment.tearDown()
            wireMockServer.stop()
            logger.info("Tear down complete")
        }
    }

    @Test
    fun getBarnAuthorizedTest() {
        val cookie = getAuthCookie(fnr)

        with(engine) {
            with(handleRequest(HttpMethod.Get, "/barn") {
                addHeader("Accept", "application/json")
                addHeader("Cookie", cookie.toString())
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                val mappedResponse : BarnResponse = objectMapper.readValue(response.content!!)
                assertEquals(2, mappedResponse.barn.size)
            }
        }
    }

    @Test(expected = CookieNotSetException::class)
    fun getBarnUnauthorizedTest() {
        with(engine) {
            with(handleRequest(HttpMethod.Get, "/barn") {
                addHeader("Accept", "application/json")
            }) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }

    @Test(expected = InsufficientAuthenticationLevelException::class)
    fun getBarnWrongAuthenticationLevel() {

        val cookie = getAuthCookie(fnr, authLevel = "Level3")

        with(engine) {
            with(handleRequest(HttpMethod.Get, "/barn") {
                addHeader("Accept", "application/json")
                addHeader("Cookie", cookie.toString())
            }) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }
    }

    @Test(expected = TokenExpiredException::class)
    fun getBarnExpiredToken() {

        val cookie = getAuthCookie(fnr, expiry = -(oneMinuteInMillis))
        logger.debug("cookie={}", cookie.toString())

        with(engine) {
            with(handleRequest(HttpMethod.Get, "/barn") {
                addHeader("Accept", "application/json")
                addHeader("Cookie", cookie.toString())
            }) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }

    @Test
    fun sendSoknadValidering() {
        val cookie = getAuthCookie(fnr).toString()
        obligatoriskeFelterIkkeSatt(engine, cookie)
        ugyldigInformasjonOmBarn(engine, cookie)
    }

    @Test
    fun getAnsettelsesforholdAuthorizedTest() {
        val cookie = getAuthCookie(fnr)

        with(engine) {
            with(handleRequest(HttpMethod.Get, "/ansettelsesforhold") {
                addHeader("Accept", "application/json")
                addHeader("Cookie", cookie.toString())
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                val expectedResponse : AnsettelsesforholdResponse = objectMapper.readValue(expectedGetAnsettelsesforholdJson)
                val actualResponse : AnsettelsesforholdResponse = objectMapper.readValue(response.content!!)
                assertEquals(expectedResponse, actualResponse)

            }
        }
    }

    @Test
    fun testDeepIsReadyReturnsOk() {
        with(engine) {
            with(handleRequest(HttpMethod.Get, "/isready-deep")) {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }
}
