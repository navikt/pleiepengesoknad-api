package no.nav.helse

import io.ktor.server.testing.withApplication
import no.nav.helse.wiremock.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("nav.ApplicationWithMocks")

class ApplicationWithMocks {


    companion object {

        @JvmStatic
        fun main(args: Array<String>) {

            val wireMockSerer = bootstrapWiremock(8081)

            val everythingMocked = TestConfiguration.asArray(
                TestConfiguration.asMap(
                    port = 8082,
                    wireMockServer = wireMockSerer
                )
            )

            // Verdier som må settes utenom det som er satt i denne klassen med q1OnlyMockLogin:
            // -Dnav.authorization.service_account.client_secret=
            // -Dnav.authorization.api_gateway.api_key=

            val q1OnlyMockLogin = TestConfiguration.asArray(TestConfiguration.asMap(
                wireMockServer = wireMockSerer,
                port = 8082,
                tokenUrl = "https://api-gw-q1.oera.no/helse-reverse-proxy/security-token-service/rest/v1/sts/token",
                aktoerRegisterBaseUrl = "https://api-gw-q1.oera.no/helse-reverse-proxy/aktoer-register",
                sparkelUrl = "https://api-gw-q1.oera.no/helse-reverse-proxy/sparkel",
                pleiepengesoknadProsesseringUrl = "https://api-gw-q1.oera.no/helse-reverse-proxy/pleiepengesoknad-prosessering",
                pleiepengerDokumentUrl = "https://pleiepenger-dokument.nais.oera-q.local"
            ))

            Runtime.getRuntime().addShutdownHook(object : Thread() {
                override fun run() {
                    logger.info("Tearing down")
                    wireMockSerer.stop()
                    logger.info("Tear down complete")
                }
            })

            withApplication { no.nav.helse.main(everythingMocked) }
        }
    }
}