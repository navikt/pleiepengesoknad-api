package no.nav.helse

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import no.nav.helse.dusseldorf.ktor.core.getOptionalList
import no.nav.helse.dusseldorf.ktor.core.getRequiredString
import no.nav.helse.general.auth.ApiGatewayApiKey
import java.net.URI
import java.net.URL

@KtorExperimentalAPI
data class Configuration(val config : ApplicationConfig) {
    fun getJwksUrl() : URL {
        return URL(config.getRequiredString("nav.authorization.jwks_uri", secret = false))
    }

    fun getIssuer() : String {
        return config.getRequiredString("nav.authorization.issuer", secret = false)
    }

    fun getCookieName() : String {
        return config.getRequiredString("nav.authorization.cookie_name", secret = false)
    }

    fun getWhitelistedCorsAddreses() : List<URI> {
        return config.getOptionalList(
            key = "nav.cors.addresses",
            builder = { value ->
                URI.create(value)
            },
            secret = false
        )
    }

    fun getSparkelUrl() : URL {
        return URL(config.getRequiredString("nav.gateways.sparkel_url", secret = false))
    }

    fun getServiceAccountClientId(): String {
        return config.getRequiredString("nav.authorization.service_account.client_id", secret = false)
    }

    fun getServiceAccountClientSecret(): String {
        return config.getRequiredString("nav.authorization.service_account.client_secret", secret = true)
    }

    fun getServiceAccountScopes(): List<String> {
        return config.getOptionalList(
            key = "nav.authorization.service_account.scopes",
            builder = { value -> value },
            secret = false
        )
    }

    fun getAuthorizationServerTokenUrl(): URL {
        return URL(config.getRequiredString("nav.authorization.token_url", secret = false))
    }

    fun getAktoerRegisterUrl(): URL {
        return URL(config.getRequiredString("nav.gateways.aktoer_register_url", secret = false))
    }

    fun getPleiepengerDokumentUrl(): URL {
        return URL(config.getRequiredString("nav.gateways.pleiepenger_dokument_url", secret = false))
    }

    fun getPleiepengesoknadProsesseringBaseUrl(): URL {
        return URL(config.getRequiredString("nav.gateways.pleiepengesoknad_prosessering_base_url", secret = false))
    }

    fun getApiGatewayApiKey() : ApiGatewayApiKey {
        val apiKey = config.getRequiredString(key = "nav.authorization.api_gateway.api_key", secret = true)
        return ApiGatewayApiKey(value = apiKey)
    }
}