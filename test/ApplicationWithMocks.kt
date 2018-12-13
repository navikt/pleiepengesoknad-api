package no.nav.pleiepenger.api

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.ktor.server.testing.withApplication
import com.github.tomakehurst.wiremock.client.WireMock.*


class ApplicationWithMocks {

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            val wireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())
            wireMockServer.start()
            configureFor(wireMockServer.port())
            stubGetId()


            val testArgs = arrayOf(
                "-P:ktor.deployment.port=8082",
                "-P:nav.gateways.idGateway.baseUrl=" + wireMockServer.baseUrl() + "/id-gateway"
            )

            withApplication { no.nav.pleiepenger.api.main(testArgs) }
        }

        fun stubGetId()  {
            stubFor(get(urlPathMatching("/id-gateway/.*")).willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"id\":\"1234\"}")
            ))
        }
    }
}
