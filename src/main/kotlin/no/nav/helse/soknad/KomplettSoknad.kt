package no.nav.helse.soknad

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.helse.soker.Soker
import no.nav.helse.vedlegg.Vedlegg
import java.time.LocalDate
import java.time.ZonedDateTime

data class KomplettSoknad(
    val sprak: Sprak?, //TODO Språk?
    val mottatt: ZonedDateTime,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val soker: Soker,
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
    val harMedsoker: Boolean,
    val samtidigHjemme: Boolean?,
    val harForstattRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val tilsynsordning: Tilsynsordning?,
    val nattevaak: Nattevaak?,
    val beredskap: Beredskap?,
    val harHattInntektSomFrilanser: Boolean = false,
    val frilans: Frilans? = null,
    val harHattInntektSomSelvstendigNaringsdrivende: Boolean = false,
    val selvstendigVirksomheter: List<Virksomhet>? = null,
    @JsonProperty("skal_bekrefte_omsorg") val skalBekrefteOmsorg: Boolean? = null, // TODO: Fjern optional når prodsatt.
    @JsonProperty("skal_passe_pa_barnet_i_hele_perioden") val skalPassePaBarnetIHelePerioden: Boolean? = null, // TODO: Fjern optional når prodsatt.
    @JsonProperty("beskrivelse_omsorgsrollen") val beskrivelseOmsorgsRollen: String? = null // TODO: Fjern optional når prodsatt.
)
