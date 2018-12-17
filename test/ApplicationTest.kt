package no.nav.pleiepenger.api

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.typesafe.config.ConfigFactory
import io.ktor.config.ApplicationConfig
import io.ktor.config.HoconApplicationConfig
import io.ktor.http.*
import kotlin.test.*
import io.ktor.server.testing.*
import no.nav.pleiepenger.api.general.jackson.configureObjectMapper
import no.nav.pleiepenger.api.id.IdResponse
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
                Pair("nav.gateways.idGateway.baseUrl", wireMockServer.baseUrl() + "/id-gateway")
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
    fun getIdTest() {
        stubFor(
            get("/id-gateway/foo")
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"1234\"}")
                )
        )
        with(engine) {
            with(handleRequest(HttpMethod.Get, "/id/12345678911") {
                addHeader("accept", "application/json")
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(IdResponse("1234"), objectMapper.readValue(response.content!!))
           }
        }
    }
}
