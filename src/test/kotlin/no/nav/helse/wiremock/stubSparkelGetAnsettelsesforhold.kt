package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.client.WireMock

fun stubSparkelGetAnsettelsesforhold() {
    WireMock.stubFor(
        WireMock.get(WireMock.urlMatching(".*/sparkel-mock/api/arbeidsforhold/.*"))
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
        "organisasjoner": [{
            "organisasjonsnummer": "913548221",
            "navn": "EQUINOR AS, AVD STATOIL SOKKELVIRKSOMHET"
        },{
            "organisasjonsnummer": "984054564",
            "navn": "NAV, AVD WALDEMAR THRANES GATE"
        },{
            "organisasjonsnummer": "984054564",
            "navn": "NAV, AVD WALDEMAR THRANES GATE"
        }]
    }
""".trimIndent()

// Er 3 entries i response fra Sparkel. Men 2 er samme organisasjon, dermed er det bare 2 i expected.
val expectedGetAnsettelsesforholdJson = """
    {
        "organisasjoner": [{
            "organisasjonsnummer": "913548221",
            "navn": "EQUINOR AS, AVD STATOIL SOKKELVIRKSOMHET"
        },{
            "organisasjonsnummer": "984054564",
            "navn": "NAV, AVD WALDEMAR THRANES GATE"
        }]
    }
""".trimIndent()
