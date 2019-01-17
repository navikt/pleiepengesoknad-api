package no.nav.helse

import io.ktor.config.ApplicationConfig
import io.ktor.http.*
import io.ktor.util.KtorExperimentalAPI
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URL
import java.util.concurrent.TimeUnit

private val logger: Logger = LoggerFactory.getLogger("nav.Configuration")


@KtorExperimentalAPI
data class Configuration(val config : ApplicationConfig) {

    private fun getString(key: String) : String  {
        val stringValue = config.property(key).getString()
        logger.info("{}={}", key, if (key.contains("password")) "***" else stringValue)
        return stringValue
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
        val corsAdressesString : List<String> = config.property("nav.cors.addresses").getList()
        val corsAddresses : MutableList<URI> = mutableListOf()
        logger.info("nav.cors.addresses")
        corsAdressesString.forEach {
            logger.info(it)
            corsAddresses.add(URI(it))
        }
        return corsAddresses.toList()
    }

    fun getSparkelUrl() : Url {
        return Url(getString("nav.gateways.sparkel_url"))
    }

    fun getSparkelReadinessUrl() : URL {
        val sparkelUrl = getSparkelUrl()
        val url = URLBuilder()
            .takeFrom(sparkelUrl)
            .path(sparkelUrl.fullPath, "isready")
            .build()
        return url.toURI().toURL()

    }

    fun getKafkaBootstrapServers() : String {
        return getString("nav.kafka.bootstrap_servers")
    }

    fun getKafkaUsername() : String {
        return getString("nav.kafka.username")
    }

    fun getKafkaPassword() : String {
        return getString("nav.kafka.password")
    }

    fun getServiceAccountUsername(): String {
        return getString("nav.authorization.service_account.username")
    }

    fun getServiceAccountPassword(): String {
        return getString("nav.authorization.service_account.password")
    }

    fun getServiceAccountScopes(): List<String> {
        val scopes : List<String> = config.property("nav.authorization.service_account.scopes").getList()
        logger.info("nav.authorization.service_account.scopes")
        scopes.forEach {
            logger.info(it)
        }
        return scopes
    }

    fun getAuthorizationServerTokenUrl(): URL {
        return URL(getString("nav.authorization.token_url"))
    }
}