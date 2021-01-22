package no.nav.helse

import io.ktor.config.*
import io.ktor.util.*
import no.nav.helse.dusseldorf.ktor.auth.EnforceEqualsOrContains
import no.nav.helse.dusseldorf.ktor.auth.issuers
import no.nav.helse.dusseldorf.ktor.auth.withAdditionalClaimRules
import no.nav.helse.dusseldorf.ktor.core.getOptionalList
import no.nav.helse.dusseldorf.ktor.core.getOptionalString
import no.nav.helse.dusseldorf.ktor.core.getRequiredList
import no.nav.helse.dusseldorf.ktor.core.getRequiredString
import no.nav.helse.general.auth.ApiGatewayApiKey
import java.net.URI

@KtorExperimentalAPI
data class Configuration(val config: ApplicationConfig) {

    private val loginServiceClaimRules = setOf(
        EnforceEqualsOrContains("acr", "Level4")
    )

    internal fun issuers() = config.issuers().withAdditionalClaimRules(mapOf(
        "login-service-v1" to loginServiceClaimRules,
        "login-service-v2" to loginServiceClaimRules
    ))

    internal fun getCookieName(): String {
        return config.getRequiredString("nav.authorization.cookie_name", secret = false)
    }

    internal fun getWhitelistedCorsAddreses(): List<URI> {
        return config.getOptionalList(
            key = "nav.cors.addresses",
            builder = { value ->
                URI.create(value)
            },
            secret = false
        )
    }

    internal fun getK9OppslagUrl() = URI(config.getRequiredString("nav.gateways.k9_oppslag_url", secret = false))

    internal fun getK9DokumentUrl() = URI(config.getRequiredString("nav.gateways.k9_dokument_url", secret = false))

    internal fun getPleiepengesoknadMottakBaseUrl() =
        URI(config.getRequiredString("nav.gateways.pleiepengesoknad_mottak_base_url", secret = false))

    internal fun getApiGatewayApiKey(): ApiGatewayApiKey {
        val apiKey = config.getRequiredString(key = "nav.authorization.api_gateway.api_key", secret = true)
        return ApiGatewayApiKey(value = apiKey)
    }

    private fun getScopesFor(operation: String) =
        config.getRequiredList("nav.auth.scopes.$operation", secret = false, builder = { it }).toSet()

    internal fun getSendSoknadTilProsesseringScopes() = getScopesFor("sende-soknad-til-prosessering")
    internal fun getRedisPort() = config.getRequiredString("nav.redis.port", secret = false).toInt()
    internal fun getRedisHost() = config.getRequiredString("nav.redis.host", secret = false)

    internal fun getStoragePassphrase(): String {
        return config.getRequiredString("nav.storage.passphrase", secret = true)
    }
}
