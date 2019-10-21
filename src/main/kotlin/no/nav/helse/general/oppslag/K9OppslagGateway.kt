package no.nav.helse.general.oppslag

import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.httpGet
import io.ktor.http.HttpHeaders
import no.nav.helse.general.CallId
import no.nav.helse.general.auth.ApiGatewayApiKey
import no.nav.helse.general.auth.IdToken
import java.net.URI

abstract class K9OppslagGateway(
    protected val baseUrl: URI,
    private val apiGatewayApiKey: ApiGatewayApiKey
) {

    protected fun generateHttpRequest(
        idToken: IdToken,
        url: String,
        callId: CallId
    ): Request {
        return url
            .httpGet()
            .header(
                HttpHeaders.Authorization to "Bearer ${idToken.value}",
                HttpHeaders.Accept to "application/json",
                HttpHeaders.XCorrelationId to callId.value,
                apiGatewayApiKey.headerKey to apiGatewayApiKey.value
            )
    }
}