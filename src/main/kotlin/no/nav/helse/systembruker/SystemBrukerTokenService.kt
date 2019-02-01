package no.nav.helse.systembruker

import no.nav.helse.monitorering.Readiness
import no.nav.helse.monitorering.ReadinessResult
import java.time.LocalDateTime

class SystemBrukerTokenService(
    private val systemBrukerTokenGateway: SystemBrukerTokenGateway
) : Readiness {

    override suspend fun getResult(): ReadinessResult {
        return try {
            getToken()
            ReadinessResult(isOk = true, message = "Henting av Systembruker Access Token OK")
        } catch (cause: Throwable) {
            ReadinessResult(isOk = false, message = "${cause.message}")
        }
    }

    @Volatile private var cachedToken: String? = null
    @Volatile private var expiry: LocalDateTime? = null

    private suspend fun getToken() : String {
        if (hasCachedToken() && isCachedTokenValid()) {
            return cachedToken!!
        }

        clearCachedData()

        val response = systemBrukerTokenGateway.getToken()
        setCachedData(response)
        return cachedToken!!
    }

    suspend fun getAuthorizationHeader() : String {
        return "Bearer ${getToken()}"
    }

    private fun setCachedData(response: Response) {
        cachedToken = response.accessToken
        expiry = LocalDateTime.now()
            .plusSeconds(response.expiresIn)
            .minusSeconds(10L)
    }

    private fun clearCachedData() {
        cachedToken = null
        expiry = null
    }

    private fun hasCachedToken() : Boolean {
        return cachedToken != null && expiry != null
    }

    private fun isCachedTokenValid() : Boolean {
        return expiry!!.isAfter(LocalDateTime.now())
    }
}