package no.nav.helse

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.ktor.config.*
import no.nav.helse.dusseldorf.ktor.auth.EnforceEqualsOrContains
import no.nav.helse.dusseldorf.ktor.auth.issuers
import no.nav.helse.dusseldorf.ktor.auth.withAdditionalClaimRules
import no.nav.helse.dusseldorf.ktor.core.getOptionalList
import no.nav.helse.dusseldorf.ktor.core.getOptionalString
import no.nav.helse.dusseldorf.ktor.core.getRequiredList
import no.nav.helse.dusseldorf.ktor.core.getRequiredString
import no.nav.helse.kafka.KafkaConfig
import java.net.URI
import java.time.Duration

data class Configuration(val config: ApplicationConfig) {

    private val loginServiceClaimRules = setOf(
        EnforceEqualsOrContains("acr", "Level4")
    )

    enum class Miljø { PROD, DEV, LOCAL }

    internal fun miljø(): Miljø = when(config.getRequiredString("NAIS_CLUSTER_NAME", false)){
        "prod-gcp" -> Miljø.PROD
        "dev-gcp" -> Miljø.DEV
        else -> Miljø.LOCAL
    }

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

    internal fun getSoknadMellomlagringTidTimer() = config.getRequiredString("nav.mellomlagring.søknad_tid_timer", false)
    internal fun getEndringsmeldingMellomlagringTidTimer() = config.getRequiredString("nav.mellomlagring.endringsmelding_tid_timer", false)

    internal fun getK9OppslagUrl() = URI(config.getRequiredString("nav.gateways.k9_oppslag_url", secret = false))

    internal fun getSifInnsynApiUrl() = URI(config.getRequiredString("nav.gateways.sif_innsyn_api_url", secret = false))

    internal fun getK9MellomlagringUrl() = URI(config.getRequiredString("nav.gateways.k9_mellomlagring_url", secret = false))
    internal fun getK9MellomlagringScopes() = getScopesFor("k9-mellomlagring-scope")

    internal fun getK9BrukerdialogCacheUrl() = URI(config.getRequiredString("nav.gateways.k9_brukerdialog_cache_url", secret = false))
    internal fun getK9BrukerdialogCacheTokenxAudience() = getScopesFor("k9-brukerdialog-cache-tokenx-audience")

    private fun getScopesFor(operation: String) = config.getRequiredList("nav.auth.scopes.$operation", secret = false, builder = { it }).toSet()

    internal fun<K, V>cache(
        expiry: Duration = Duration.ofMinutes(config.getRequiredString("nav.cache.barn.expiry_in_minutes", secret = false).toLong())
    ) : Cache<K, V> {
        val maxSize = config.getRequiredString("nav.cache.barn.max_size", secret = false).toLong()
        return Caffeine.newBuilder()
            .expireAfterWrite(expiry)
            .maximumSize(maxSize)
            .build()
    }

    internal fun getKafkaConfig() = config.getRequiredString("nav.kafka.bootstrap_servers", secret = false).let { bootstrapServers ->
        val trustStore =
            config.getOptionalString("nav.kafka.truststore_path", secret = false)?.let { trustStorePath ->
                config.getOptionalString("nav.kafka.credstore_password", secret = true)?.let { credstorePassword ->
                    Pair(trustStorePath, credstorePassword)
                }
            }

        val keyStore = config.getOptionalString("nav.kafka.keystore_path", secret = false)?.let { keystorePath ->
            config.getOptionalString("nav.kafka.credstore_password", secret = true)?.let { credstorePassword ->
                Pair(keystorePath, credstorePassword)
            }
        }

        KafkaConfig(
            bootstrapServers = bootstrapServers,
            trustStore = trustStore,
            keyStore = keyStore
        )
    }
}
