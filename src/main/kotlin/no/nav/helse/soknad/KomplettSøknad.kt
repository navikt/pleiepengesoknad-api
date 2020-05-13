package no.nav.helse.soknad

import no.nav.helse.soker.Søker
import no.nav.helse.vedlegg.Vedlegg
import java.time.LocalDate
import java.time.ZonedDateTime

data class KomplettSøknad(
    val språk: Språk? = null,
    val mottatt: ZonedDateTime,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val søker: Søker,
    val barn: BarnDetaljer,
    val arbeidsgivere: ArbeidsgiverDetaljer,
    val vedlegg: List<Vedlegg> = listOf(), // TODO: Fjern listof() når krav om legeerklæring er påkrevd igjen.
    val medlemskap: Medlemskap,
    val bekrefterPeriodeOver8Uker: Boolean? = null,
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden?,
    val ferieuttakIPerioden: FerieuttakIPerioden?,
    val relasjonTilBarnet: String,
    val harMedsøker: Boolean? = null,
    val samtidigHjemme: Boolean?,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val tilsynsordning: Tilsynsordning?,
    val nattevåk: Nattevåk?,
    val beredskap: Beredskap?,
    val frilans: Frilans? = null,
    val selvstendigVirksomheter: List<Virksomhet>? = null,
    val skalBekrefteOmsorg: Boolean? = null, // TODO: Fjern optional når prodsatt.
    val skalPassePåBarnetIHelePerioden: Boolean? = null, // TODO: Fjern optional når prodsatt.
    val beskrivelseOmsorgsrollen: String? = null // TODO: Fjern optional når prodsatt.
)
