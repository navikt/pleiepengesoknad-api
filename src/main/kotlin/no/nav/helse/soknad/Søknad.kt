package no.nav.helse.soknad

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.fpsak.tidsserie.LocalDateInterval
import no.nav.helse.barn.Barn
import no.nav.helse.soker.Søker
import no.nav.helse.soknad.domene.Frilans
import no.nav.helse.soknad.domene.OpptjeningIUtlandet
import no.nav.k9.søknad.Søknad
import java.net.URL
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

enum class Språk { nb, nn }

data class Søknad(
    val newVersion: Boolean?,
    val søknadId: String = UUID.randomUUID().toString(),
    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    val språk: Språk? = null,
    val barn: BarnDetaljer,
    val arbeidsgivere: List<Arbeidsgiver>,
    val vedlegg: List<URL> = listOf(), // TODO: Fjern listof() når krav om legeerklæring er påkrevd igjen.
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val tilOgMed: LocalDate,
    val medlemskap: Medlemskap,
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden,
    val ferieuttakIPerioden: FerieuttakIPerioden?,
    val opptjeningIUtlandet: List<OpptjeningIUtlandet>? = null, // TODO: 08/06/2022 Fjerne nullable når frontend er prodsatt
    val harMedsøker: Boolean? = null,
    val samtidigHjemme: Boolean? = null,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val omsorgstilbud: Omsorgstilbud? = null,
    val nattevåk: Nattevåk? = null,
    val beredskap: Beredskap? = null,
    val frilans: Frilans,
    val selvstendigNæringsdrivende: SelvstendigNæringsdrivende,
    val barnRelasjon: BarnRelasjon? = null,
    val barnRelasjonBeskrivelse: String? = null,
    val harVærtEllerErVernepliktig: Boolean? = null
) {
    fun oppdaterBarnMedFnr(listeOverBarn: List<Barn>) {
        if (barn.manglerIdentitetsnummer()) {
            barn oppdaterFødselsnummer listeOverBarn.hentIdentitetsnummerForBarn(barn.aktørId)
        }
    }

    fun tilKomplettSøknad(
        k9FormatSøknad: Søknad,
        søker: Søker
    ): KomplettSøknad = KomplettSøknad(
        språk = språk,
        søknadId = søknadId,
        mottatt = mottatt,
        fraOgMed = fraOgMed,
        tilOgMed = tilOgMed,
        søker = søker,
        barn = barn,
        vedleggId = vedlegg.map { it.vedleggId() },
        arbeidsgivere = arbeidsgivere,
        medlemskap = medlemskap,
        ferieuttakIPerioden = ferieuttakIPerioden,
        opptjeningIUtlandet = opptjeningIUtlandet,
        utenlandsoppholdIPerioden = utenlandsoppholdIPerioden,
        harMedsøker = harMedsøker!!,
        samtidigHjemme = samtidigHjemme,
        harBekreftetOpplysninger = harBekreftetOpplysninger,
        harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter,
        omsorgstilbud = omsorgstilbud,
        nattevåk = nattevåk,
        beredskap = beredskap,
        frilans = frilans,
        selvstendigNæringsdrivende = selvstendigNæringsdrivende,
        barnRelasjon = barnRelasjon,
        barnRelasjonBeskrivelse = barnRelasjonBeskrivelse,
        harVærtEllerErVernepliktig = harVærtEllerErVernepliktig,
        k9FormatSøknad = k9FormatSøknad
    )
}

fun URL.vedleggId() = this.toString().substringAfterLast("/")

private fun List<Barn>.hentIdentitetsnummerForBarn(aktørId: String?): String? {
    return this.firstOrNull(){ it.aktørId == aktørId}?.identitetsnummer
}

enum class BarnRelasjon(val utskriftsvennlig: String) {
    MOR("MOR"),
    MEDMOR("MEDMOR"),
    FAR("FAR"),
    FOSTERFORELDER("FOSTERFORELDER"),
    ANNET("ANNET")
}

data class Medlemskap(
    val harBoddIUtlandetSiste12Mnd: Boolean? = null,
    val utenlandsoppholdSiste12Mnd: List<Bosted> = listOf(),
    val skalBoIUtlandetNeste12Mnd: Boolean? = null,
    val utenlandsoppholdNeste12Mnd: List<Bosted> = listOf()
)

data class UtenlandsoppholdIPerioden(
    val skalOppholdeSegIUtlandetIPerioden: Boolean? = null,
    val opphold: List<Utenlandsopphold> = listOf()
)

data class Nattevåk(
    val harNattevåk: Boolean? = null,
    val tilleggsinformasjon: String?
) {
    override fun toString(): String {
        return "Nattevåk(harNattevåk=${harNattevåk})"
    }
}

data class Beredskap(
    val beredskap: Boolean,
    val tilleggsinformasjon: String?
) {
    override fun toString(): String {
        return "Beredskap(beredskap=${beredskap})"
    }
}

data class Utenlandsopphold(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val tilOgMed: LocalDate,
    val landkode: String,
    val landnavn: String,
    val erUtenforEøs: Boolean?,
    val erBarnetInnlagt: Boolean?,
    val perioderBarnetErInnlagt: List<Periode> = listOf(),
    val årsak: Årsak?
) {
    override fun toString(): String {
        return "Utenlandsopphold(fraOgMed=$fraOgMed, tilOgMed=$tilOgMed, landkode='$landkode', landnavn='$landnavn', erUtenforEos=$erUtenforEøs, erBarnetInnlagt=$erBarnetInnlagt, årsak=$årsak)"
    }
}

data class Periode(
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate
){
    fun somLocalDateInterval() = LocalDateInterval(fraOgMed, tilOgMed)
}

data class Bosted(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val tilOgMed: LocalDate,
    val landkode: String,
    val landnavn: String
) {
    override fun toString(): String {
        return "Utenlandsopphold(fraOgMed=$fraOgMed, tilOgMed=$tilOgMed, landkode='$landkode', landnavn='$landnavn')"
    }
}

data class FerieuttakIPerioden(
    val skalTaUtFerieIPerioden: Boolean,
    val ferieuttak: List<Ferieuttak>
) {
    override fun toString(): String {
        return "FerieuttakIPerioden(skalTaUtFerieIPerioden=$skalTaUtFerieIPerioden, ferieuttak=$ferieuttak)"
    }
}

data class Ferieuttak(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val tilOgMed: LocalDate
) {
    override fun toString(): String {
        return "Ferieuttak(fraOgMed=$fraOgMed, tilOgMed=$tilOgMed)"
    }
}

enum class Årsak {
    BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING,
    BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD,
    ANNET;

    fun tilK9Årsak()= when(this) {
        BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING -> no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold.UtenlandsoppholdÅrsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING
        BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD -> no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold.UtenlandsoppholdÅrsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD
        else -> null
    }
}