package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.client.WireMock

fun stubSparkelGetSoker() {
    WireMock.stubFor(
        WireMock.get(WireMock.urlMatching(".*/sparkel-mock/api/person/.*"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json;charset=UTF-8")
                    .withBody(sparkelResponse)
            )
    )
}

private val sparkelResponse = """
    {
        "fdato": "1997-05-25",
        "etternavn": "MORSEN",
        "mellomnavn": "HEISANN",
        "id": {
            "aktor": "1060877738241"
        },
        "fornavn": "MOR",
        "kjønn": "KVINNE"
    }
""".trimIndent()

fun expectedGetSokerJson(fodselsnummer: String) = """
    {
        "etternavn": "MORSEN",
        "fornavn": "MOR",
        "mellomnavn": "HEISANN",
        "kjonn": "kvinne",
        "fodselsnummer": "$fodselsnummer"
    }
""".trimIndent()
