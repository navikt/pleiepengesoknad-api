package no.nav.helse

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import no.nav.helse.dusseldorf.ktor.core.getOptionalList
import no.nav.helse.dusseldorf.ktor.core.getRequiredList
import no.nav.helse.dusseldorf.ktor.core.getRequiredString
import no.nav.helse.general.auth.ApiGatewayApiKey
import java.net.URI

@KtorExperimentalAPI
data class Configuration(val config : ApplicationConfig) {
    internal fun getJwksUrl() = URI(config.getRequiredString("nav.authorization.jwks_uri", secret = false))

    internal fun getIssuer() : String {
        return config.getRequiredString("nav.authorization.issuer", secret = false)
    }

    internal fun getCookieName() : String {
        return config.getRequiredString("nav.authorization.cookie_name", secret = false)
    }

    internal fun getWhitelistedCorsAddreses() : List<URI> {
        return config.getOptionalList(
            key = "nav.cors.addresses",
            builder = { value ->
                URI.create(value)
            },
            secret = false
        )
    }

    internal fun getK9OppslagUrl() = URI(config.getRequiredString("nav.gateways.k9_oppslag_url", secret = false))

    internal fun getAktoerRegisterUrl() = URI(config.getRequiredString("nav.gateways.aktoer_register_url", secret = false))

    internal fun getPleiepengerDokumentUrl() = URI(config.getRequiredString("nav.gateways.pleiepenger_dokument_url", secret = false))

    internal fun getPleiepengesoknadMottakBaseUrl() = URI(config.getRequiredString("nav.gateways.pleiepengesoknad_mottak_base_url", secret = false))

    internal fun getApiGatewayApiKey() : ApiGatewayApiKey {
        val apiKey = config.getRequiredString(key = "nav.authorization.api_gateway.api_key", secret = true)
        return ApiGatewayApiKey(value = apiKey)
    }

    private fun getScopesFor(operation: String) = config.getRequiredList("nav.auth.scopes.$operation", secret = false, builder = { it }).toSet()
    internal fun getSendSoknadTilProsesseringScopes() = getScopesFor("sende-soknad-til-prosessering")
}