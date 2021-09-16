package no.nav.helse

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.ktor.config.*
import no.nav.helse.dusseldorf.ktor.auth.EnforceEqualsOrContains
import no.nav.helse.dusseldorf.ktor.auth.issuers
import no.nav.helse.dusseldorf.ktor.auth.withAdditionalClaimRules
import no.nav.helse.dusseldorf.ktor.core.getOptionalList
import no.nav.helse.dusseldorf.ktor.core.getRequiredList
import no.nav.helse.dusseldorf.ktor.core.getRequiredString
import java.net.URI
import java.time.Duration

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

    internal fun getSoknadMellomlagringTidTimer() = config.getRequiredString("nav.mellomlagring.tid_timer", false)

    internal fun getK9OppslagUrl() = URI(config.getRequiredString("nav.gateways.k9_oppslag_url", secret = false))

    internal fun getK9MellomlagringUrl() = URI(config.getRequiredString("nav.gateways.k9_mellomlagring_url", secret = false))

    internal fun getK9MellomlagringScopes() = getScopesFor("k9-mellomlagring-scope")

    internal fun getPleiepengesoknadMottakBaseUrl() = URI(config.getRequiredString("nav.gateways.pleiepengesoknad_mottak_base_url", secret = false))

    private fun getScopesFor(operation: String) = config.getRequiredList("nav.auth.scopes.$operation", secret = false, builder = { it }).toSet()

    internal fun getPleiepengesoknadMottakClientId() = getScopesFor("pleiepengesoknad-mottak-client-id")
    internal fun getRedisPort() = config.getRequiredString("nav.redis.port", secret = false).toInt()
    internal fun getRedisHost() = config.getRequiredString("nav.redis.host", secret = false)

    internal fun getStoragePassphrase(): String {
        return config.getRequiredString("nav.storage.passphrase", secret = true)
    }

    internal fun<K, V>cache(
        expiry: Duration = Duration.ofMinutes(config.getRequiredString("nav.cache.barn.expiry_in_minutes", secret = false).toLong())
    ) : Cache<K, V> {
        val maxSize = config.getRequiredString("nav.cache.barn.max_size", secret = false).toLong()
        return Caffeine.newBuilder()
            .expireAfterWrite(expiry)
            .maximumSize(maxSize)
            .build()
    }
}
