package no.nav.helse.general.systemauth

import no.nav.helse.dusseldorf.ktor.auth.Client
import no.nav.helse.dusseldorf.ktor.auth.ClientSecretClient
import no.nav.helse.dusseldorf.ktor.auth.PrivateKeyClient
import no.nav.helse.dusseldorf.oauth2.client.FromCertificateHexThumbprint
import no.nav.helse.dusseldorf.oauth2.client.FromJwk
import no.nav.helse.dusseldorf.oauth2.client.SignedJwtAccessTokenClient
import no.nav.helse.general.auth.ApiGatewayApiKey
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class AccessTokenClientResolver(
    clients : Map<String, Client>,
    apiGatewayApiKey: ApiGatewayApiKey

) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(AccessTokenClientResolver::class.java)
        private const val NAIS_STS_ALIAS = "nais-sts"
        private const val AZURE_V2_ALIAS = "azure-v2"
    }

    private val naisStsClient = clients.getOrElse(NAIS_STS_ALIAS) {
        throw IllegalStateException("Client[$NAIS_STS_ALIAS] må være satt opp.")
    } as ClientSecretClient

    private val azureV2Client = clients.getOrElse(AZURE_V2_ALIAS) {
        throw IllegalStateException("Client[$AZURE_V2_ALIAS] må være satt opp.")
    } as PrivateKeyClient

    private val naisStsAccessTokenClient = NaisStsAccessTokenClient(
        clientId = naisStsClient.clientId(),
        clientSecret = naisStsClient.clientSecret,
        tokenEndpoint = naisStsClient.tokenEndpoint(),
        apiGatewayApiKey = apiGatewayApiKey
    )

    private val azureV2AccessTokenClient = SignedJwtAccessTokenClient(
        clientId = azureV2Client.clientId(),
        tokenEndpoint = azureV2Client.tokenEndpoint(),
        privateKeyProvider = FromJwk(azureV2Client.privateKeyJwk),
        keyIdProvider = FromCertificateHexThumbprint(azureV2Client.certificateHexThumbprint)
    )

    internal fun aktoerRegister() = naisStsAccessTokenClient
    internal fun sparkel() = naisStsAccessTokenClient
    internal fun pleiepengesoknaMottak() = azureV2AccessTokenClient
}