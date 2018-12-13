package no.nav.pleiepenger.api

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.ktor.http.*
import kotlin.test.*
import io.ktor.server.testing.*
import io.ktor.config.MapApplicationConfig
import no.nav.pleiepenger.api.general.jackson.configureObjectMapper
import no.nav.pleiepenger.api.id.IdResponse
import org.junit.AfterClass
import org.junit.BeforeClass

class ApplicationTest {

    private val objectMapper = configureObjectMapper()


    private companion object {

        val wireMockServer: WireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())

        init {
            wireMockServer.start()
        }


        val engine = TestApplicationEngine(createTestEnvironment {
            config = MapApplicationConfig().apply {
                put("nav.cors.addresses", listOf("http://localhost:8888"))
                put("nav.gateways.idGateway.baseUrl",
                    URLBuilder().takeFrom(wireMockServer.baseUrl()).path("id-gateway").build().toString()
                )
            }
            module {
                pleiepengesoknadapi()
            }
        })


        @BeforeClass
        @JvmStatic
        fun setup() {
            engine.start(wait = false)
            configureFor(wireMockServer.port())

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
