package no.nav.helse.soknad

import no.nav.helse.soker.Søker
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
    val arbeidsgivere: List<ArbeidsforholdAnsatt>,
    var vedleggId : List<String> = listOf(),
    val medlemskap: Medlemskap,
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden?,
    val ferieuttakIPerioden: FerieuttakIPerioden?,
    val harMedsøker: Boolean? = null,
    val samtidigHjemme: Boolean?,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val omsorgstilbud: Omsorgstilbud? = null,
    val nattevåk: Nattevåk?,
    val beredskap: Beredskap?,
    val frilans: Frilans? = null,
    val selvstendigNæringsdrivende: SelvstendigNæringsdrivende? = null,
    val barnRelasjon: BarnRelasjon? = null,
    val barnRelasjonBeskrivelse: String? = null,
    val harVærtEllerErVernepliktig: Boolean? = null,
    val k9FormatSøknad: Søknad? = null
)
