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
    @JsonAlias("sprak") val språk: Språk? = null,
    val barn: BarnDetaljer,
    @JsonAlias("relasjon_til_barnet") val relasjonTilBarnet: String? = null,
    val arbeidsgivere: ArbeidsgiverDetaljer,
    val vedlegg: List<URL> = listOf(), // TODO: Fjern listof() når krav om legeerklæring er påkrevd igjen.
    @JsonFormat(pattern = "yyyy-MM-dd") @JsonAlias("fra_og_med") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") @JsonAlias("til_og_med") val tilOgMed: LocalDate,
    val medlemskap: Medlemskap,
    @JsonAlias("bekrefter_periode_over_8_uker") val bekrefterPeriodeOver8Uker: Boolean? = null,// TODO: Fjern optional når prodsatt.
    @JsonAlias("utenlandsopphold_i_perioden") val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden?,
    @JsonAlias("ferieuttak_i_perioden") val ferieuttakIPerioden: FerieuttakIPerioden?,
    @JsonAlias("har_medsoker") val harMedsøker: Boolean? = null,
    @JsonAlias("samtidig_hjemme") val samtidigHjemme: Boolean? = null,
    @JsonAlias("har_forstatt_rettigheter_og_plikter") val harForståttRettigheterOgPlikter: Boolean,
    @JsonAlias("har_bekreftet_opplysninger") val harBekreftetOpplysninger: Boolean,
    val tilsynsordning: Tilsynsordning?,
    @JsonAlias("nattevaak") val nattevåk: Nattevåk? = null,
    val beredskap: Beredskap? = null,
    val frilans: Frilans? = null,
    @JsonAlias("selvstendig_virksomheter") val selvstendigVirksomheter: List<Virksomhet> = listOf(),
    @JsonAlias("skal_bekrefte_omsorg") val skalBekrefteOmsorg: Boolean? = null, // TODO: Fjern optional når prodsatt.
    @JsonAlias("skal_passe_pa_barnet_i_hele_perioden") val skalPassePåBarnetIHelePerioden: Boolean? = null, // TODO: Fjern optional når prodsatt.
    @JsonAlias("beskrivelse_omsorgs_rollen") val beskrivelseOmsorgsrollen: String? = null // TODO: Fjern optional når prodsatt.
)

data class ArbeidsgiverDetaljer(
    val organisasjoner: List<OrganisasjonDetaljer>
)

data class Medlemskap(
    @JsonAlias("har_bodd_i_utlandet_siste_12_mnd") val harBoddIUtlandetSiste12Mnd: Boolean? = null,
    @JsonAlias("utenlandsopphold_siste_12_mnd") val utenlandsoppholdSiste12Mnd: List<Bosted> = listOf(),
    @JsonAlias("skal_bo_i_utlandet_neste_12_mnd") val skalBoIUtlandetNeste12Mnd: Boolean? = null,
    @JsonAlias("utenlandsopphold_neste_12_mnd") val utenlandsoppholdNeste12Mnd: List<Bosted> = listOf()
)

data class UtenlandsoppholdIPerioden(
    @JsonAlias("skal_oppholde_seg_i_utlandet_i_perioden") val skalOppholdeSegIUtlandetIPerioden: Boolean? = null,
    val opphold: List<Utenlandsopphold> = listOf()
)

data class BarnDetaljer(
    @JsonAlias("fodselsnummer") val fødselsnummer: String?,
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonAlias("fodselsdato") val fødselsdato: LocalDate?,
    @JsonAlias("aktoer_id", "aktoerId") val aktørId: String?,
    val navn: String?
) {
    override fun toString(): String {
        return "BarnDetaljer(aktørId=${aktørId}, navn=${navn}, fodselsdato=${fødselsdato}"
    }
}

data class OrganisasjonDetaljer(
    val navn: String? = null,
    @JsonAlias("skal_jobbe") val skalJobbe: String,
    val organisasjonsnummer: String,
    @JsonAlias("jobber_normalt_timer") val jobberNormaltTimer: Double,
    @JsonAlias("skal_jobbe_prosent") val skalJobbeProsent: Double,
    @JsonAlias("vet_ikke_ekstrainfo") val vetIkkeEkstrainfo: String? = null
)

enum class TilsynsordningSvar {
    ja,
    nei,
    @JsonAlias("vet_ikke") vetIkke
}

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
    @JsonAlias("vet_ikke") val vetIkke: TilsynsordningVetIkke? = null
)

data class Nattevåk(
    @JsonAlias("har_nattevaak") val harNattevåk: Boolean? = null,
    val tilleggsinformasjon: String?
) {
    override fun toString(): String {
        return "Nattevåk(harNattevåk=${harNattevåk})"
    }
}

data class Beredskap(
    @JsonAlias("i_beredskap") val beredskap: Boolean,
    val tilleggsinformasjon: String?
) {
    override fun toString(): String {
        return "Beredskap(beredskap=${beredskap})"
    }
}

data class Utenlandsopphold(
    @JsonFormat(pattern = "yyyy-MM-dd") @JsonAlias("fra_og_med") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") @JsonAlias("til_og_med") val tilOgMed: LocalDate,
    val landkode: String,
    val landnavn: String,
    @JsonAlias("er_utenfor_eos") val erUtenforEøs: Boolean?,
    @JsonAlias("er_barnet_innlagt") val erBarnetInnlagt: Boolean?,
    val årsak: Årsak?
) {
    override fun toString(): String {
        return "Utenlandsopphold(fraOgMed=$fraOgMed, tilOgMed=$tilOgMed, landkode='$landkode', landnavn='$landnavn', erUtenforEos=$erUtenforEøs, erBarnetInnlagt=$erBarnetInnlagt, årsak=$årsak)"
    }
}

data class Bosted(
    @JsonFormat(pattern = "yyyy-MM-dd") @JsonAlias("fra_og_med") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") @JsonAlias("til_og_med") val tilOgMed: LocalDate,
    val landkode: String,
    val landnavn: String
) {
    override fun toString(): String {
        return "Utenlandsopphold(fraOgMed=$fraOgMed, tilOgMed=$tilOgMed, landkode='$landkode', landnavn='$landnavn')"
    }
}

data class FerieuttakIPerioden(
    @JsonAlias("skal_ta_ut_ferie_i_periode") val skalTaUtFerieIPerioden: Boolean,
    val ferieuttak: List<Ferieuttak>
) {
    override fun toString(): String {
        return "FerieuttakIPerioden(skalTaUtFerieIPerioden=$skalTaUtFerieIPerioden, ferieuttak=$ferieuttak)"
    }
}

data class Ferieuttak(
    @JsonFormat(pattern = "yyyy-MM-dd") @JsonAlias("fra_og_med") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") @JsonAlias("til_og_med") val tilOgMed: LocalDate
) {
    override fun toString(): String {
        return "Ferieuttak(fraOgMed=$fraOgMed, tilOgMed=$tilOgMed)"
    }
}

data class Frilans(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val startdato: LocalDate,
    @JsonAlias("jobber_fortsatt_som_frilans") val jobberFortsattSomFrilans: Boolean
)

enum class Årsak {
    BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING,
    BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD,
    ANNET,
}
