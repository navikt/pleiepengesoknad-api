package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.AnythingPattern

fun stubStsGetAccessToken() {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching(".*/authorization-server-mock/token.*"))
            .withHeader("x-nav-apiKey", AnythingPattern())
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                            {
                                "access_token": "i-am-a-access-token",
                                "token_type": "Bearer",
                                "expires_in": 1000
                            }
                        """.trimIndent())
            )
    )
}