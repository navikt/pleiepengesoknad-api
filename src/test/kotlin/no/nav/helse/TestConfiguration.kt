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
        pleiepengesoknadProsesseringUrl : String? = wireMockServer?.getPleiepengesoknadProsesseringUrl(),
        pleiepengerDokumentUrl : String? = wireMockServer?.getPleiepengerDokumentUrl(),
        corsAdresses : String = "http://localhost:8080",
        issuer : String = "iss-localhost",
        cookieName : String = "localhost-idtoken"
    ) : Map<String, String>{
        return mapOf(
            Pair("ktor.deployment.port","$port"),
            Pair("nav.authorization.token_url","$tokenUrl"),
            Pair("nav.authorization.issuer", issuer),
            Pair("nav.authorization.cookie_name", cookieName),
            Pair("nav.authorization.jwks_uri","$jwkSetUrl"),
            Pair("nav.gateways.sparkel_url","$sparkelUrl"),
            Pair("nav.gateways.aktoer_register_url", "$aktoerRegisterBaseUrl"),
            Pair("nav.gateways.pleiepengesoknad_prosessering_base_url", "$pleiepengesoknadProsesseringUrl"),
            Pair("nav.cors.addresses", corsAdresses),
            Pair("nav.gateways.pleiepenger_dokument_url", "$pleiepengerDokumentUrl")
        )
    }

    fun asArray(map : Map<String, String>) : Array<String>  {
        val list = mutableListOf<String>()
        map.forEach { configKey, configValue ->
            list.add("-P:$configKey=$configValue")
        }
        return list.toTypedArray()
    }
}