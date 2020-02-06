package no.nav.helse.soknad

import no.nav.helse.soker.Soker
import no.nav.helse.vedlegg.Vedlegg
import java.time.LocalDate
import java.time.ZonedDateTime

data class KomplettSoknad(
    val sprak: Sprak?,
    val mottatt : ZonedDateTime,
    val fraOgMed : LocalDate,
    val tilOgMed : LocalDate,
    val soker : Soker,
    val barn : BarnDetaljer,
    val arbeidsgivere: ArbeidsgiverDetaljer,
    val vedlegg: List<Vedlegg>,
    val medlemskap : Medlemskap,
    val relasjonTilBarnet : String,
    val grad : Int?,
    val harMedsoker : Boolean,
    val samtidigHjemme: Boolean?,
    val harForstattRettigheterOgPlikter : Boolean,
    val harBekreftetOpplysninger : Boolean,
    val dagerPerUkeBorteFraJobb: Double?,
    val tilsynsordning: Tilsynsordning?,
    val nattevaak: Nattevaak?,
    val beredskap: Beredskap?,
    val harHattInntektSomFrilanser: Boolean,
    val frilans: Frilans?

)