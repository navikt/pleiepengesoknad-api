package no.nav.helse.soknad

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.helse.soker.Soker
import no.nav.helse.vedlegg.Vedlegg
import java.time.LocalDate
import java.time.ZonedDateTime

data class KomplettSoknad(
    val mottatt : ZonedDateTime,
    val fraOgMed : LocalDate,
    val tilOgMed : LocalDate,
    val soker : Soker,
    val barn : BarnDetaljer,
    val arbeidsgivere: ArbeidsgiverDetailjer,
    val vedlegg: List<Vedlegg>,
    val medlemskap : Medlemskap,
    val relasjonTilBarnet : String,
    val grad : Int,
    val erSelvstendigNaeringsdrivendeEllerFrilanser : Boolean,
    @JsonProperty("forventes_at_barnet_kan_vaere_i_etablert_tilsynsordning")
    val forventesAtBarnetKanVaereIEtablertTilsynsordning: Boolean
)