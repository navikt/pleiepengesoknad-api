package no.nav.helse

import com.github.tomakehurst.wiremock.WireMockServer
import com.typesafe.config.ConfigFactory
import io.ktor.config.ApplicationConfig
import io.ktor.config.HoconApplicationConfig
import io.ktor.http.*
import kotlin.test.*
import io.ktor.server.testing.*
import io.ktor.util.KtorExperimentalAPI
import no.nav.helse.general.jackson.configureObjectMapper
import no.nav.helse.wiremock.*
import org.junit.AfterClass
import org.junit.BeforeClass
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*

private const val fnr = "290990123456"
private val oneMinuteInMillis = Duration.ofMinutes(1).toMillis()
private val logger: Logger = LoggerFactory.getLogger("nav.ApplicationTest")

@KtorExperimentalAPI
class ApplicationTest {

    private val objectMapper = configureObjectMapper()

    private companion object {

        val wireMockServer: WireMockServer = bootstrapWiremock()

        fun getConfig() : ApplicationConfig {

            val fileConfig = ConfigFactory.load()
            val testConfig = ConfigFactory.parseMap(TestConfiguration.asMap(wireMockServer = wireMockServer))
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
            wireMockServer.stop()
            logger.info("Tear down complete")
        }
    }

    @Test
    fun getAnsettelsesforholdUnauthorizedTest() {
        with(engine) {
            with(handleRequest(HttpMethod.Get, "/arbeidsgiver?fra_og_med=2019-01-01&til_og_med=2019-01-30") {
                addHeader("Accept", "application/json")
            }) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }

    @Test
    fun getAnsettelsesforholdWrongAuthenticationLevel() {

        val cookie = getAuthCookie(fnr, authLevel = "Level3")

        with(engine) {
            with(handleRequest(HttpMethod.Get, "/arbeidsgiver?fra_og_med=2019-01-01&til_og_med=2019-01-30") {
                addHeader("Accept", "application/json")
                addHeader("Cookie", cookie.toString())
            }) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }
    }

    @Test
    fun getAnsettelsesforholdExpiredToken() {

        val cookie = getAuthCookie(fnr, expiry = -(oneMinuteInMillis))
        logger.debug("cookie={}", cookie.toString())

        with(engine) {
            with(handleRequest(HttpMethod.Get, "/arbeidsgiver?fra_og_med=2019-01-01&til_og_med=2019-01-30") {
                addHeader("Accept", "application/json")
                addHeader("Cookie", cookie.toString())
            }) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }

    @Test
    fun sendSoknadTests() {
        val cookie = getAuthCookie(fnr)
        //testAtDetLiggerMeldingPaaKoen(antallMeldinger = 0, kafkaEnvironment = kafkaEnvironment)
        gyldigSoknad(engine, cookie)
        //testAtDetLiggerMeldingPaaKoen(antallMeldinger = 1, kafkaEnvironment = kafkaEnvironment)
        obligatoriskeFelterIkkeSatt(engine, cookie)
        ugyldigInformasjonOmBarn(engine, cookie)
    }

    @Test
    fun getAnsettelsesforholdAuthorizedTest() {
        val cookie = getAuthCookie(fnr)

        with(engine) {
            with(handleRequest(HttpMethod.Get, "/arbeidsgiver?fra_og_med=2019-01-01&til_og_med=2019-01-30") {
                addHeader("Accept", "application/json")
                addHeader("Cookie", cookie.toString())
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                val expectedResponse = objectMapper.readTree(expectedGetAnsettelsesforholdJson)
                val actualResponse = objectMapper.readTree(response.content!!)
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

    @Test
    fun testGetBarnIsEmptyList() {
        val cookie = getAuthCookie(fnr)

        with(engine) {
            with(handleRequest(HttpMethod.Get, "/barn") {
                addHeader("Accept", "application/json")
                addHeader("Cookie", cookie.toString())
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                val expectedResponse = objectMapper.readTree("""
                    {
                        "barn": []
                    }
                """.trimIndent())
                val actualResponse = objectMapper.readTree(response.content!!)
                assertEquals(expectedResponse, actualResponse)
            }
        }
    }

    @Test
    fun testHaandteringAvVedlegg() {
        val cookie = getAuthCookie(fnr)
        val jpeg = "vedlegg/iPhone_6.jpg".fromResources()

        with(engine) {
            // LASTER OPP VEDLEGG
            val url = handleRequestUploadImage(
                cookie = cookie,
                vedlegg = jpeg
            )
            val path = Url(url).fullPath
            // HENTER OPPLASTET VEDLEGG
            handleRequest(HttpMethod.Get, path) {
                addHeader("Cookie", cookie.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(Arrays.equals(jpeg, response.byteContent))
                // SLETTER OPPLASTET VEDLEGG
                handleRequest(HttpMethod.Delete, path) {
                    addHeader("Cookie", cookie.toString())
                }.apply {
                    assertEquals(HttpStatusCode.NoContent, response.status())
                    // VERIFISERER AT VEDLEGG ER SLETTET
                    handleRequest(HttpMethod.Get, path) {
                        addHeader("Cookie", cookie.toString())
                    }.apply {
                        assertEquals(HttpStatusCode.NotFound, response.status())
                    }
                }
            }
        }
    }

    @Test
    fun testHentSoker() {
        val cookie = getAuthCookie(fnr)

        with(engine) {
            with(handleRequest(HttpMethod.Get, "/soker") {
                addHeader("Accept", "application/json")
                addHeader("Cookie", cookie.toString())
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                val expectedResponse = objectMapper.readTree(expectedGetSokerJson(fnr))
                val actualResponse = objectMapper.readTree(response.content!!)
                assertEquals(expectedResponse, actualResponse)
            }
        }
    }
}