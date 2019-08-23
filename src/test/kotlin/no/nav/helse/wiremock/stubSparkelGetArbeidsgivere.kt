package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import io.ktor.http.HttpHeaders

internal fun WireMockServer.stubSparkelGetArbeidsgivere(
    simulerFeil: Boolean = false
) : WireMockServer {
    WireMock.stubFor(
        WireMock.get(WireMock.urlMatching(".*$sparkelPath/api/arbeidsgivere/.*"))
            .withHeader("x-nav-apiKey", AnythingPattern())
            .withHeader(HttpHeaders.Authorization, AnythingPattern())
            .willReturn(
                WireMock.aResponse()
                    .withStatus(if (simulerFeil) 500 else 200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(sparkelResponse)
            )
    )
    return this
}

private val sparkelResponse = """
    {
        "arbeidsgivere": [{
            "orgnummer": "913548221",
            "navn": "EQUINOR AS, AVD STATOIL SOKKELVIRKSOMHET ÆØÅ"
        },{
            "orgnummer": "984054564",
            "navn": "NAV, AVD WALDEMAR THRANES GATE"
        },{
            "orgnummer": "984054564",
            "navn": "NAV, AVD WALDEMAR THRANES GATE"
        }]
    }
""".trimIndent()