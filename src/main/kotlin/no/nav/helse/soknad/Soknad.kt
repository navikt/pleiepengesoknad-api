package no.nav.helse.soknad

import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.Size

/*
    I starten vil vi kun tillate søknader for et barn og et ansettelsesforhold.
    Må sende inn minst et vedlegg
 */

@ValidSoknad
data class Soknad (
    @get:Valid val barn : BarnDetaljer,
    @get:Valid val ansettelsesforhold : AnsettelsesforholdDetaljer,
    @get:Valid @get:Size(min=1) val vedlegg : List<Vedlegg>,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate
)