package no.nav.helse.k9format

import no.nav.helse.soker.Søker
import no.nav.helse.soknad.*
import no.nav.helse.soknad.Beredskap
import no.nav.helse.soknad.Nattevåk
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.personopplysninger.Bosteder
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold.UtenlandsoppholdPeriodeInfo
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold.UtenlandsoppholdÅrsak
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.psb.v1.*
import no.nav.k9.søknad.ytelse.psb.v1.Beredskap.BeredskapPeriodeInfo
import no.nav.k9.søknad.ytelse.psb.v1.Nattevåk.NattevåkPeriodeInfo
import no.nav.k9.søknad.ytelse.psb.v1.tilsyn.TilsynPeriodeInfo
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.streams.toList
import no.nav.k9.søknad.Søknad as K9Søknad
import no.nav.k9.søknad.felles.personopplysninger.Barn as K9Barn
import no.nav.k9.søknad.felles.personopplysninger.Søker as K9Søker
import no.nav.k9.søknad.ytelse.psb.v1.Beredskap as K9Beredskap
import no.nav.k9.søknad.ytelse.psb.v1.Nattevåk as K9Nattevåk
import no.nav.k9.søknad.ytelse.psb.v1.tilsyn.Tilsynsordning as K9Tilsynsordning

const val DAGER_PER_UKE = 5
private val k9FormatVersjon = Versjon.of("1.0.0")

fun Søknad.tilK9Format(mottatt: ZonedDateTime, søker: Søker, dagensDato: LocalDate = LocalDate.now()): K9Søknad {
    val søknadsperiode = Periode(fraOgMed, tilOgMed)
    val psb = PleiepengerSyktBarn()
        .medSøknadsperiode(søknadsperiode)
        .medBarn(barn.tilK9Barn())
        .medOpptjeningAktivitet(byggK9OpptjeningAktivitet())
        .medArbeidstid(byggK9Arbeidstid(dagensDato))
        .medUttak(byggK9Uttak(søknadsperiode))
        .medBosteder(medlemskap.tilK9Bosteder())
        .medSøknadInfo(byggK9DataBruktTilUtledning())

    barnRelasjon?.let { psb.medOmsorg(byggK9Omsorg()) }
    beredskap?.let { if (it.beredskap) psb.medBeredskap(beredskap.tilK9Beredskap(søknadsperiode)) }
    nattevåk?.let { if (it.harNattevåk == true) psb.medNattevåk(nattevåk.tilK9Nattevåk(søknadsperiode)) }

    when {
        omsorgstilbud != null -> psb.medTilsynsordning(omsorgstilbud.tilK9Tilsynsordning(søknadsperiode, dagensDato))
        else -> psb.medTilsynsordning(tilK9Tilsynsordning0Timer(søknadsperiode))
    }

    ferieuttakIPerioden?.let {
        if (it.ferieuttak.isNotEmpty() && it.skalTaUtFerieIPerioden) {
            psb.medLovbestemtFerie(ferieuttakIPerioden.tilK9LovbestemtFerie())
        }
    }
    utenlandsoppholdIPerioden?.let {
        psb.medUtenlandsopphold(utenlandsoppholdIPerioden.tilK9Utenlandsopphold())
    }

    return K9Søknad(SøknadId.of(søknadId), k9FormatVersjon, mottatt, søker.tilK9Søker(), psb)
}

fun Søker.tilK9Søker(): K9Søker = K9Søker(NorskIdentitetsnummer.of(fødselsnummer))

fun BarnDetaljer.tilK9Barn(): K9Barn {
    val k9Barn = K9Barn()

    if(this.fødselsnummer != null){
        k9Barn.medNorskIdentitetsnummer(NorskIdentitetsnummer.of(this.fødselsnummer))
    } else k9Barn.medFødselsdato(this.fødselsdato)

    return k9Barn
}

fun Søknad.byggK9DataBruktTilUtledning(): DataBruktTilUtledning = DataBruktTilUtledning(
    harForståttRettigheterOgPlikter,
    harBekreftetOpplysninger,
    samtidigHjemme,
    harMedsøker,
    null
)

fun Søknad.byggK9Omsorg() = Omsorg()
    .medRelasjonTilBarnet(
        when (barnRelasjon) {
            BarnRelasjon.FAR -> Omsorg.BarnRelasjon.FAR
            BarnRelasjon.MOR -> Omsorg.BarnRelasjon.MOR
            BarnRelasjon.FOSTERFORELDER -> Omsorg.BarnRelasjon.FOSTERFORELDER
            BarnRelasjon.MEDMOR -> Omsorg.BarnRelasjon.MEDMOR
            BarnRelasjon.ANNET -> Omsorg.BarnRelasjon.ANNET
            else -> null
        }
    ).medBeskrivelseAvOmsorgsrollen(barnRelasjonBeskrivelse)

fun Beredskap.tilK9Beredskap(
    periode: Periode
): K9Beredskap = K9Beredskap()
    .medPerioder(
        mapOf(
            periode to BeredskapPeriodeInfo()
                .medTilleggsinformasjon(tilleggsinformasjon)
        )
    )

fun Nattevåk.tilK9Nattevåk(
    periode: Periode
): K9Nattevåk? = K9Nattevåk().medPerioder(
    mapOf(
        periode to NattevåkPeriodeInfo().medTilleggsinformasjon(tilleggsinformasjon)
    )
)

fun tilK9Tilsynsordning0Timer(periode: Periode) = K9Tilsynsordning().apply {
    leggeTilPeriode(
        periode,
        TilsynPeriodeInfo().medEtablertTilsynTimerPerDag(
            Duration.ZERO
        )
    )
}

fun Omsorgstilbud.tilK9Tilsynsordning(periode: Periode, dagensDato: LocalDate = LocalDate.now()): K9Tilsynsordning =
    K9Tilsynsordning().apply {

        if (historisk == null && planlagt == null) return tilK9Tilsynsordning0Timer(periode)

        historisk?.ukedager?.apply {
            val gårsdagensDato = dagensDato.minusDays(1)
            val periodeTilOgMed = if(gårsdagensDato.isBefore(periode.tilOgMed)) gårsdagensDato else periode.tilOgMed
            periode.fraOgMed.datesUntil(periodeTilOgMed.plusDays(1)).toList()
                .map { dato: LocalDate ->
                    when(dato.dayOfWeek){
                        DayOfWeek.MONDAY -> mandag
                        DayOfWeek.TUESDAY -> tirsdag
                        DayOfWeek.WEDNESDAY -> onsdag
                        DayOfWeek.THURSDAY -> torsdag
                        DayOfWeek.FRIDAY -> fredag
                        else -> null
                    }?.let {tilsynLengde: Duration ->
                        leggeTilPeriode(
                            Periode(dato, dato),
                            TilsynPeriodeInfo().medEtablertTilsynTimerPerDag(
                                Duration.ZERO.plusOmIkkeNullOgAvkortTilNormalArbeidsdag(tilsynLengde)
                            )
                        )
                    }
                }
        }

        historisk?.enkeltdager?.forEach {
            leggeTilPeriode(
                Periode(it.dato, it.dato),
                TilsynPeriodeInfo().medEtablertTilsynTimerPerDag(
                    Duration.ZERO.plusOmIkkeNullOgAvkortTilNormalArbeidsdag(it.tid)
                )
            )
        }

        planlagt?.ukedager?.apply {
            val periodeStart = if (dagensDato.isBefore(periode.fraOgMed)) periode.fraOgMed else dagensDato
            periodeStart.datesUntil(periode.tilOgMed.plusDays(1)).toList()
                .map { dato: LocalDate ->
                    when (dato.dayOfWeek) {
                        DayOfWeek.MONDAY -> mandag
                        DayOfWeek.TUESDAY -> tirsdag
                        DayOfWeek.WEDNESDAY -> onsdag
                        DayOfWeek.THURSDAY -> torsdag
                        DayOfWeek.FRIDAY -> fredag
                        else -> null
                    }?.let { tilsynLengde: Duration ->
                        leggeTilPeriode(
                            Periode(dato, dato),
                            TilsynPeriodeInfo().medEtablertTilsynTimerPerDag(
                                Duration.ZERO.plusOmIkkeNullOgAvkortTilNormalArbeidsdag(tilsynLengde)
                            )
                        )
                    }
                }
        }

        planlagt?.enkeltdager?.forEach {
            leggeTilPeriode(
                Periode(it.dato, it.dato),
                TilsynPeriodeInfo().medEtablertTilsynTimerPerDag(
                    Duration.ZERO.plusOmIkkeNullOgAvkortTilNormalArbeidsdag(it.tid)
                )
            )
        }
    }

fun Søknad.byggK9Uttak(periode: Periode): Uttak? {
    val perioder = mutableMapOf<Periode, Uttak.UttakPeriodeInfo>()

    perioder[periode] = Uttak.UttakPeriodeInfo(Duration.ofHours(7).plusMinutes(30))

    return Uttak().medPerioder(perioder)
}

fun FerieuttakIPerioden.tilK9LovbestemtFerie(): LovbestemtFerie {
    if (!skalTaUtFerieIPerioden) return LovbestemtFerie()

    val perioder = mutableMapOf<Periode, LovbestemtFerie.LovbestemtFeriePeriodeInfo>()

    ferieuttak.forEach { ferieuttak ->
        perioder[Periode(ferieuttak.fraOgMed, ferieuttak.tilOgMed)] = LovbestemtFerie.LovbestemtFeriePeriodeInfo()
    }

    return LovbestemtFerie().medPerioder(perioder)
}

fun Medlemskap.tilK9Bosteder(): Bosteder? {
    val perioder = mutableMapOf<Periode, Bosteder.BostedPeriodeInfo>()

    utenlandsoppholdSiste12Mnd.forEach { bosted ->
        if (!bosted.landkode.isEmpty()) perioder[Periode(bosted.fraOgMed, bosted.tilOgMed)] =
            Bosteder.BostedPeriodeInfo()
                .medLand(Landkode.of(bosted.landkode))
    }

    utenlandsoppholdNeste12Mnd.forEach { bosted ->
        if (!bosted.landkode.isNullOrEmpty()) perioder[Periode(bosted.fraOgMed, bosted.tilOgMed)] =
            Bosteder.BostedPeriodeInfo()
                .medLand(Landkode.of(bosted.landkode))
    }

    return Bosteder().medPerioder(perioder)
}

private fun UtenlandsoppholdIPerioden.tilK9Utenlandsopphold(): Utenlandsopphold {
    val perioder = mutableMapOf<Periode, UtenlandsoppholdPeriodeInfo>()

    opphold.forEach { utenlandsopphold ->
        val årsak = when (utenlandsopphold.årsak) {
            Årsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING -> UtenlandsoppholdÅrsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING
            Årsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD -> UtenlandsoppholdÅrsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD
            else -> null
        }
        val periode = Periode(utenlandsopphold.fraOgMed, utenlandsopphold.tilOgMed)
        perioder[periode] = UtenlandsoppholdPeriodeInfo()
            .medLand(Landkode.of(utenlandsopphold.landkode))
            .apply { årsak?.let { medÅrsak(årsak) } }
    }

    return Utenlandsopphold().medPerioder(perioder)
}