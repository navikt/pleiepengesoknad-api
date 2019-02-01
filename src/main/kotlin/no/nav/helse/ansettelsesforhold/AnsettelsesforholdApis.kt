package no.nav.helse.ansettelsesforhold

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import no.nav.helse.general.auth.getFodselsnummer
import no.nav.helse.general.getCallId
import java.time.LocalDate

@KtorExperimentalLocationsAPI
fun Route.ansettelsesforholdApis(
    service: AnsettelsesforholdService
) {

    @Location("/ansettelsesforhold")
    data class GetAnsettelsesforhold(val fra_og_med : String, val til_og_med : String)

    get<GetAnsettelsesforhold> { parameters ->
        call.respond(
            AnsettelsesforholdResponse(
                service.getAnsettelsesforhold(
                    fnr = call.getFodselsnummer(),
                    callId = call.getCallId(),
                    fraOgMed = LocalDate.parse(parameters.fra_og_med),
                    tilOgMed = LocalDate.parse(parameters.til_og_med)
                )
            )
        )
    }
}

