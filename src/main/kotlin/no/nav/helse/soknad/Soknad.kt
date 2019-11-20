package no.nav.helse.soknad

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URL
import java.time.Duration
import java.time.LocalDate

enum class Sprak{nb, nn}

data class Soknad (
    val newVersion: Boolean?,
    val sprak: Sprak? = null,
    val barn : BarnDetaljer,
    val relasjonTilBarnet : String? = null,
    val arbeidsgivere : ArbeidsgiverDetaljer,
    val vedlegg : List<URL>,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val medlemskap: Medlemskap,
    val harMedsoker : Boolean? = null,
    val samtidigHjemme: Boolean? = null,
    val harForstattRettigheterOgPlikter : Boolean,
    val harBekreftetOpplysninger : Boolean,
    val grad : Int? = null,
    val dagerPerUkeBorteFraJobb: Double? = null,
    val tilsynsordning: Tilsynsordning?,
    val nattevaak: Nattevaak? = null,
    val beredskap: Beredskap? = null
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
    val jobberNormaltTimer: Double? = null,
    val skalJobbeProsent: Double?  = null,
    val vetIkkeEkstrainfo: String? = null
)

enum class TilsynsordningSvar{ja, nei, vet_ikke}
enum class TilsynsordningVetIkkeSvar{er_sporadisk, er_ikke_laget_en_plan, annet}

data class TilsynsordningJa(
    val mandag: Duration?,
    val tirsdag: Duration?,
    val onsdag: Duration?,
    val torsdag: Duration?,
    val fredag: Duration?,
    val tilleggsinformasjon: String? = null
)
data class TilsynsordningVetIkke(
    val svar: TilsynsordningVetIkkeSvar,
    val annet: String? = null
)

data class Tilsynsordning(
    val svar: TilsynsordningSvar,
    val ja: TilsynsordningJa? = null,
    val vetIkke: TilsynsordningVetIkke? = null
)

data class Nattevaak(
    val harNattevaak: Boolean? = null,
    val tilleggsinformasjon: String?
)

data class Beredskap(
    @JsonProperty("i_beredskap")
    val beredskap: Boolean? = null,
    val tilleggsinformasjon: String?
)