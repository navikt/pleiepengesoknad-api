package no.nav.helse.soknad

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.helse.arbeidsgiver.Arbeidsgiver
import java.net.URL
import java.time.LocalDate

data class Soknad (
    val barn : BarnDetaljer,
    val relasjonTilBarnet : String,
    val arbeidsgivere : ArbeidsgiverDetailjer,
    val vedlegg : List<URL>,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val medlemskap: Medlemskap,
    val harMedsoker : Boolean? = null,
    val grad : Int,
    val erSelvstendigNaeringsdrivendeEllerFrilanser : Boolean? = null,
    @JsonProperty("forventes_at_barnet_kan_vaere_i_etablert_tilsynsordning")
    val forventesAtBarnetKanVaereIEtablertTilsynsordning: Boolean? = null
)
data class ArbeidsgiverDetailjer(
    val organisasjoner : List<Arbeidsgiver>
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
    val navn: String
)