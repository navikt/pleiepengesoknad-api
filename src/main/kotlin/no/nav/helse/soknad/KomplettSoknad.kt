package no.nav.helse.soknad

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.helse.soker.Soker
import no.nav.helse.vedlegg.Vedlegg
import java.time.LocalDate
import java.time.ZonedDateTime

data class KomplettSoknad(
    val sprak: Sprak?,
    val mottatt: ZonedDateTime,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val soker: Soker,
    val barn: BarnDetaljer,
    val arbeidsgivere: ArbeidsgiverDetaljer,
    val vedlegg: List<Vedlegg>,
    val medlemskap: Medlemskap,
    @JsonProperty("utenlandsopphold_i_perioden")
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden?,
    @JsonProperty("ferieuttak_i_perioden")
    val ferieuttakIPerioden: FerieuttakIPerioden?,
    val relasjonTilBarnet: String,
    val grad: Int?,
    val harMedsoker: Boolean,
    val samtidigHjemme: Boolean?,
    val harForstattRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val dagerPerUkeBorteFraJobb: Double?,
    val tilsynsordning: Tilsynsordning?,
    val nattevaak: Nattevaak?,
    val beredskap: Beredskap?,
    val harHattInntektSomFrilanser: Boolean = false,
    val frilans: Frilans?,
    @JsonProperty("skal_bekrefte_omsorg") val skalBekrefteOmsorg: Boolean? = null, // TODO: Fjern optional når prodsatt.
    @JsonProperty("skal_passe_pa_barnet_i_hele_perioden") val skalPassePaBarnetIHelePerioden: Boolean? = null, // TODO: Fjern optional når prodsatt.
    @JsonProperty("beskrivelse_omsorgsrollen") val beskrivelseOmsorgsRollen: String? = null // TODO: Fjern optional når prodsatt.
)
