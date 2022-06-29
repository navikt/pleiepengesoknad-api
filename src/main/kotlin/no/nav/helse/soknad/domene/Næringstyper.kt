package no.nav.helse.soknad.domene

import no.nav.k9.søknad.felles.type.VirksomhetType

enum class Næringstyper {
    FISKE,
    JORDBRUK_SKOGBRUK,
    DAGMAMMA,
    ANNEN;

    internal fun tilK9VirksomhetType() = when(this) {
        FISKE -> VirksomhetType.FISKE
        JORDBRUK_SKOGBRUK -> VirksomhetType.JORDBRUK_SKOGBRUK
        DAGMAMMA -> VirksomhetType.DAGMAMMA
        ANNEN -> VirksomhetType.ANNEN
    }
}