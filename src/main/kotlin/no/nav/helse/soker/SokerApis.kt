package no.nav.helse.soker

import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import java.time.LocalDate

fun Route.sokerApis(
    httpClient: HttpClient
) {

    @Location("/soker/{id}")
    class getSoker(
        val id: kotlin.String
    )

    get { it: getSoker ->
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
