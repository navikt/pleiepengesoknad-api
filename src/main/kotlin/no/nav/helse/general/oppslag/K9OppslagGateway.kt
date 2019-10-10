package no.nav.helse.general.oppslag

import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.httpGet
import io.ktor.http.HttpHeaders
import no.nav.helse.aktoer.NorskIdent
import no.nav.helse.dusseldorf.ktor.health.HealthCheck
import no.nav.helse.dusseldorf.oauth2.client.AccessTokenClient
import no.nav.helse.dusseldorf.oauth2.client.CachedAccessTokenClient
import no.nav.helse.general.CallId
import no.nav.helse.general.auth.ApiGatewayApiKey
import no.nav.helse.general.rest.NavHeaders
import java.net.URI

abstract class K9OppslagGateway(
    protected val baseUrl: URI,
    protected val accessTokenClient: AccessTokenClient, //skal erstattes med annet token
    protected val apiGatewayApiKey: ApiGatewayApiKey,
    protected val scopes : Set<String> = setOf("openid")
) : HealthCheck {

    protected val cachedAccessTokenClient = CachedAccessTokenClient(accessTokenClient)

    protected fun generateHttpRequest(
        url: String,
        ident: NorskIdent,
        callId: CallId
    ): Request {
        val authorizationHeader = cachedAccessTokenClient.getAccessToken(scopes).asAuthoriationHeader()
        return url
            .httpGet()
            .header(
                HttpHeaders.Authorization to authorizationHeader,
                HttpHeaders.Accept to "application/json",
                "Nav-Consumer-Id" to "pleiepengesoknad-api",
                "Nav-Personidenter" to ident.getValue(),
                NavHeaders.CallId to callId.value,
                apiGatewayApiKey.headerKey to apiGatewayApiKey.value
            )
    }
}