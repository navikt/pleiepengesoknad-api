package no.nav.helse

import io.ktor.config.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import no.nav.helse.dusseldorf.ktor.core.getOptionalList
import no.nav.helse.dusseldorf.ktor.core.getOptionalString
import no.nav.helse.dusseldorf.ktor.core.getRequiredList
import no.nav.helse.dusseldorf.ktor.core.getRequiredString
import no.nav.helse.general.auth.ApiGatewayApiKey
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URL

//TODO: Kan fjernes etter no/nav/helse/App.kt:108
private const val ISSUER = "issuer"
private const val JWKS_URI = "jwks_uri"

//TODO: Kan fjernes etter no/nav/helse/App.kt:108
private val jsonParser = JSONParser()
private val logger: Logger = LoggerFactory.getLogger("no.nav.helse.Configuration")

@KtorExperimentalAPI
data class Configuration(val config: ApplicationConfig) {

    private val discoveryJson =
        runBlocking { config.getRequiredString("nav.authorization.loginservice_discovery_url", false).discover(listOf(ISSUER, JWKS_URI)) }

    private val issuer = discoveryJson[ISSUER] as String
    private val jwksUrl = discoveryJson[JWKS_URI] as String

    //TODO: Kan fjernes etter no/nav/helse/App.kt:108
    internal fun getJwksUrl() = URI(jwksUrl)

    //TODO: Kan fjernes etter no/nav/helse/App.kt:108
    internal fun getIssuer(): String = issuer

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
    internal fun getRedisPort() = config.getOptionalString("nav.redis.port", secret = false)
    internal fun getRedisHost() = config.getOptionalString("nav.redis.host", secret = false)

    internal fun getStoragePassphrase(): String {
        return config.getRequiredString("nav.storage.passphrase", secret = true)
    }
}

//TODO: Kan fjernes etter no/nav/helse/App.kt:108
private fun String.discover(requiredAttributes: List<String>): JSONObject {
    val asText = URL(this).readText()
    val asJson = jsonParser.parse(asText) as JSONObject
    return if (asJson.containsKeys(requiredAttributes)) asJson else {
        throw IllegalStateException("Response fra Discovery Endpoint inneholdt ikke attributtene '[${requiredAttributes.joinToString()}]'. Response='$asText'\"")
    }
}

//TODO: Kan fjernes etter no/nav/helse/App.kt:108
private fun JSONObject.containsKeys(requiredAttributes: List<String>): Boolean {
    requiredAttributes.forEach {
        if (!containsKey(it)) return false
    }
    return true
}
