package no.nav.helse.general.systemauth

import no.nav.helse.dusseldorf.oauth2.client.CachedAccessTokenClient

class AuthorizationService (
    private val accessTokenClient : CachedAccessTokenClient,
    private val scopes : Set<String>
) {
    internal fun getAuthorizationHeader() = accessTokenClient.getAccessToken(scopes).asAuthoriationHeader()
}