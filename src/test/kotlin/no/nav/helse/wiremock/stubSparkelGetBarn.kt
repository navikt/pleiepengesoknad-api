package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.AnythingPattern

fun stubSparkelGetBarn(
    harBarn : Boolean = true
) {
    WireMock.stubFor(
        WireMock.get(WireMock.urlMatching(".*/sparkel-mock/api/person/.\\d+/barn"))
            .withHeader("x-nav-apiKey", AnythingPattern())
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(if (harBarn) sparkelResponseHarBarn else sparkelResponseIngenBarn)
            )
    )
}

private val sparkelResponseIngenBarn = """
{
    "barn": []
}
""".trimIndent()

private val sparkelResponseHarBarn = """
{
    "barn": [
        {
            "fdato": "2000-08-27",
            "statsborgerskap": "NOR",
            "mellomnavn": "EN",
            "etternavn": "BARNESEN",
            "aktørId": "1000000000001",
            "fornavn": "BARN",
            "kjønn": "MANN",
            "status": "BOSA"
        },
        {
            "fdato": "2001-04-10",
            "statsborgerskap": "NOR",
            "mellomnavn": "TO",
            "etternavn": "BARNESEN",
            "aktørId": "1000000000002",
            "fornavn": "BARN",
            "kjønn": "KVINNE",
            "status": "FØDR"
        },
                {
            "fdato": "2010-04-10",
            "statsborgerskap": "NOR",
            "mellomnavn": "TRE",
            "etternavn": "BARNESEN",
            "aktørId": "1000000000002",
            "fornavn": "BARN",
            "kjønn": "KVINNE",
            "status": "DØD"
        },
                {
            "fdato": "2010-04-10",
            "statsborgerskap": "NOR",
            "mellomnavn": "FIRE",
            "etternavn": "BARNESEN",
            "aktørId": "1000000000002",
            "fornavn": "BARN",
            "kjønn": "KVINNE",
            "status": "DØD"
        }
    ]
}
""".trimIndent()