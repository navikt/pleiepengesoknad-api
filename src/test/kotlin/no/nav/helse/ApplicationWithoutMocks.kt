package no.nav.helse

import io.ktor.server.testing.withApplication
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("nav.ApplicationWithoutMocks")

/**
 *  - Mer leslig loggformat
 *  - Setter proxy settings
 *  - Starter på annen port
 */
class ApplicationWithoutMocks {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {

            System.setProperty("http.nonProxyHosts", "localhost,api-gw-q1.oera.no")
            System.setProperty("http.proxyHost", "127.0.0.1")
            System.setProperty("http.proxyPort", "5001")
            System.setProperty("https.proxyHost", "127.0.0.1")
            System.setProperty("https.proxyPort", "5001")

            // Verdier som må settes utenom det som er satt i denne klassen:
            // -Dnav.authorization.service_account.client_secret=
            // -Dnav.authorization.api_gateway.api_key=

            val q1Args = TestConfiguration.asArray(TestConfiguration.asMap(
                port = 8083,
                tokenUrl = "https://api-gw-q1.oera.no/helse-reverse-proxy/security-token-service/rest/v1/sts/token",
                jwkSetUrl = "https://login.microsoftonline.com/navtestb2c.onmicrosoft.com/discovery/v2.0/keys?p=b2c_1a_idporten_ver1",
                issuer = "https://login.microsoftonline.com/d38f25aa-eab8-4c50-9f28-ebf92c1256f2/v2.0/",
                cookieName = "selvbetjening-idtoken",
                aktoerRegisterBaseUrl = "https://api-gw-q1.oera.no/helse-reverse-proxy/aktoer-register",
                sparkelUrl = "https://api-gw-q1.oera.no/helse-reverse-proxy/sparkel",
                pleiepengesoknadProsesseringUrl = "https://api-gw-q1.oera.no/helse-reverse-proxy/pleiepengesoknad-prosessering",
                pleiepengerDokumentUrl = "https://pleiepenger-dokument.nais.oera-q.local"
            ))

            withApplication { no.nav.helse.main(q1Args) }
        }
    }
}