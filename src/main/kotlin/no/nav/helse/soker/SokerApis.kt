package no.nav.helse.soker

import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate

private val logger: Logger = LoggerFactory.getLogger("nav.sokerApis")


@KtorExperimentalLocationsAPI
fun Route.sokerApis(
    httpClient: HttpClient
) {

    @Location("/soker")
    class getSoker

    get { _: getSoker ->
        logger.info("Client is '{}'", httpClient::javaClass)
        call.respond(
            Soker(
                fornavn = "Soker",
                mellomnavn = "Soker",
                etternavn = "Sokersen",
                fodselsdato = LocalDate.now()
            )
        )
    }
}
