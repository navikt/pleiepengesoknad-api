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
    val harMedsoker : Boolean,
    val grad : Int,
    val erSelvstendigNaeringsdrivendeEllerFrilanser : Boolean,
    val forventesAtBarnetKanVaereIEtablertTilsynsOrdning: Boolean
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

data class BarnDetaljer(
    val fodselsnummer: String?,
    val alternativId: String?,
    val navn: String
)

/*
{
    "grad": 100,
    "er_medsoker": true,
    "er_selvstendig_naeringsdrivende_eller_frilanser": false,
    "forventes_at_barnet_kan_vaere_i_etablert_tilsynsordning": false
}
 */