package no.nav.helse.soknad

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.helse.soker.Søker
import no.nav.helse.vedlegg.Vedlegg
import java.time.LocalDate
import java.time.ZonedDateTime

data class KomplettSoknad(
    @JsonAlias("sprak","språk")
    val språk: Språk? = null,
    val mottatt: ZonedDateTime,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val soker: Søker,
    val barn: BarnDetaljer,
    val arbeidsgivere: ArbeidsgiverDetaljer,
    val vedlegg: List<Vedlegg> = listOf(), // TODO: Fjern listof() når krav om legeerklæring er påkrevd igjen.
    val medlemskap: Medlemskap,
    @JsonProperty("bekrefter_periode_over_8_uker")
    val bekrefterPeriodeOver8Uker: Boolean? = null,
    @JsonProperty("utenlandsopphold_i_perioden")
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden?,
    @JsonProperty("ferieuttak_i_perioden")
    val ferieuttakIPerioden: FerieuttakIPerioden?,
    val relasjonTilBarnet: String,
    @JsonAlias("harMedsoker", "harMedsøker")
    val harMedsøker: Boolean? = null,
    val samtidigHjemme: Boolean?,
    @JsonAlias("harForstattRettigheterOgPlikter", "harForståttRettigheterOgPlikter")
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val tilsynsordning: Tilsynsordning?,
    @JsonAlias("nattevaak", "nattevåk")
    val nattevåk: Nattevåk?,
    val beredskap: Beredskap?,
    val harHattInntektSomFrilanser: Boolean = false,
    val frilans: Frilans? = null,
    val harHattInntektSomSelvstendigNaringsdrivende: Boolean = false,
    val selvstendigVirksomheter: List<Virksomhet>? = null,
    val skalBekrefteOmsorg: Boolean? = null, // TODO: Fjern optional når prodsatt.
    @JsonAlias("skalPassePåBarnetIHelePerioden", "skalPassePaBarnetIHelePerioden")
    val skalPassePåBarnetIHelePerioden: Boolean? = null, // TODO: Fjern optional når prodsatt.
    val beskrivelseOmsorgsRollen: String? = null // TODO: Fjern optional når prodsatt.
)
