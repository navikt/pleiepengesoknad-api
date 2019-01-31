package no.nav.helse

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import no.nav.helse.general.auth.ApiGatewayApiKey
import no.nav.helse.general.buildURL
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URL
import java.util.concurrent.TimeUnit

private val logger: Logger = LoggerFactory.getLogger("nav.Configuration")

@KtorExperimentalAPI
data class Configuration(val config : ApplicationConfig) {

    private fun getString(key: String,
                          secret: Boolean = false) : String  {
        val stringValue = config.property(key).getString()
        logger.info("{}={}", key, if (secret) "***" else stringValue)
        return stringValue
    }

    private fun <T>getListFromCsv(key: String,
                                  builder: (value: String) -> T) : List<T> {
        val csv = getString(key)
        val list = csv.replace(" ", "").split(",")
        val builtList = mutableListOf<T>()
        list.forEach { entry ->
            logger.info("$key entry = $entry")
            builtList.add(builder(entry))
        }
        return builtList.toList()

    }


    fun getJwksUrl() : URL {
        return URL(getString("nav.authorization.jwks_uri"))
    }

    fun getIssuer() : String {
        return getString("nav.authorization.issuer")
    }

    fun getCookieName() : String {
        return getString("nav.authorization.cookie_name")
    }

    fun getJwkCacheSize() : Long {
        return getString("nav.authorization.jwk_cache.size").toLong()
    }

    fun getJwkCacheExpiryDuration() : Long {
        return getString("nav.authorization.jwk_cache.expiry_duration").toLong()
    }

    fun getJwkCacheExpiryTimeUnit() : TimeUnit {
        return TimeUnit.valueOf(getString("nav.authorization.jwk_cache.expiry_time_unit"))
    }

    fun getJwsJwkRateLimitBucketSize() : Long {
        return getString("nav.authorization.jwk_rate_limit.bucket_size").toLong()
    }

    fun getJwkRateLimitRefillRate() : Long {
        return getString("nav.authorization.jwk_rate_limit.refill_rate").toLong()
    }

    fun getJwkRateLimitRefillTimeUnit() : TimeUnit {
        return TimeUnit.valueOf(getString("nav.authorization.jwk_rate_limit.refill_time_unit"))
    }

    fun getWhitelistedCorsAddreses() : List<URI> {
        return getListFromCsv(
            key = "nav.cors.addresses",
            builder = { value ->
                URI.create(value)
            }
        )
    }

    fun getSparkelUrl() : URL {
        return URL(getString("nav.gateways.sparkel_url"))
    }

    fun getSparkelReadinessUrl() : URL {
        return buildURL(baseUrl = getSparkelUrl(), pathParts = listOf("isready"))
    }

    fun getKafkaBootstrapServers() : String {
        return getString("nav.kafka.bootstrap_servers")
    }

    fun getKafkaUsername() : String {
        return getString("nav.kafka.username")
    }

    fun getKafkaPassword() : String {
        return getString(key = "nav.kafka.password", secret = true)
    }

    fun getServiceAccountUsername(): String {
        return getString("nav.authorization.service_account.username")
    }

    fun getServiceAccountPassword(): String {
        return getString(key = "nav.authorization.service_account.password", secret = true)
    }

    fun getServiceAccountScopes(): List<String> {
        return getListFromCsv(
                key = "nav.authorization.service_account.scopes",
                builder = { value -> value}
        )
    }

    fun getAuthorizationServerTokenUrl(): URL {
        return URL(getString("nav.authorization.token_url"))
    }

    fun getAktoerRegisterUrl(): URL {
        return URL(getString("nav.gateways.aktoer_register_url"))
    }

    fun getApiGatewayApiKey() : ApiGatewayApiKey {
        val apiKey = getString(key = "nav.authorization.api_gateway.api_key", secret = true)
        return ApiGatewayApiKey(value = apiKey)
    }

    fun logIndirectlyUsedConfiguration() {
        logger.info("# Indirectly used configuration")
        val properties = System.getProperties()
        logger.info("## System Properties")
        properties.forEach { key, value ->
            if (key is String && (key.startsWith(prefix = "http", ignoreCase = true) || key.startsWith(prefix = "https", ignoreCase = true))) {
                    logger.info("$key=$value")
            }
        }
        logger.info("## Environment variables")
        val environmentVariables = System.getenv()
        logger.info("HTTP_PROXY=${environmentVariables["HTTP_PROXY"]}")
        logger.info("HTTPS_PROXY=${environmentVariables["HTTPS_PROXY"]}")
        logger.info("NO_PROXY=${environmentVariables["NO_PROXY"]}")
    }
}