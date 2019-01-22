package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.client.WireMock

fun stubSparkelGetBarn() {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching("/sparkel-mock/barn/.*"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(body())
            )
    )
}

private fun body() : String {
    return """
        {
            "barn" : [{
                "fornavn": "Santa",
                "mellomnavn": "Claus",
                "etternavn": "Winter",
                "fodselsnummer": "29099012345"
            },{
                "fornavn": "George",
                "etternavn": "Costanza",
                "fodselsnummer": "29099052345"
            }]
        }
    """.trimIndent()
}