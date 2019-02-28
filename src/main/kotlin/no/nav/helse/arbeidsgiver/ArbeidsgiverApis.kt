package no.nav.helse.arbeidsgiver

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import no.nav.helse.general.auth.getFodselsnummer
import no.nav.helse.general.getCallId
import no.nav.helse.general.validation.ValidationException
import no.nav.helse.general.validation.Violation
import java.time.LocalDate

private const val fraOgMedQueryName = "fra_og_med"
private const val tilOgMedQueryName = "til_og_med"

fun Route.ansettelsesforholdApis(
    service: ArbeidsgiverService
) {

    get("/arbeidsgiver") {
        val violations = validateQueryParameters(call.request.queryParameters[fraOgMedQueryName], call.request.queryParameters[tilOgMedQueryName])

        if (violations.isNotEmpty()) {
            throw ValidationException(violations)
        } else {
            call.respond(
                ArbeidsgiverResponse(
                    service.getAnsettelsesforhold(
                        fnr = call.getFodselsnummer(),
                        callId = call.getCallId(),
                        fraOgMed = LocalDate.parse(call.request.queryParameters[fraOgMedQueryName]),
                        tilOgMed = LocalDate.parse(call.request.queryParameters[tilOgMedQueryName])
                    )
                )
            )
        }
    }
}

private fun validateQueryParameters(fraOgMed: String?, tilOgMed: String?) : List<Violation> {
    val violations = mutableListOf<Violation>()
    try {
        LocalDate.parse(fraOgMed)
    } catch (cause: Throwable) {
        violations.add(Violation(name = "fra_og_med", reason = "Ikke gyldig format (YYYY-MM-DD)", invalidValue = fraOgMed))
    }
    try {
        LocalDate.parse(tilOgMed)
    } catch (cause: Throwable) {
        violations.add(Violation(name = "til_og_med", reason = "Ikke gyldig format (YYYY-MM-DD)", invalidValue = tilOgMed))
    }
    return violations.toList()
}

data class ArbeidsgiverResponse (
    val organisasjoner : List<Arbeidsgiver>
)