package no.nav.helse.soknad

import java.net.URL
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@ValidSoknad
data class Soknad (
    @get:Valid val barn : BarnDetaljer,
    @get:NotBlank val relasjonTilBarnet : String,
    @get:Valid val ansettelsesforhold : AnsettelsesforholdDetaljer,
    @get:Valid @get:Size(min=1) val vedlegg : List<URL>,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate
)