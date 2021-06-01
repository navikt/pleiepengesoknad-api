package no.nav.helse

import com.github.fppt.jedismock.RedisServer
import io.ktor.server.testing.*
import no.nav.helse.dusseldorf.testsupport.asArguments
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.helse.mellomlagring.started
import no.nav.helse.wiremock.*
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
                .stubK9MellomlagringHealth()
                .stubPleiepengesoknadMottakHealth()
                .stubOppslagHealth()
                .stubLeggSoknadTilProsessering("v1/soknad")
                .stubK9Mellomlagring()
                .stubK9OppslagSoker()
                .stubK9OppslagBarn()
                .stubK9OppslagArbeidsgivere()

            val redisServer: RedisServer = RedisServer
                .newRedisServer()
                .started()

            val testArgs = TestConfiguration.asMap(
                port = 8082,
                wireMockServer = wireMockServer,
                redisServer = redisServer
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
