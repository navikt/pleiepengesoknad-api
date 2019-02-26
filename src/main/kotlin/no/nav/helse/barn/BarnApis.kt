package no.nav.helse.barn

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import java.time.LocalDate

@KtorExperimentalLocationsAPI
fun Route.barnApis() {

    @Location("/barn")
    class getBarn

    get { _: getBarn ->
        call.respond(BarnResponse())
    }
}

private data class BarnResponse(
    val barn: List<Barn> = listOf()
)

private data class Barn (
    val fodselsdato : LocalDate,
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String
)