package no.nav.helse.soknad

import no.nav.helse.soker.Søker
import no.nav.helse.vedlegg.Vedlegg
import no.nav.k9.søknad.Søknad
import java.time.LocalDate
import java.time.ZonedDateTime

data class KomplettSøknad(
    val språk: Språk? = null,
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val søker: Søker,
    val barn: BarnDetaljer,
    val ansatt: List<ArbeidsforholdAnsatt>?,
    val vedlegg: List<Vedlegg> = listOf(), // TODO: Fjern listof() når krav om legeerklæring er påkrevd igjen.
    val medlemskap: Medlemskap,
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden?,
    val ferieuttakIPerioden: FerieuttakIPerioden?,
    val harMedsøker: Boolean? = null,
    val samtidigHjemme: Boolean?,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val omsorgstilbudV2: OmsorgstilbudV2? = null,
    val nattevåk: Nattevåk?,
    val beredskap: Beredskap?,
    val frilans: Frilans? = null,
    val selvstendigNæringsdrivende: SelvstendigNæringsdrivende? = null,
    val skalBekrefteOmsorg: Boolean? = null, // TODO: Fjern optional når prodsatt.
    val skalPassePåBarnetIHelePerioden: Boolean? = null, // TODO: Fjern optional når prodsatt.
    val beskrivelseOmsorgsrollen: String? = null, // TODO: Fjern optional når prodsatt.
    val barnRelasjon: BarnRelasjon? = null,
    val barnRelasjonBeskrivelse: String? = null,
    val harVærtEllerErVernepliktig: Boolean? = null,
    val k9FormatSøknad: Søknad? = null
)
