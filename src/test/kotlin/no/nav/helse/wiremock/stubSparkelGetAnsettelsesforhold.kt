package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.client.WireMock

fun stubSparkelGetAnsettelsesforhold() {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching("/sparkel-mock/id/.*/ansettelsesforhold"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(getAnsettelsesforholdMockBody())
            )
    )
}

fun getAnsettelsesforholdMockBody() : String {
    return """
        {
            "ansettelsesforhold" : [{
                "navn": "Byggmax",
                "organisasjonsnummer": "9848541789"
            },{
                "navn": "Onecall",
                "organisasjonsnummer": "9848541782"
            }]
        }
    """.trimIndent()
}