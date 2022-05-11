package no.nav.helse

import io.ktor.server.testing.*
import no.nav.helse.dusseldorf.testsupport.asArguments
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.helse.wiremock.pleiepengesoknadApiConfig
import no.nav.helse.wiremock.stubK9Mellomlagring
import no.nav.helse.wiremock.stubK9MellomlagringHealth
import no.nav.helse.wiremock.stubK9OppslagArbeidsgivere
import no.nav.helse.wiremock.stubK9OppslagBarn
import no.nav.helse.wiremock.stubK9OppslagSoker
import no.nav.helse.wiremock.stubOppslagHealth
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ApplicationWithMocks {
    companion object {

        private val logger: Logger = LoggerFactory.getLogger(ApplicationWithMocks::class.java)

        @JvmStatic
        fun main(args: Array<String>) {

            val wireMockServer = WireMockBuilder()
                .withPort(8081)
                .withAzureSupport()
                .withLoginServiceSupport()
                .pleiepengesoknadApiConfig()
                .build()
                .stubK9MellomlagringHealth()
                .stubOppslagHealth()
                .stubK9Mellomlagring()
                .stubK9OppslagSoker()
                .stubK9OppslagBarn()
                .stubK9OppslagArbeidsgivere()

            val testArgs = TestConfiguration.asMap(
                port = 8082,
                wireMockServer = wireMockServer
            ).asArguments()

            Runtime.getRuntime().addShutdownHook(object : Thread() {
                override fun run() {
                    logger.info("Tearing down")
                    wireMockServer.stop()
                    logger.info("Tear down complete")
                }
            })

            withApplication { no.nav.helse.main(testArgs) }
        }
    }
}
