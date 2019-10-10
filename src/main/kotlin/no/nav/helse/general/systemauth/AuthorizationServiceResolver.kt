package no.nav.helse.general.systemauth

import no.nav.helse.dusseldorf.ktor.auth.Client
import no.nav.helse.dusseldorf.ktor.auth.ClientSecretClient
import no.nav.helse.dusseldorf.ktor.health.HealthCheck
import no.nav.helse.dusseldorf.ktor.health.Healthy
import no.nav.helse.dusseldorf.ktor.health.Result
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import no.nav.helse.dusseldorf.oauth2.client.CachedAccessTokenClient
import no.nav.helse.general.auth.ApiGatewayApiKey
import org.slf4j.Logger
import org.slf4j.LoggerFactory

// Når Azure blir tatt i bruk her trengs et nytt parameter private val scopes : Map<String, Set<String>>
internal class AuthorizationServiceResolver(
    clients : Map<String, Client>,
    apiGatewayApiKey: ApiGatewayApiKey
) : HealthCheck {
    private companion object {
        private const val NAIS_STS_ALIAS = "nais-sts"
        private val logger: Logger = LoggerFactory.getLogger(AuthorizationServiceResolver::class.java)
    }

    private val naisStsClient = clients.getOrElse(NAIS_STS_ALIAS) {
        throw IllegalStateException("Client[$NAIS_STS_ALIAS] må være satt opp.")
    } as ClientSecretClient

    private val naisStsAccessTokenClient = NaisStsAccessTokenClient(
        clientId = naisStsClient.clientId(),
        clientSecret = naisStsClient.clientSecret,
        tokenEndpoint = naisStsClient.tokenEndpoint(),
        apiGatewayApiKey = apiGatewayApiKey
    )

    private val cachedNaisStsAccessTokenClient = CachedAccessTokenClient(naisStsAccessTokenClient)
    private val naisStsAuthorizationService = AuthorizationService(cachedNaisStsAccessTokenClient, setOf("openid"))

    internal fun aktoerRegister() = naisStsAuthorizationService
    internal fun pleiepengesoknaMottak() = naisStsAuthorizationService

    override suspend fun check(): Result {
        return try {
            naisStsAuthorizationService.getAuthorizationHeader()
            Healthy("AuthorizationServiceResolver","Healthy!")
        } catch (cause: Throwable) {
            logger.error("AuthorizationServiceResolver helsesjekk feilet.", cause)
            UnHealthy("AuthorizationServiceResolver","Unhealthy!")
        }
    }
}