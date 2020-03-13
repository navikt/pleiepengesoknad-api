package no.nav.helse.soknad

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URL
import java.time.Duration
import java.time.LocalDate

enum class Sprak { nb, nn }

data class Soknad(
    val newVersion: Boolean?,
    val sprak: Sprak? = null,
    val barn: BarnDetaljer,
    val relasjonTilBarnet: String? = null,
    val arbeidsgivere: ArbeidsgiverDetaljer,
    val vedlegg: List<URL>,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val medlemskap: Medlemskap,
    @JsonProperty("bekrefter_periode_over_8_uker")
    val bekrefterPeriodeOver8Uker: Boolean? = null,// TODO: Fjern optional når prodsatt.
    @JsonProperty("utenlandsopphold_i_perioden")
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden?,
    @JsonProperty("ferieuttak_i_perioden")
    val ferieuttakIPerioden: FerieuttakIPerioden?,
    val harMedsoker: Boolean? = null,
    val samtidigHjemme: Boolean? = null,
    val harForstattRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val tilsynsordning: Tilsynsordning?,
    val nattevaak: Nattevaak? = null,
    val beredskap: Beredskap? = null,
    val harHattInntektSomFrilanser: Boolean = false,
    val frilans: Frilans? = null,
    val harHattInntektSomSelvstendigNaringsdrivende: Boolean = false,
    @JsonProperty("selvstendig_virksomheter")
    val selvstendigVirksomheter: List<Virksomhet>? = null,
    @JsonProperty("skal_bekrefte_omsorg") val skalBekrefteOmsorg: Boolean? = null, // TODO: Fjern optional når prodsatt.
    @JsonProperty("skal_passe_pa_barnet_i_hele_perioden") val skalPassePaBarnetIHelePerioden: Boolean? = null, // TODO: Fjern optional når prodsatt.
    @JsonProperty("beskrivelse_omsorgsrollen") val beskrivelseOmsorgsRollen: String? = null // TODO: Fjern optional når prodsatt.
)

data class ArbeidsgiverDetaljer(
    val organisasjoner: List<OrganisasjonDetaljer>
)

data class Medlemskap(
    @JsonProperty("har_bodd_i_utlandet_siste_12_mnd")
    val harBoddIUtlandetSiste12Mnd: Boolean? = null,
    @JsonProperty("utenlandsopphold_siste_12_mnd")
    val utenlandsoppholdSiste12Mnd: List<Bosted> = listOf(),
    @JsonProperty("skal_bo_i_utlandet_neste_12_mnd")
    val skalBoIUtlandetNeste12Mnd: Boolean? = null,
    @JsonProperty("utenlandsopphold_neste_12_mnd")
    val utenlandsoppholdNeste12Mnd: List<Bosted> = listOf()
)

data class UtenlandsoppholdIPerioden(
    @JsonProperty("skal_oppholde_seg_i_utlandet_i_perioden")
    val skalOppholdeSegIUtlandetIPerioden: Boolean? = null,
    val opphold: List<Utenlandsopphold> = listOf()
)

data class BarnDetaljer(
    val fodselsnummer: String?,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fodselsdato: LocalDate?,
    val aktoerId: String?,
    val navn: String?
) {
    override fun toString(): String {
        return "BarnDetaljer(aktoerId=${aktoerId}, navn=${navn}, fodselsdato=${fodselsdato}"
    }
}

data class OrganisasjonDetaljer(
    val navn: String? = null,
    val skalJobbe: String? = null,
    val organisasjonsnummer: String,
    val jobberNormaltTimer: Double? = null, //TODO: Fjern ? når dette er prodsatt. skalJobbeProsent skal ikke lenger være optional.
    val skalJobbeProsent: Double? = null,  //TODO: Fjern ? når dette er prodsatt. skalJobbeProsent skal ikke lenger være optional.
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

data class Nattevaak(
    val harNattevaak: Boolean? = null,
    val tilleggsinformasjon: String?
) {
    override fun toString(): String {
        return "Nattevaak(harNattevaak=${harNattevaak})"
    }
}

data class Beredskap(
    @JsonProperty("i_beredskap")
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
    val arsak: Arsak?
) {
    override fun toString(): String {
        return "Utenlandsopphold(fraOgMed=$fraOgMed, tilOgMed=$tilOgMed, landkode='$landkode', landnavn='$landnavn', erUtenforEos=$erUtenforEos, erBarnetInnlagt=$erBarnetInnlagt, arsak=$arsak)"
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
    @JsonProperty("skal_ta_ut_ferie_i_periode")
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

enum class Arsak {
    BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING,
    BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD,
    ANNET,
}
