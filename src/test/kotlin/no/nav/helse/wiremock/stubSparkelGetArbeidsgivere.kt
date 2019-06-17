package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import io.ktor.http.HttpHeaders

fun stubSparkelGetArbeidsgivere() {
    WireMock.stubFor(
        WireMock.get(WireMock.urlMatching(".*/sparkel-mock/api/arbeidsgivere/.*"))
            .withHeader("x-nav-apiKey", AnythingPattern())
            .withHeader(HttpHeaders.Authorization, AnythingPattern())
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(sparkelResponse)
            )
    )
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