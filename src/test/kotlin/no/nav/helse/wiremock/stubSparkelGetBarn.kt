package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.client.WireMock

fun stubSparkelGetBarn() {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching("/sparkel-mock/id/.*/barn"))
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
                "fodselsdato": "1990-09-29"
            },{
                "fornavn": "George",
                "etternavn": "Costanza",
                "fodselsdato": "1966-06-06"
            }]
        }
    """.trimIndent()
}