package no.nav.helse.soknad

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URL
import java.time.Duration
import java.time.LocalDate

enum class Språk { nb, nn }

data class Søknad(
    val newVersion: Boolean?,
    @JsonAlias("sprak","språk")
    val språk: Språk? = null,
    val barn: BarnDetaljer,
    val relasjonTilBarnet: String? = null,
    val arbeidsgivere: ArbeidsgiverDetaljer,
    val vedlegg: List<URL> = listOf(), // TODO: Fjern listof() når krav om legeerklæring er påkrevd igjen.
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val tilOgMed: LocalDate,
    val medlemskap: Medlemskap,
    val bekrefterPeriodeOver8Uker: Boolean? = null,// TODO: Fjern optional når prodsatt.
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden?,
    val ferieuttakIPerioden: FerieuttakIPerioden?,
    @JsonAlias("harMedsoker", "harMedsøker")
    val harMedsøker: Boolean? = null,
    val samtidigHjemme: Boolean? = null,
    @JsonAlias("harForstattRettigheterOgPlikter", "harForståttRettigheterOgPlikter")
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val tilsynsordning: Tilsynsordning?,
    @JsonAlias("nattevaak", "nattevåk")
    val nattevåk: Nattevåk? = null,
    val beredskap: Beredskap? = null,
    val frilans: Frilans? = null,
    val selvstendigVirksomheter: List<Virksomhet> = listOf(),
    val skalBekrefteOmsorg: Boolean? = null, // TODO: Fjern optional når prodsatt.
    @JsonAlias("skalPassePåBarnetIHelePerioden", "skalPassePaBarnetIHelePerioden")
    val skalPassePåBarnetIHelePerioden: Boolean? = null, // TODO: Fjern optional når prodsatt.
    val beskrivelseOmsorgsRollen: String? = null // TODO: Fjern optional når prodsatt.
)

data class ArbeidsgiverDetaljer(
    val organisasjoner: List<OrganisasjonDetaljer>
)

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

data class BarnDetaljer(
    val fodselsnummer: String?,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fodselsdato: LocalDate?,
    val aktørId: String?,
    val navn: String?
) {
    override fun toString(): String {
        return "BarnDetaljer(aktørId=${aktørId}, navn=${navn}, fodselsdato=${fodselsdato}"
    }
}

data class OrganisasjonDetaljer(
    val navn: String? = null,
    val skalJobbe: String,
    val organisasjonsnummer: String,
    val jobberNormaltTimer: Double,
    val skalJobbeProsent: Double,
    val vetIkkeEkstrainfo: String? = null
)

enum class TilsynsordningSvar { ja, nei, vet_ikke }

enum class TilsynsordningVetIkkeSvar { er_sporadisk, er_ikke_laget_en_plan, annet }

data class TilsynsordningJa(
    val mandag: Duration?,
    val tirsdag: Duration?,
    val onsdag: Duration?,
    val torsdag: Duration?,
    val fredag: Duration?,
    val tilleggsinformasjon: String? = null
) {
    override fun toString(): String {
        return "TilsynsordningJa(mandag=${mandag}, tirsdag=${tirsdag}, onsdag=${onsdag}, torsdag=${torsdag}, fredag=${fredag})"
    }
}

data class TilsynsordningVetIkke(
    val svar: TilsynsordningVetIkkeSvar,
    val annet: String? = null
) {
    override fun toString(): String {
        return "TilsynsordningVetIkke(svar=${svar})"
    }
}

data class Tilsynsordning(
    val svar: TilsynsordningSvar,
    val ja: TilsynsordningJa? = null,
    val vetIkke: TilsynsordningVetIkke? = null
)

data class Nattevåk(
    @JsonAlias("harNattevaak", "harNattevåk")
    val harNattevåk: Boolean? = null,
    val tilleggsinformasjon: String?
) {
    override fun toString(): String {
        return "Nattevåk(harNattevåk=${harNattevåk})"
    }
}

data class Beredskap(
    val beredskap: Boolean? = null,
    val tilleggsinformasjon: String?
) {
    override fun toString(): String {
        return "Beredskap(beredskap=${beredskap})"
    }
}

data class Utenlandsopphold(
    @JsonFormat(pattern = "yyyy-MM-dd") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val tilOgMed: LocalDate,
    val landkode: String,
    val landnavn: String,
    val erUtenforEos: Boolean?,
    val erBarnetInnlagt: Boolean?,
    val årsak: Årsak?
) {
    override fun toString(): String {
        return "Utenlandsopphold(fraOgMed=$fraOgMed, tilOgMed=$tilOgMed, landkode='$landkode', landnavn='$landnavn', erUtenforEos=$erUtenforEos, erBarnetInnlagt=$erBarnetInnlagt, årsak=$årsak)"
    }
}

data class Bosted(
    @JsonFormat(pattern = "yyyy-MM-dd") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val tilOgMed: LocalDate,
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
    @JsonFormat(pattern = "yyyy-MM-dd") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val tilOgMed: LocalDate
) {
    override fun toString(): String {
        return "Ferieuttak(fraOgMed=$fraOgMed, tilOgMed=$tilOgMed)"
    }
}

data class Frilans(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val startdato: LocalDate,
    val jobberFortsattSomFrilans: Boolean
)

enum class Årsak {
    BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING,
    BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD,
    ANNET,
}
