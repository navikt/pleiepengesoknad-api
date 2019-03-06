package no.nav.helse.soknad

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.helse.arbeidsgiver.Arbeidsgiver
import org.hibernate.validator.constraints.Length
import java.net.URL
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@ValidSoknad
data class Soknad (
    @get:Valid val barn : BarnDetaljer,
    @get:NotBlank val relasjonTilBarnet : String,
    @get:Valid val arbeidsgivere : ArbeidsgiverDetailjer,
    @get:Valid @get:Size(min=1) val vedlegg : List<URL>,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val medlemskap: Medlemskap
)
data class ArbeidsgiverDetailjer(
    val organisasjoner : List<Arbeidsgiver>
)

data class Medlemskap(
    @JsonProperty("har_bodd_i_utlandet_siste_12_mnd")
    val harBoddIUtlandetSiste12Mnd : Boolean,
    @JsonProperty("skal_bo_i_utlandet_neste_12_mnd")
    val skalBoIUtlandetNeste12Mnd : Boolean
)

@ValidBarnDetaljer
data class BarnDetaljer(
    @get:Pattern(regexp = "\\d{11}") val fodselsnummer: String?,
    @get:Pattern(regexp = "\\d{11}") val alternativId: String?,
    @get:Length(max = 100) val navn: String
)