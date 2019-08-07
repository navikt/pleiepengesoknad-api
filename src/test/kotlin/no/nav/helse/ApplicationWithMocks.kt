package no.nav.helse

import io.ktor.server.testing.withApplication
import no.nav.helse.dusseldorf.ktor.testsupport.asArguments
import no.nav.helse.dusseldorf.ktor.testsupport.wiremock.WireMockBuilder
import no.nav.helse.wiremock.*
import no.nav.helse.wiremock.pleiepengesoknadApiConfig
import no.nav.helse.wiremock.stubAktoerRegisterGetAktoerId
import no.nav.helse.wiremock.stubLeggSoknadTilProsessering
import no.nav.helse.wiremock.stubPleiepengerDokument
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
                .withNaisStsSupport()
                .withLoginServiceSupport()
                .pleiepengesoknadApiConfig()
                .build()
                .stubPleiepengerDokumentHealth()
                .stubAktoerRegisterGetAktoerId()
                .stubLeggSoknadTilProsessering()
                .stubPleiepengerDokument()
                .stubSparkelGetArbeidsgivere()

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