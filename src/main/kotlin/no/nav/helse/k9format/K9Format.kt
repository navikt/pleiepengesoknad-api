package no.nav.helse.k9format

import no.nav.fpsak.tidsserie.LocalDateTimeline
import no.nav.helse.soker.Søker
import no.nav.helse.soknad.*
import no.nav.helse.soknad.Beredskap
import no.nav.helse.soknad.Nattevåk
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.personopplysninger.Bosteder
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold.UtenlandsoppholdPeriodeInfo
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.psb.v1.Beredskap.BeredskapPeriodeInfo
import no.nav.k9.søknad.ytelse.psb.v1.DataBruktTilUtledning
import no.nav.k9.søknad.ytelse.psb.v1.LovbestemtFerie
import no.nav.k9.søknad.ytelse.psb.v1.Nattevåk.NattevåkPeriodeInfo
import no.nav.k9.søknad.ytelse.psb.v1.Omsorg
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarn
import no.nav.k9.søknad.ytelse.psb.v1.Uttak
import no.nav.k9.søknad.ytelse.psb.v1.tilsyn.TilsynPeriodeInfo
import java.time.Duration
import java.time.ZonedDateTime
import no.nav.k9.søknad.Søknad as K9Søknad
import no.nav.k9.søknad.felles.personopplysninger.Barn as K9Barn
import no.nav.k9.søknad.felles.personopplysninger.Søker as K9Søker
import no.nav.k9.søknad.ytelse.psb.v1.Beredskap as K9Beredskap
import no.nav.k9.søknad.ytelse.psb.v1.Nattevåk as K9Nattevåk
import no.nav.k9.søknad.ytelse.psb.v1.tilsyn.Tilsynsordning as K9Tilsynsordning

const val DAGER_PER_UKE = 5
private val k9FormatVersjon = Versjon.of("1.0.0")

fun Søknad.tilK9Format(mottatt: ZonedDateTime, søker: Søker): K9Søknad {
    val søknadsperiode = Periode(fraOgMed, tilOgMed)
    val psb = PleiepengerSyktBarn()
        .medSøknadsperiode(søknadsperiode)
        .medBarn(barn.tilK9Barn())
        .medOpptjeningAktivitet(byggK9OpptjeningAktivitet())
        .medArbeidstid(byggK9Arbeidstid())
        .medUttak(byggK9Uttak(søknadsperiode))
        .medBosteder(medlemskap.tilK9Bosteder())
        .medSøknadInfo(byggK9DataBruktTilUtledning())

    barnRelasjon?.let { psb.medOmsorg(byggK9Omsorg()) }
    beredskap?.let { if (it.beredskap) psb.medBeredskap(beredskap.tilK9Beredskap(søknadsperiode)) }
    nattevåk?.let { if (it.harNattevåk == true) psb.medNattevåk(nattevåk.tilK9Nattevåk(søknadsperiode)) }

    when (omsorgstilbud) {
        null -> psb.medTilsynsordning(tilK9Tilsynsordning0Timer(søknadsperiode))
        else -> psb.medTilsynsordning(omsorgstilbud.tilK9Tilsynsordning(søknadsperiode))
    }

    ferieuttakIPerioden?.let {
        if (it.ferieuttak.isNotEmpty() && it.skalTaUtFerieIPerioden) {
            psb.medLovbestemtFerie(ferieuttakIPerioden.tilK9LovbestemtFerie())
        }
    }

    if(utenlandsoppholdIPerioden.skalOppholdeSegIUtlandetIPerioden == true){
        psb.medUtenlandsopphold(utenlandsoppholdIPerioden.tilK9Utenlandsopphold())
    }

    return K9Søknad(SøknadId.of(søknadId), k9FormatVersjon, mottatt, søker.tilK9Søker(), psb)
}

fun Søker.tilK9Søker(): K9Søker = K9Søker(NorskIdentitetsnummer.of(fødselsnummer))

fun BarnDetaljer.tilK9Barn(): K9Barn = when {
    fødselsnummer != null -> K9Barn().medNorskIdentitetsnummer(NorskIdentitetsnummer.of(fødselsnummer))
    fødselsdato != null -> K9Barn().medFødselsdato(fødselsdato)
    else -> K9Barn()
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

internal fun UtenlandsoppholdIPerioden.tilK9Utenlandsopphold(): Utenlandsopphold {
    val perioder = mutableMapOf<Periode, UtenlandsoppholdPeriodeInfo>()

    opphold.forEach { utenlandsopphold ->
        var tidslinjeUtenInnleggelse = LocalDateTimeline(utenlandsopphold.fraOgMed, utenlandsopphold.tilOgMed, 1)

        utenlandsopphold.perioderBarnetErInnlagt.forEach { periodeMedInnleggelse ->
            tidslinjeUtenInnleggelse = tidslinjeUtenInnleggelse.disjoint(periodeMedInnleggelse.somLocalDateInterval())
            perioder[Periode(periodeMedInnleggelse.fraOgMed, periodeMedInnleggelse.tilOgMed)] = utenlandsopphold.somUtenlandsoppholdPeriodeInfo()
        }

        val gjenværendePerioderUtenInnleggelse = tidslinjeUtenInnleggelse.toSegments().map {
            no.nav.helse.soknad.Periode(it.fom, it.tom)
        }

        gjenværendePerioderUtenInnleggelse.forEach { periodeUtenInnleggelse ->
            perioder[Periode(periodeUtenInnleggelse.fraOgMed, periodeUtenInnleggelse.tilOgMed)] = UtenlandsoppholdPeriodeInfo()
                .medLand(Landkode.of(utenlandsopphold.landkode))
        }
    }

    return Utenlandsopphold().medPerioder(perioder)
}

private fun no.nav.helse.soknad.Utenlandsopphold.somUtenlandsoppholdPeriodeInfo() =
    UtenlandsoppholdPeriodeInfo()
        .medLand(Landkode.of(landkode))
        .apply {
            if (årsak != null && årsak != no.nav.helse.soknad.Årsak.ANNET) {
                medÅrsak(årsak.tilK9Årsak())
            }
        }
