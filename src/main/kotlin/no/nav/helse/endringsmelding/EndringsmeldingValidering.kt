package no.nav.helse.endringsmelding

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.soker.validate
import no.nav.k9.søknad.felles.Feil
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarnSøknadValidator

fun KomplettEndringsmelding.validerering(gyldigEndringsperiode: Periode) = mutableListOf<Violation>().apply {
    søker.validate()

    val k9FormatValideringsFeil: MutableList<Feil> =
        PleiepengerSyktBarnSøknadValidator().valider(k9Format, listOf(gyldigEndringsperiode))

    when {
        k9FormatValideringsFeil.isNotEmpty() -> {
            addAll(k9FormatValideringsFeil.map {
                Violation(
                    parameterName = it.felt,
                    parameterType = ParameterType.ENTITY,
                    reason = it.feilmelding,
                    invalidValue = "K9-format feilkode: ${it.feilkode}",
                )
            })
        }
    }
}.sortedBy { it.reason }.toMutableSet()

fun KomplettEndringsmelding.forsikreValidert(gyldigEndringsperiode: Periode): KomplettEndringsmelding {
    val valideringsfeil = validerering(gyldigEndringsperiode)
    if (valideringsfeil.isNotEmpty()) throw Throwblem(
        problemDetails = ValidationProblemDetails(valideringsfeil)
    ) else return this
}
