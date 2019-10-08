package no.nav.helse.barn

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import no.nav.helse.general.auth.getNorskIdent
import no.nav.helse.general.getCallId
import no.nav.helse.k9.K9OppslagBarnService
import no.nav.helse.k9.tilDto

@KtorExperimentalLocationsAPI
fun Route.barnApis(
    k9OppslagBarnService: K9OppslagBarnService
) {

    @Location("/barn")
    class getBarn

    get { _: getBarn ->
        call.respond(
            BarnResponse(
                k9OppslagBarnService.hentNaaverendeBarn(
                    ident = call.getNorskIdent().getValue(),
                    callId = call.getCallId()
                ).map { it.tilDto() }
            )
        )
    }
}

private data class BarnResponse(
    val barn: List<no.nav.helse.k9.BarnDTO>
)
