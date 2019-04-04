package no.nav.helse.general.auth

import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.protocol.HttpContext

internal class ApiGatewayHttpRequestInterceptor(
    private val apiGatewayApiKey: ApiGatewayApiKey
) : HttpRequestInterceptor {

    override fun process(request: HttpRequest?, context: HttpContext?) {
        request!!.addHeader(apiGatewayApiKey.headerKey, apiGatewayApiKey.value)
    }

}