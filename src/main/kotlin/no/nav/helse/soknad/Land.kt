package no.nav.helse.soknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.general.krever
import java.util.*

class Land(
    val landkode: String,
    val landnavn: String
) {
    companion object{
        // ISO 3166 alpha-3 landkode - https://en.wikipedia.org/wiki/ISO_3166-1_alpha-3
        internal val LANDKODER: MutableSet<String> = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA3)
    }

    override fun equals(other: Any?) = this === other || other is Land && this.equals(other)
    private fun equals(other: Land) = this.landkode == other.landkode && this.landnavn == other.landnavn
    internal fun validerV2(felt: String) = mutableListOf<String>().apply {
        krever(LANDKODER.contains(landkode), "$felt.Landkode '$landkode' er ikke en gyldig ISO 3166-1 alpha-3 kode.")
        krever(landnavn.isNotBlank(), "$felt.landnavn kan ikke være tomt eller blankt.")
    }

    internal fun valider(feltnavn: String) = mutableSetOf<Violation>().apply {
        if (landkode.isBlank()) {
            add(
                Violation(
                    parameterName = "${feltnavn}.landkode",
                    parameterType = ParameterType.ENTITY,
                    reason = "Landkode kan ikke være blank.",
                    invalidValue = landkode
                )
            )
        }

        if (!LANDKODER.contains(landkode)) {
            add(
                Violation(
                    parameterName = "${feltnavn}.landkode",
                    parameterType = ParameterType.ENTITY,
                    reason = "Landkode er ikke en gyldig ISO 3166-1 alpha-3 kode.",
                    invalidValue = landkode
                )
            )
        }

        if (landnavn.isBlank()) {
            add(
                Violation(
                    parameterName = "${feltnavn}.landnavn",
                    parameterType = ParameterType.ENTITY,
                    reason = "Landnavn kan ikke være blank.",
                    invalidValue = landnavn
                )
            )
        }
    }
}