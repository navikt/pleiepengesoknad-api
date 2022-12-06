package no.nav.helse.soknad

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import no.nav.helse.dusseldorf.ktor.core.DefaultProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Duration

class InnsendingCache(expireMinutes: Long) {

    private val cache: Cache<String, String> = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(Duration.ofMinutes(expireMinutes))
        .evictionListener<String, String> { _, _, _ -> logger.info("Tømmer cache for ugått innsending.") }
        .build()

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @kotlin.jvm.Throws(Throwblem::class)
    fun put(key: String) {
        if (duplikatEksisterer(key)) {
            throw Throwblem(DuplikatInnsendingProblem())
        }
        cache.put(key, "") // verdi er ikke relevant. Vi er kun interessert i key.
    }

    private fun duplikatEksisterer(key: String): Boolean = when (cache.getIfPresent(key)) {
        null -> false
        else -> true
    }
}

class DuplikatInnsendingProblem: DefaultProblemDetails(
    title = "Duplikat innsending",
    type = URI("/problem-details/duplikat-innsendin"),
    status = 400,
    detail = "Det ble funnet en eksisterende innsending på søker med samme ytelse.",
    instance = URI("")
)
