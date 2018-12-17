package no.nav.pleiepenger.api

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.typesafe.config.ConfigFactory
import io.ktor.config.ApplicationConfig
import io.ktor.config.HoconApplicationConfig
import io.ktor.http.*
import kotlin.test.*
import io.ktor.server.testing.*
import no.nav.pleiepenger.api.general.auth.UnauthorizedException
import no.nav.pleiepenger.api.general.jackson.configureObjectMapper
import no.nav.pleiepenger.api.wiremock.bootstrap
import org.junit.AfterClass
import org.junit.BeforeClass

class ApplicationTest {

    private val objectMapper = configureObjectMapper()


    private companion object {

        val wireMockServer: WireMockServer = bootstrap()

        fun getConfig() : ApplicationConfig {

            val fileConfig = ConfigFactory.load()
            val testConfig = ConfigFactory.parseMap(mutableMapOf(
                Pair("nav.gateways.sparkel_url", wireMockServer.baseUrl() + "/sparkel-mock")
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
        stubFor(
            get("/sparkel-mock/foo")
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"1234\"}")
                )
        )
        val cookie = getAuthCookie("290990123456")

        with(engine) {
            with(handleRequest(HttpMethod.Get, "/barn") {
                addHeader("Accept", "application/json")
                addHeader("Cookie", cookie.toString())
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test(expected = UnauthorizedException::class)
    fun getBarnUnauthorizedTest() {
        with(engine) {
            with(handleRequest(HttpMethod.Get, "/barn") {
                addHeader("Accept", "application/json")
            }) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }
    }
}
