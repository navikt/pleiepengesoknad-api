package no.nav.helse

import com.github.kittinunf.fuel.httpGet
import com.github.tomakehurst.wiremock.WireMockServer
import no.nav.helse.dusseldorf.ktor.testsupport.jws.ClientCredentials
import no.nav.helse.dusseldorf.ktor.testsupport.wiremock.getAzureV2WellKnownUrl
import no.nav.helse.dusseldorf.ktor.testsupport.wiremock.getLoginServiceV1WellKnownUrl
import no.nav.helse.dusseldorf.ktor.testsupport.wiremock.getNaisStsWellKnownUrl
import no.nav.helse.wiremock.*
import org.json.JSONObject

object TestConfiguration {

    fun asMap(
        wireMockServer: WireMockServer? = null,
        port : Int = 8080,
        k9OppslagUrl: String? = wireMockServer?.getK9OppslagUrl(),
        aktoerRegisterBaseUrl : String? = wireMockServer?.getAktoerRegisterUrl(),
        pleiepengesoknadMottakUrl : String? = wireMockServer?.getPleiepengesoknadMottakUrl(),
        pleiepengerDokumentUrl : String? = wireMockServer?.getPleiepengerDokumentUrl(),
        corsAdresses : String = "http://localhost:8080"
    ) : Map<String, String> {

        val naisStsWellKnownJson = wireMockServer?.getNaisStsWellKnownUrl()?.getAsJson()
        val loginServiceWellKnownJson = wireMockServer?.getLoginServiceV1WellKnownUrl()?.getAsJson()

        val map = mutableMapOf(
            Pair("ktor.deployment.port","$port"),
            Pair("nav.authorization.issuer", "${loginServiceWellKnownJson?.getString("issuer")}"),
            Pair("nav.authorization.cookie_name", "localhost-idtoken"),
            Pair("nav.authorization.jwks_uri","${loginServiceWellKnownJson?.getString("jwks_uri")}"),
            Pair("nav.gateways.k9_oppslag_url","$k9OppslagUrl"),
            Pair("nav.gateways.aktoer_register_url", "$aktoerRegisterBaseUrl"),
            Pair("nav.gateways.pleiepengesoknad_mottak_base_url", "$pleiepengesoknadMottakUrl"),
            Pair("nav.gateways.pleiepenger_dokument_url", "$pleiepengerDokumentUrl"),
            Pair("nav.cors.addresses", corsAdresses),
            Pair("nav.authorization.api_gateway.api_key", "verysecret")
        )

        // Clients
        if (wireMockServer != null) {
            map["nav.auth.clients.0.alias"] = "nais-sts"
            map["nav.auth.clients.0.client_id"] = "srvpleiepengesokna"
            map["nav.auth.clients.0.client_secret"] = "very-secret"
            map["nav.auth.clients.0.token_endpoint"] = "${naisStsWellKnownJson?.getString("token_endpoint")}"
        }

        if (wireMockServer != null) {
            map["nav.auth.clients.1.alias"] = "azure-v2"
            map["nav.auth.clients.1.client_id"] = "pleiepengesoknad-api"
            map["nav.auth.clients.1.private_key_jwk"] = ClientCredentials.ClientA.privateKeyJwk
            map["nav.auth.clients.1.certificate_hex_thumbprint"] = ClientCredentials.ClientA.certificateHexThumbprint
            map["nav.auth.clients.1.discovery_endpoint"] = wireMockServer.getAzureV2WellKnownUrl()
            map["nav.auth.scopes.sende-soknad-til-prosessering"] = "pleiepengesoknad-mottak/.default"
        }

        return map.toMap()
    }

    private fun String.getAsJson() = JSONObject(this.httpGet().responseString().third.component1())
}