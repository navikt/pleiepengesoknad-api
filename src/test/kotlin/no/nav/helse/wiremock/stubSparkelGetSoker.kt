package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.AnythingPattern

fun stubSparkelGetSoker(
    fodselsdato : String = "1997-05-25"
) {
    WireMock.stubFor(
        WireMock.get(WireMock.urlMatching(".*/sparkel-mock/api/person/.*"))
            .withHeader("x-nav-apiKey", AnythingPattern())
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(sparkelResponse(fodselsdato))
            )
    )
}

private fun sparkelResponse(fodselsdato: String) : String = """
    {
        "fdato": "$fodselsdato",
        "etternavn": "MORSEN",
        "mellomnavn": "HEISANN",
        "id": {
            "aktor": "1060877738241"
        },
        "fornavn": "MOR",
        "kjønn": "KVINNE"
    }
""".trimIndent()

fun expectedGetSokerJson(
    fodselsnummer: String,
    fodselsdato: String = "1997-05-25",
    myndig : Boolean = true) = """
    {
        "etternavn": "MORSEN",
        "fornavn": "MOR",
        "mellomnavn": "HEISANN",
        "fodselsnummer": "$fodselsnummer",
        "fodselsdato": "$fodselsdato",
        "myndig": $myndig
    }
""".trimIndent()

