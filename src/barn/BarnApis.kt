package no.nav.pleiepenger.api.barn

import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import no.nav.pleiepenger.api.general.auth.getFodselsnummer
import java.time.LocalDate
import java.util.*

fun Route.barnApis(
    httpClient: HttpClient
) {

    @Location("/soker/{id}/barn")
    class getBarn(
        val id: kotlin.String
    )

    get { it: getBarn ->
        call.respond(
            BarnResponse(
                Collections.singletonList(
                    Barn(
                        fornavn = "Barn" + getFodselsnummer(call),
                        mellomnavn = "Barn",
                        etternavn = "Barnesen",
                        fodselsdato = LocalDate.now()
                    )
                )
            )
        )
    }

}