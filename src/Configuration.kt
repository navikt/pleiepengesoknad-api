package no.nav.pleiepenger.api

import io.ktor.config.ApplicationConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URL
import java.util.concurrent.TimeUnit

private val logger: Logger = LoggerFactory.getLogger("nav.Configuration")


data class Configuration(val config : ApplicationConfig) {

    private fun getString(key: String) : String  {
        val stringValue = config.property(key).getString()
        logger.info("{}={}", key, stringValue)
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
}