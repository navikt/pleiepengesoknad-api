package no.nav.helse.soknad

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URL
import java.time.Duration
import java.time.LocalDate

enum class Sprak{nb, nn}

data class Soknad (
    val sprak: Sprak? = null,
    val barn : BarnDetaljer,
    val relasjonTilBarnet : String? = null,
    val arbeidsgivere : ArbeidsgiverDetaljer,
    val vedlegg : List<URL>,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val medlemskap: Medlemskap,
    val harMedsoker : Boolean? = null,
    val harForstattRettigheterOgPlikter : Boolean,
    val harBekreftetOpplysninger : Boolean,
    val grad : Int? = null,
    val dagerPerUkeBorteFraJobb: Double? = null
)
data class ArbeidsgiverDetaljer(
    val organisasjoner : List<OrganisasjonDetaljer>
)

data class Medlemskap(
    @JsonProperty("har_bodd_i_utlandet_siste_12_mnd")
    val harBoddIUtlandetSiste12Mnd : Boolean? = null,
    @JsonProperty("skal_bo_i_utlandet_neste_12_mnd")
    val skalBoIUtlandetNeste12Mnd : Boolean? = null
)

data class BarnDetaljer(
    val fodselsnummer: String?,
    val alternativId: String?,
    val aktoerId: String?,
    val navn: String?
)

data class OrganisasjonDetaljer (
    val navn: String? = null,
    val organisasjonsnummer: String,
    val redusertArbeidsprosent: Int? = null
)