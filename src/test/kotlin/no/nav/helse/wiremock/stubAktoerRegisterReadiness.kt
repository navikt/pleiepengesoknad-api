package no.nav.helse.wiremock

import com.github.tomakehurst.wiremock.client.WireMock

fun stubAktoerRegisterReadiness() {
    WireMock.stubFor(
        WireMock.get(WireMock.urlMatching(".*/aktoer-register-mock/internal/isAlive"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
            )
    )
}