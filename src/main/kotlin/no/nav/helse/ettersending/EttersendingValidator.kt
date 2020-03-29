package no.nav.helse.ettersending

import no.nav.helse.dusseldorf.ktor.core.*
import no.nav.helse.vedlegg.Vedlegg
import java.net.URL

internal fun Ettersending.valider() = mutableSetOf<Violation>().let {

    if (søknadstype != "omsorgspenger") {
        it.add(
            Violation(
                parameterName = "søknadstype",
                parameterType = ParameterType.ENTITY,
                reason = "Feil søknadstype. Kun 'omsorgspenger' er tillatt.",
                invalidValue = søknadstype

            )
        )
    }

    if (beskrivelse.isBlank()) {
        it.add(
            Violation(
                parameterName = "beskrivelse",
                parameterType = ParameterType.ENTITY,
                reason = "Beskrivelse kan ikke være tomt.",
                invalidValue = beskrivelse
            )
        )
    }

    if (vedlegg.isEmpty()) {
        it.add(
            Violation(
                parameterName = "vedlegg",
                parameterType = ParameterType.ENTITY,
                reason = "Det må sendes minst et vedlegg.",
                invalidValue = vedlegg
            )
        )
    }

    vedlegg.mapIndexed { index, url ->
        // Kan oppstå url = null etter Jackson deserialisering
        if (!url.path.matches(Regex("/vedlegg/.*"))) {
            it.add(
                Violation(
                    parameterName = "vedlegg[$index]",
                    parameterType = ParameterType.ENTITY,
                    reason = "Ikke gyldig vedlegg URL.",
                    invalidValue = url
                )
            )
        }
    }

    if (!harBekreftetOpplysninger) {
        it.add(
            Violation(
                parameterName = "harBekreftetOpplysninger",
                parameterType = ParameterType.ENTITY,
                reason = "Opplysningene må bekreftes for å sende inn ettersending.",
                invalidValue = false

            )
        )
    }

    if (!harForståttRettigheterOgPlikter) {
        it.add(
            Violation(
                parameterName = "harForståttRettigheterOgPlikter",
                parameterType = ParameterType.ENTITY,
                reason = "Må ha forstått rettigheter og plikter for å sende inn ettersending.",
                invalidValue = false
            )
        )
    }

    if (it.isNotEmpty()) {
        throw Throwblem(ValidationProblemDetails(it))
    }
}

