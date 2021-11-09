package no.nav.helse.general.oppslag

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.httpGet
import io.ktor.http.*
import no.nav.helse.general.CallId
import no.nav.helse.general.auth.IdToken
import org.slf4j.Logger
import java.net.URI

abstract class K9OppslagGateway(
    protected val baseUrl: URI,
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
                HttpHeaders.XCorrelationId to callId.value
            )
    }
}

fun FuelError.throwable(request: Request, logger: Logger, errorMessage: String): Throwable {
    val errorResponseBody = response.body().asString("text/plain")
    logger.error("Error response = '$errorResponseBody' fra '${request.url}'")
    logger.error(toString())
    return when (response.statusCode) {
        451 -> TilgangNektetException("Tilgang nektet.")
        else -> {
            IllegalStateException(errorMessage)
        }
    }
}

data class TilgangNektetException(override val message: String) : RuntimeException(message)
