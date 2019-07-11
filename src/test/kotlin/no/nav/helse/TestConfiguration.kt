package no.nav.helse

import com.github.tomakehurst.wiremock.WireMockServer
import no.nav.helse.wiremock.*

object TestConfiguration {

    fun asMap(
        wireMockServer: WireMockServer? = null,
        port : Int = 8080,
        jwkSetUrl : String? = wireMockServer?.getJwksUri(),
        tokenUrl : String? = wireMockServer?.getAuthorizationTokenUrl(),
        sparkelUrl: String? = wireMockServer?.getSparkelUrl(),
        aktoerRegisterBaseUrl : String? = wireMockServer?.getAktoerRegisterUrl(),
        pleiepengesoknadMottakUrl : String? = wireMockServer?.getPleiepengesoknadMottakUrl(),
        pleiepengerDokumentUrl : String? = wireMockServer?.getPleiepengerDokumentUrl(),
        corsAdresses : String = "http://localhost:8080",
        issuer : String = "iss-localhost",
        cookieName : String = "localhost-idtoken",
        apiGatewayKey : String? = "foo",
        clientSecret : String? = "bar"
    ) : Map<String, String> {

        val map = mutableMapOf(
            Pair("ktor.deployment.port","$port"),
            Pair("nav.auth.clients.0.alias", "nais-sts"),
            Pair("nav.auth.clients.0.client_id", "srvpleiepengesokna"),
            Pair("nav.auth.clients.0.token_endpoint", "$tokenUrl"),
            Pair("nav.authorization.issuer", issuer),
            Pair("nav.authorization.cookie_name", cookieName),
            Pair("nav.authorization.jwks_uri","$jwkSetUrl"),
            Pair("nav.gateways.sparkel_url","$sparkelUrl"),
            Pair("nav.gateways.aktoer_register_url", "$aktoerRegisterBaseUrl"),
            Pair("nav.gateways.pleiepengesoknad_mottak_base_url", "$pleiepengesoknadMottakUrl"),
            Pair("nav.cors.addresses", corsAdresses),
            Pair("nav.gateways.pleiepenger_dokument_url", "$pleiepengerDokumentUrl")
        )

        if (apiGatewayKey != null) map["nav.authorization.api_gateway.api_key"] = apiGatewayKey
        if (clientSecret != null) map["nav.auth.clients.0.client_secret"] = clientSecret

        return map.toMap()
    }

    fun asArray(map : Map<String, String>) : Array<String>  {
        val list = mutableListOf<String>()
        map.forEach { configKey, configValue ->
            list.add("-P:$configKey=$configValue")
        }
        return list.toTypedArray()
    }
}