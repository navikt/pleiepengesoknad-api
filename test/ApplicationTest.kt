package no.nav.pleiepenger.api

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
import no.nav.pleiepenger.api.barn.BarnResponse
import no.nav.pleiepenger.api.general.auth.CookieNotSetException
import no.nav.pleiepenger.api.general.auth.InsufficientAuthenticationLevelException
import no.nav.pleiepenger.api.general.jackson.configureObjectMapper
import no.nav.pleiepenger.api.wiremock.*
import org.junit.AfterClass
import org.junit.BeforeClass

private const val fnr = "290990123456"

@KtorExperimentalAPI
class ApplicationTest {

    private val objectMapper = configureObjectMapper()

    private companion object {

        val wireMockServer: WireMockServer = bootstrap()

        fun getConfig() : ApplicationConfig {

            val fileConfig = ConfigFactory.load()
            val testConfig = ConfigFactory.parseMap(mutableMapOf(
                Pair("nav.gateways.sparkel_url", wireMockServer.getSparkelUrl()),
                Pair("nav.authorization.jwks_uri", wireMockServer.getJwksUri())
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
            wireMockServer.stop()
        }
    }

    @Test
    fun getBarnAuthorizedTest() {
        stubSparkelgetId()
        stubSparkelGetBarn()
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

        val cookie = getAuthCookie(fnr, expiryInMinutes = -1)

        with(engine) {
            with(handleRequest(HttpMethod.Get, "/barn") {
                addHeader("Accept", "application/json")
                addHeader("Cookie", cookie.toString())
            }) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }
}
