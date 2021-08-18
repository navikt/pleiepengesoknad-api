package no.nav.helse.soknad

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.barn.Barn
import java.net.URL
import java.time.LocalDate
import java.util.*

enum class Språk { nb, nn }

data class Søknad(
    val newVersion: Boolean?,
    val søknadId: String = UUID.randomUUID().toString(),
    val språk: Språk? = null,
    val barn: BarnDetaljer,
    val arbeidsgivere: ArbeidsgiverDetaljer,
    val vedlegg: List<URL> = listOf(), // TODO: Fjern listof() når krav om legeerklæring er påkrevd igjen.
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val tilOgMed: LocalDate,
    val medlemskap: Medlemskap,
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden?,
    val ferieuttakIPerioden: FerieuttakIPerioden?,
    val harMedsøker: Boolean? = null,
    val samtidigHjemme: Boolean? = null,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val omsorgstilbud: Omsorgstilbud? = null, // TODO: 18/08/2021 Blir erstattet med omsorgstilbudV2 etter prodsetting .
    val omsorgstilbudV2: OmsorgstilbudV2? = null,
    val nattevåk: Nattevåk? = null,
    val beredskap: Beredskap? = null,
    val frilans: Frilans? = null,
    val selvstendigVirksomheter: List<Virksomhet> = listOf(),
    val selvstendigArbeidsforhold: Arbeidsforhold? = null,
    val skalBekrefteOmsorg: Boolean? = null, // TODO: Fjern optional når prodsatt.
    val skalPassePåBarnetIHelePerioden: Boolean? = null, // TODO: Fjern optional når prodsatt.
    val beskrivelseOmsorgsrollen: String? = null, // TODO: Fjern optional når prodsatt.
    val barnRelasjon: BarnRelasjon? = null,
    val barnRelasjonBeskrivelse: String? = null,
    val harVærtEllerErVernepliktig: Boolean? = null
) {

    fun oppdaterBarnMedFnr(listeOverBarn: List<Barn>) {
        if (barn.manglerIdentitetsnummer()) {
            barn oppdaterFødselsnummer listeOverBarn.hentIdentitetsnummerForBarn(barn.aktørId)
        }
    }
}

private fun List<Barn>.hentIdentitetsnummerForBarn(aktørId: String?): String? {
    this.forEach {
        if (it.aktørId == aktørId) return it.identitetsnummer
    }
    return null
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
)

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

data class Frilans(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val startdato: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val sluttdato: LocalDate? = null,
    val jobberFortsattSomFrilans: Boolean,
    val arbeidsforhold: Arbeidsforhold? = null
)

enum class Årsak {
    BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING,
    BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD,
    ANNET,
}

data class Arbeidsforhold(
    val skalJobbe:SkalJobbe,
    val arbeidsform: Arbeidsform,
    val jobberNormaltTimer: Double,
    val skalJobbeProsent: Double
)

enum class SkalJobbe {
    @JsonAlias("ja") JA,
    @JsonAlias("nei") NEI,
    @JsonAlias("redusert") REDUSERT,
    @JsonAlias("vetIkke") VET_IKKE
}
