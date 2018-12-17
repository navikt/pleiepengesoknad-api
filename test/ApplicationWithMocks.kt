package no.nav.pleiepenger.api

import io.ktor.server.testing.withApplication
import com.github.tomakehurst.wiremock.client.WireMock.*
import no.nav.pleiepenger.api.wiremock.bootstrap
import no.nav.pleiepenger.api.wiremock.getJwksUri
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("nav.ApplicationWithMocks")

class ApplicationWithMocks {

    companion object {
        @JvmStatic fun main(args: Array<String>) {

            val wireMockServer = bootstrap(8084)
            stubGetId()

            val testArgs = arrayOf(
                "-P:ktor.deployment.port=8085",
                "-P:nav.gateways.idGateway.baseUrl=" + wireMockServer.baseUrl() + "/id-gateway",
                "-P:nav.authorization.jwks_uri=" + wireMockServer.getJwksUri()
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